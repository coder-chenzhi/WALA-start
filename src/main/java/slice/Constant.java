package slice;

public class Constant {
	public static final String EXCLUSIONS = "java\\/awt\\/.*\n" +
		      "javax\\/swing\\/.*\n" +
		      "sun\\/awt\\/.*\n" +
		      "sun\\/swing\\/.*\n" +
		      "com\\/sun\\/.*\n" +
		      "sun\\/.*\n" +
		      "org\\/netbeans\\/.*\n" +
		      "org\\/openide\\/.*\n" +
		      "com\\/ibm\\/crypto\\/.*\n" +
		      "com\\/ibm\\/security\\/.*\n" +
		      "org\\/apache\\/xerces\\/.*\n" +
		      "java\\/security\\/.*\n" +
		      "java\\/io\\/ObjectStreamClass*\n" +
		      "apple\\/.*\n" +
		      "com\\/apple\\/.*\n" +
		      "jdk\\/.*\n" +
		      "org\\/omg\\/.*\n" +
		      "org\\/w3c\\/.*\n" +
		      "";
	public static final String SCOPE = "./dat/scope.txt";
	public static final String SOURCECODEFILE = "./dat/walaTest.java";
	public static final String SOURCECLASSFILE= "./dat/walaTest.class";
	public static final String EXCLUSIONFILE = "./dat/exclusions.txt";
	public static final String SLICEPARAM = "./dat/hadoop_refactoring.log";
	
	public static final String repoPath = "/Users/jinfu/Documents/workspace/Git/hadoop/";
	public static final String repoUrl = "https://github.com/apache/hadoop.git";
	public static final String commitId = "248d9b6fff648cdb02581d458556b6f7c090ef1a";
	
	//When java execute shell command it will search the command from /bin. 
	//Hence, if the command is not in path /bin, it will fatal
	public static final String MVNCMD = "mvn";
	public static final String ARGS1 = "compile"; //improve here to compile only sub-project to save time
	public static final String ARGS2 = "-DskipTests";
	
	public static final int LINENUM = 13;
}
