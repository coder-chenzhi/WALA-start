package org.maven;

import slice.Constant;

public class mavenMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		MavenImpl mvnImpl = new MavenImpl();
		mvnImpl.mavenCompile(Constant.repoPath);
	}
}
