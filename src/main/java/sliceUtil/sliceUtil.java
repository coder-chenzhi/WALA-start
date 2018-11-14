package sliceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.util.strings.StringStuff;

public class sliceUtil {
	
	/**
	 * construct method entry point based on method name, call graph will build based on the method entry
	 * @param clr ClassLoaderReference
	 * @param cha IClassHierarchy 
	 * @param methodName,
	 * 
	 */
	public Iterable<Entrypoint> makeMethodEntrypoints(ClassLoaderReference clr, IClassHierarchy cha, String methodName) {
		assert null != clr : "\"clr\" is null.";
		assert null != cha : "\"cha\" is null.";
		Atom name = Atom.findOrCreateUnicodeAtom(methodName);
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass iclass : cha) {
			if (iclass.getClassLoader().getReference().equals(clr)) {
				for (IMethod imethod : iclass.getAllMethods()) {					
					if (imethod.getName().equals(name)) {
						if (imethod != null) {
							result.add(new DefaultEntrypoint(imethod, cha));
						}
					}
				}
			}
		}
		return new Iterable<Entrypoint>() {
			@Override
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		};
	}
	/**
	 * construct entry point based on method name and method descriptor, call graph will build based on the method entry
	 * @param clr ClassLoaderReference
	 * @param cha IClassHierarchy
	 * @param methodName
	 * @param method descriptor, something like "([Ljava/lang/String;)V"
	 */
    public Iterable<Entrypoint> makeMethodEntrypoints(ClassLoaderReference clr, IClassHierarchy cha, String methodName, String descriptor) {
    	assert null != clr : "\"clr\" is null.";
        assert null != cha : "\"cha\" is null.";
        final Atom entryMethod = Atom.findOrCreateAsciiAtom(methodName);
        final HashSet<Entrypoint> result = HashSetFactory.make();
        for (IClass iclass : cha) {
          if (iclass.getClassLoader().getReference().equals(clr)) {
            MethodReference mainRef = MethodReference.findOrCreate(iclass.getReference(), entryMethod, Descriptor
                .findOrCreateUTF8(descriptor));
            IMethod m = iclass.getMethod(mainRef.getSelector());
            if (m != null) {
              result.add(new DefaultEntrypoint(m, cha));
            }
          }
        }
        return new Iterable<Entrypoint>() {
          @Override
          public Iterator<Entrypoint> iterator() {
            return result.iterator();
          }
        };
      }
	/**
	 * construct entry point based on class name
	 * @param clr ClassLoaderReference
	 * @param cha IClassHierarchy
	 * @param entryClass class name
	 */
	public Iterable<Entrypoint> makeClassEntrypoints(AnalysisScope scope, IClassHierarchy cha, String entryClass) {
    	assert null != scope : "\"scope\" is null.";
        assert null != cha : "\"cha\" is null.";
        Collection<Entrypoint> result = new ArrayList<Entrypoint>();
        IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application,
            StringStuff.deployment2CanonicalTypeString(entryClass)));
        for (IMethod m : klass.getDeclaredMethods()) {
          if (m.isPublic()) {
            result.add(new DefaultEntrypoint(m, cha));
          }
        }
        return result;
      }
	/**
	 * find slicing entry method in the call graph
	 * @param cg call graph
	 * @param methodName
	 * @param method descriptor, "([Ljava/lang/String;)V"
	 */
    public CGNode findSliceMethod(CallGraph cg, String methodName, String descriptor) {
        Descriptor d = Descriptor.findOrCreateUTF8(descriptor);
        Atom name = Atom.findOrCreateUnicodeAtom(methodName);
        for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();) {
          CGNode n = it.next();
          if (n.getMethod().getName().equals(name) && n.getMethod().getDescriptor().equals(d)) {
            return n;
          }
        }
        Assertions.UNREACHABLE("failed to find main() method");
        return null;
      }
	/**
	 * find slicing entry method in the call graph
	 * @param cg call graph
	 * @param methodName
	 */
    public CGNode findSliceMethod(CallGraph cg, String methodName) {
        Atom name = Atom.findOrCreateUnicodeAtom(methodName);
        for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();) {
          CGNode n = it.next();
          if (n.getMethod().getName().equals(name)) {
            return n;
          }
        }
        Assertions.UNREACHABLE("failed to find main() method");
        return null;
      }
	/**
	 * return seed statement if it is invoke statement (function call)
	 * @param n CGNode
	 * @param methodName
	 */
    public Statement findInvokeStatement(CGNode n, String methodName) {
        IR ir = n.getIR();
        for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
          SSAInstruction s = it.next();
          if (s instanceof SSAAbstractInvokeInstruction) {
            SSAAbstractInvokeInstruction call = (SSAAbstractInvokeInstruction) s;
            if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
              com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
              com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
              return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
            }
          }
        
        }
        Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
        return null;
      }
	/**
	 * return seed statement if it is the first new statement
	 * @param n CGNode
	 * @param methodName
	 */
    public Statement findAllocationStatement(CGNode n) {
        IR ir = n.getIR();
        for (int i = 0; i < ir.getInstructions().length; i++) {
          SSAInstruction s = ir.getInstructions()[i];
          if (s instanceof SSANewInstruction) {
            return new NormalStatement(n, i);
          }
        }
        Assertions.UNREACHABLE("failed to find allocation in " + n);
        return null;
      }
	/**
	 * return seed statement based on source code line number
	 * @param n CGNode
	 * @param methodName
	 * @throws InvalidClassFileException 
	 */
	public Statement findSeedStatement(CGNode n, int lineNum) throws InvalidClassFileException {
		IR ir = n.getIR();
		IBytecodeMethod<?> method = (IBytecodeMethod<?>) ir.getMethod();
		for (int i = 0; i < ir.getInstructions().length; i++) {
			int bytecodeIndex = method.getBytecodeIndex(i);
			int sourceLineNum = method.getLineNumber(bytecodeIndex);
			if (sourceLineNum == lineNum) {
				SSAInstruction s = ir.getInstructions()[i];
				if (s instanceof SSANewInstruction)
					return new NormalStatement(n, i);
				else if (s instanceof SSAAbstractInvokeInstruction) {
					SSAAbstractInvokeInstruction call = (SSAAbstractInvokeInstruction) s;
					com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1,
							"expected 1 but got " + indices.size());
					return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
				}
			}
		}

		Assertions.UNREACHABLE("failed to find allocation in " + n);
		return null;
	}
	/**
	 * output slice statements
	 */

	public TreeSet<Integer> mapSourcecode(Collection<Statement> slice) {
		TreeSet<Integer> slices = new TreeSet<Integer>();
		for (Statement s : slice) {
			if (s.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
				// System.out.println(s);
				int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
				try {
					bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
					try {
						int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
						// System.out.println ( "Source line number = " + src_line_number );
						slices.add(src_line_number);
					} catch (Exception e) {
						System.err.println("Bytecode index no good");
						System.err.println(e.getMessage());
					}
				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					System.err.println(e.getMessage());
				}
			}
		}
		return slices;
	}
    
	/**
	 * function : for statement node
	 * 
	 * @param statement ASTNode
	 * @return method contain this statement
	 */
	public ASTNode findParentMethod(ASTNode node) {
		int parentNodeType = node.getParent().getNodeType();
		if (parentNodeType == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		}
		if (parentNodeType == ASTNode.INITIALIZER){
			return node.getParent();
		}
		if (parentNodeType == ASTNode.TYPE_DECLARATION){
			return node.getParent();
		}
		return findParentMethod(node.getParent());
	}
}
