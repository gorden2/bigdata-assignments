import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

public class test {
    public static void main(String[] args){
	String str = "Hi this is 123good";
	String str2 = "";
	StringTokenizer itr = new StringTokenizer(str);
  	while (itr.hasMoreTokens()) {
		String token1 = itr.nextToken();
		System.out.println(token1);
		if (token1.matches("[A-Za-z]+")){
			str2+=token1;}

      }
	System.out.println(str2);
	
    }
}
