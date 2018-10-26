package slice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Assert;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JavaLanguage;
import com.ibm.wala.classLoader.ShrikeBTMethod;
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
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.util.warnings.Warnings;

import sliceUtil.intraPSlicer;
import sliceUtil.sliceUtil;

public class sliceMain {
	public static void main(String[] args) throws CancelException, IOException, ClassHierarchyException {
		//1.create an analysis scope representing the source file application
		AnalysisScope scope = AnalysisScopeReader.readJavaScope(Constant.SCOPE, 
				(new FileProvider()).getFile(Constant.EXCLUSIONFILE), 
				sliceMain.class.getClassLoader());
		//2. build a class hierarchy, call graph, and system dependence graph
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		System.out.println(cha.getNumberOfClasses() + " classes");
		Warnings.clear();
		//3. make entry point
		sliceUtil sUtil = new sliceUtil();
		String methodName = "bajin";
		String descriptor = "([Ljava/lang/String;)V";
		Iterable<Entrypoint> entrypoints = sUtil.makeEntrypoints(scope.getApplicationLoader(), cha, methodName, descriptor);
		//4. analysis options
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		//5. Build the call graph
		JavaLanguage JAVA = new JavaLanguage();
		CallGraphBuilder<?> builder = Util.makeZeroCFABuilder(JAVA,options, new AnalysisCacheImpl(),cha, scope, null, null);
		CallGraph cg = builder.makeCallGraph(options, null);
		PointerAnalysis<?> pa = builder.getPointerAnalysis();
		
		//6. Find seed statement
		CGNode cgNode = sUtil.findMainMethod(cg, methodName, descriptor);		
		Statement statement = sUtil.findFirstAllocation(cgNode);
		
		//7. interprocedural program slicing
		Collection<Statement> slice1 = Slicer.computeBackwardSlice(statement, cg, pa, DataDependenceOptions.REFLECTION, ControlDependenceOptions.NONE);
		Collection<Statement> slice2 = Slicer.computeForwardSlice(statement, cg, pa, DataDependenceOptions.REFLECTION, ControlDependenceOptions.NONE);
        
		
		//8. intraprocedural program slicing
		intraPSlicer ipSlicer = new intraPSlicer();
		ipSlicer.intraSlice(Constant.SOURCECODEFILE, Constant.LINENUM);
		
		//9. output slicing statements
		sUtil.dumpSlice(slice1);
		sUtil.dumpSlice(slice2);
	}

}

