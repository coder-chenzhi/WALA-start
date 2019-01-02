package slice;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.util.Iterator;

public class MethodInvocation {
    public static void main(String[] args) throws Exception {
        // 1.create an analysis scope representing the source file application
        File exFile = new FileProvider().getFile("./dat/my_exclusion.txt");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(Constant.SOURCEJARFILE, exFile);
        // 2. build a class hierarchy, call graph, and system dependence graph
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        System.out.println(cha.getNumberOfClasses() + " classes");

        Iterator<IClass> classes = cha.iterator();
        IAnalysisCacheView cache = new AnalysisCacheImpl();

        IClass clazz;
        while (classes.hasNext()) {
            clazz = classes.next();
            if (!clazz.toString().contains("zju")) {
                continue;
            }
            System.out.println("class:\t" + clazz);
            for(IMethod m: clazz.getDeclaredMethods()) {
                System.out.println("method:\t" + m);
                try {
                    IR ir = cache.getIR(m);
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (int i = 0; i < instructions.length; i++) {
                        SSAInstruction instruction = instructions[i];
                        if (instruction instanceof SSAInvokeInstruction) {
                            IMethod callee = cha.resolveMethod(((SSAInvokeInstruction) instruction).getDeclaredTarget());
                            String className = callee.getDeclaringClass().getName().toString();
                            if (className.equals("Lorg/slf4j/Logger")) {
                                System.out.println(callee);
                            }
                        }
                    }
                }catch (Throwable e){
                    System.out.println("Error while creating IR for method: " + m.getReference() + "\n"+ e);
                }
            }
        }
    }
}
