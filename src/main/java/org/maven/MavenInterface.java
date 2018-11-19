package org.maven;

import java.io.IOException;

import org.apache.maven.shared.invoker.MavenInvocationException;

public interface MavenInterface {
	
	public void mavenCompile(String path) throws Exception;
	
	public void mvnScript(String script) throws IOException, InterruptedException;
	
	public void invokeMaven(String pomPath) throws MavenInvocationException;
}
