package ioUtil;

public class SliceParam {
	private String commitID;
	private String className;
	private String methodEntry;
	private String methodExtract;
	private int invokeLine;
	private int startLine;
	private int endLIne;
	
	public String getCommitID() {
		return commitID;
	}
	public void setCommitID(String commitID) {
		this.commitID = commitID;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodEntry() {
		return methodEntry;
	}
	public void setMethodEntry(String methodEntry) {
		this.methodEntry = methodEntry;
	}
	public String getMethodExtract() {
		return methodExtract;
	}
	public void setMethodExtract(String methodExtract) {
		this.methodExtract = methodExtract;
	}
	public int getStartLine() {
		return startLine;
	}
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}
	public int getEndLIne() {
		return endLIne;
	}
	public void setEndLIne(int endLIne) {
		this.endLIne = endLIne;
	}	
	public int getInvokeLine() {
		return invokeLine;
	}
	public void setInvokeLine(int invokeLine) {
		this.invokeLine = invokeLine;
	}
}
