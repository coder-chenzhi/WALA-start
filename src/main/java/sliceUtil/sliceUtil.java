package sliceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
	
	/*
	 * construct entry point based on method name and method descriptor
	 * param@ clr ClassLoaderReference
	 * param@ cha IClassHierarchy
	 * param@ methodName, bajin
	 * param@ method descriptor, "([Ljava/lang/String;)V"
	 */
    public Iterable<Entrypoint> makeEntrypoints(ClassLoaderReference clr, IClassHierarchy cha, String methodName, String descriptor) {
        assert null != cha : "\"cha\" is null.";
        final Atom entryMethod = Atom.findOrCreateAsciiAtom(methodName);
        final HashSet<Entrypoint> result = HashSetFactory.make();
        for (IClass klass : cha) {
          if (klass.getClassLoader().getReference().equals(clr)) {
            MethodReference mainRef = MethodReference.findOrCreate(klass.getReference(), entryMethod, Descriptor
                .findOrCreateUTF8(descriptor));
            IMethod m = klass.getMethod(mainRef.getSelector());
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
	/*
	 * construct entry point based on class name
	 * param@ clr ClassLoaderReference
	 * param@ cha IClassHierarchy
	 * param@ entryClass class name
	 */
    @SuppressWarnings("unused")
	private static Iterable<Entrypoint> makePublicEntrypoints(AnalysisScope scope, IClassHierarchy cha, String entryClass) {
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
	/*
	 * find main method in the call graph
	 * param@ cg call graph
	 * param@ methodName, bajin
	 * param@ method descriptor, "([Ljava/lang/String;)V"
	 */
    public CGNode findMainMethod(CallGraph cg, String methodName, String descriptor) {
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
	/*
	 * return seed statement based on function call
	 * param@ n CGNode
	 * param@ methodName, bajin
	 */
    public Statement findCallTo(CGNode n, String methodName) {
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
	/*
	 * return seed statement based on source code line number
	 * param@ n CGNode
	 * param@ methodName, bajin
	 */
    public Statement findFirstAllocation(CGNode n) {
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
	/*
	 * output slice statements
	 */
    public void dumpSlice(Collection<Statement> slice) {
        for (Statement s : slice) {
          mapSourcecode(s);
        }
      }
    private static void mapSourcecode(Statement s){
    	if (s.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
//            	System.out.println(s);
    		  int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
    		  try {
    		    bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
    		    try {
    		      int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
    		      System.out.println ( "Source line number = " + src_line_number );
    		    } catch (Exception e) {
    		      System.err.println("Bytecode index no good");
    		      System.err.println(e.getMessage());
    		    }
    		  } catch (Exception e ) {
    		    System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
    		    System.err.println(e.getMessage());
    		  }
    		}
    }
}
