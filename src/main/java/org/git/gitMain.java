package org.git;

import org.eclipse.jgit.lib.Repository;
import slice.Constant;

public class gitMain {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.cloneIfNotExists(Constant.repoPath, Constant.repoUrl);
		gitService.checkout(repo, Constant.commitId);
	}
}
