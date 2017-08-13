/**
 * Created by IntelliJ IDEA.
 * User: dm
 * Date: Apr 17, 2003
 * Time: 2:30:04 PM
 * To change this template use Options | File Templates.
 */
package bp.util;

import java.util.*;

public class Util {

   /**
    * Replaces s1 with s2 in String s
    * @param s1 the substring whose occurrences are to be replaced
    * @param s2 the replacement
    * @param s the string in which the replacement takes place
    * @return
    */
    public static String stringReplace(String s1, String s2 , String s) {
       int p=0, p1=0, p2=0;
       while ( (p=s.indexOf(s1,p2)) !=-1) {
          p1=p + s1.length() ;
          p2=p + s2.length() ;
          s=s.substring( 0 , p ) + s2 + s.substring(p1 , s.length() );
       }
       return s;
    }

    /**
     * Replaces special html characters with their escape sequence
     * @param s the string to escape
     * @return the string ready to be html'ed
     */
     public static String escapeHtml(String s) {
        s = stringReplace("\\","\\\\",s);
        s = stringReplace("<","&lt;",s);
        s = stringReplace(">","&gt;",s);
        return s;
     }

	public static String getSortedByLine(StringBuffer datalogProgram) {
		List<String> list = new ArrayList<String>();
		Scanner sc = new Scanner(datalogProgram.toString());
		while (sc.hasNextLine()) {
			list.add(sc.nextLine());
		}
		Collections.sort(list);
		StringBuffer sb = new StringBuffer();
		for (String s : list) {
			sb.append(s + "\n");
		}
		return sb.toString();
	}
}
