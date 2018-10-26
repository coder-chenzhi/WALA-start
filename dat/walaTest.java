package slice;

import java.awt.List;
import java.util.Hashtable;

public class walaTest {
	public void bajin(String[] args) {
	    int i = 1;
	    i = i + 1;
	    int j = 2;
	    int slice = 4;
	    String str2 = "slice";
	    Hashtable abc = new Hashtable(i, 0.75f);
	    String str = new String("test");
	    i = i + 3;
	    j = j - 1;
	    call(str,i,j,abc,str2);
	}
	private static void call(String str,int a, int b, Hashtable ht,String str2){
		int c = a + b;
		call2(ht);
		System.out.println(str);
		System.out.println(str2);
	}
	private static void call2(Hashtable ht2){
		int c = 9;
		ht2.put(4, 0.8);
	}
}
