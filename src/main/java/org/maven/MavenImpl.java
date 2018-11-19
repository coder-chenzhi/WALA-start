package org.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slice.Constant;

public class MavenImpl implements MavenInterface{
	Logger logger = LoggerFactory.getLogger(MavenImpl.class);

	public void mavenCompile(String path) throws Exception {
		logger.info("compiling project ... {}", path);
		File workingDir = new File(path);
//		String output = ExternalProcess.execute(workingDir, Constant.mvnCMD, "compile", "-DskipTests");
		String output = ExternalProcess.execute(workingDir, Constant.MVNCMD, Constant.ARGS1, Constant.ARGS2);
		logger.info("compile done: {}", output);
		if (output.startsWith("fatal")) {
			throw new RuntimeException("maven compile error: " + output);
		}
		
	}

	public void mvnScript(String script) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(script);
		pb.directory(new File(Constant.repoPath));
		Process p = pb.start();
		p.waitFor();
	}
	
	public void invokeMaven(String pomPath) throws MavenInvocationException{
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile( new File(pomPath) );
		request.setGoals( Collections.singletonList( "compile" ) );
		Invoker invoker = new DefaultInvoker();
		InvocationResult result = invoker.execute( request );
		if ( result.getExitCode() != 0 )
		{
		    throw new IllegalStateException( "Build failed." );
		}
	}
}
