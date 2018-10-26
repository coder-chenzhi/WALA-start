package slice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Assert;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JavaLanguage;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.util.warnings.Warnings;

public class sliceBack {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws CancelException, IOException, ClassHierarchyException {
		//1.create an analysis scope representing the source file application
		AnalysisScope scope = AnalysisScopeReader.readJavaScope(Constant.SCOPE, 
				(new FileProvider()).getFile(Constant.EXCLUSIONFILE), 
				sliceBack.class.getClassLoader());
//		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("walaTest.class",null);
//		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(Constant.EXCLUSIONS.getBytes("UTF-8"))));
		//2. build a class hierarchy, call graph, and system dependence graph
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		System.out.println(cha.getNumberOfClasses() + " classes");
		Warnings.clear();
//		Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, "Lcom/ibm/wala/slicer/walaTest");
		Iterable<Entrypoint> entrypoints = makeEntrypoints(scope.getApplicationLoader(), cha);

		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		//Build the call graph
//		CallGraphBuilder<InstanceKey> builder = Util.makeVanillaZeroOneCFABuilder(options, new AnalysisCacheImpl(), cha, scope);
		JavaLanguage JAVA = new JavaLanguage();
		CallGraphBuilder builder = Util.makeZeroCFABuilder(JAVA,options, new AnalysisCacheImpl(),cha, scope, null, null);
		CallGraph cg = builder.makeCallGraph(options, null);
		PointerAnalysis pa = builder.getPointerAnalysis();
		
		//Find seed statement
		CGNode cgNode = findMainMethod(cg);		
//		Statement statement = findCallTo(cgNode, "call");
		Statement statement = findFirstAllocation(cgNode);
		Collection<Statement> slice1 = Slicer.computeBackwardSlice(statement, cg, pa, DataDependenceOptions.REFLECTION, ControlDependenceOptions.NONE);
		Collection<Statement> slice2 = Slicer.computeForwardSlice(statement, cg, pa, DataDependenceOptions.REFLECTION, ControlDependenceOptions.NONE);
        dumpSlice(slice1);
        dumpSlice(slice2);
	}
	
    public static CGNode findMainMethod(CallGraph cg) {
      Descriptor d = Descriptor.findOrCreateUTF8("([Ljava/lang/String;)V");
      Atom name = Atom.findOrCreateUnicodeAtom("bajin");
      for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();) {
        CGNode n = it.next();
        if (n.getMethod().getName().equals(name) && n.getMethod().getDescriptor().equals(d)) {
          return n;
        }
      }
      Assertions.UNREACHABLE("failed to find main() method");
      return null;
    }

    public static Statement findCallTo(CGNode n, String methodName) {
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

    public static void dumpSlice(Collection<Statement> slice) {
      for (Statement s : slice) {
        mapSourcecode(s);
      }
    }
    
    public static void mapSourcecode(Statement s){
    	if (s.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
            	System.out.println(s);
    		  int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
    		  try {
    		    bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
    		    try {
    		      int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
    		      System.err.println ( "Source line number = " + src_line_number );
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
    
    private static Iterable<Entrypoint> makePublicEntrypoints(AnalysisScope scope, IClassHierarchy cha, String entryClass) {
        Collection<Entrypoint> result = new ArrayList<Entrypoint>();
        IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application,
            StringStuff.deployment2CanonicalTypeString(entryClass)));
        System.out.println(entryClass);
        System.out.println(klass.getDeclaredMethods());
        for (IMethod m : klass.getDeclaredMethods()) {
          if (m.isPublic()) {
            result.add(new DefaultEntrypoint(m, cha));
          }
        }
        return result;
      }
    
    public static Iterable<Entrypoint> makeEntrypoints(ClassLoaderReference clr, IClassHierarchy cha) {
        if (cha == null) {
          throw new IllegalArgumentException("cha is null");
        }
        final Atom mainMethod = Atom.findOrCreateAsciiAtom("bajin");
        final HashSet<Entrypoint> result = HashSetFactory.make();
        for (IClass klass : cha) {
          if (klass.getClassLoader().getReference().equals(clr)) {
            MethodReference mainRef = MethodReference.findOrCreate(klass.getReference(), mainMethod, Descriptor
                .findOrCreateUTF8("([Ljava/lang/String;)V"));
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

    public static Statement findFirstAllocation(CGNode n) {
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

    public static void localnames(CGNode n){
    	System.err.println("localname");
    	IR ir = n.getIR();
    	String[] names = ir.getLocalNames(5, 1);
        System.out.println(names.length);
        System.out.println(names[0]);
        for (int offsetIndex = 0; offsetIndex < ir.getInstructions().length; offsetIndex++) {
            SSAInstruction instr = ir.getInstructions()[offsetIndex];
            if (instr != null) {
              String[] localNames = ir.getLocalNames(offsetIndex, instr.getDef());
              if (localNames != null && localNames.length > 0 && localNames[0] == null) {
                System.err.println(ir);
                Assert.assertTrue(" getLocalNames() returned [null,...] for the def of instruction at offset " + offsetIndex
                    + "\n\tinstr", false);
              }
            }
          }
    }
}

