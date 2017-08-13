package bp;
import java.util.*;

public class MultiNode extends TreeSet<Node> implements Comparable<MultiNode> {

	public int compareTo(MultiNode mn) {
		return toString().compareTo(mn.toString());
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		for (Node n : this) {
			if (!first) sb.append(", ");
			first = false;
			sb.append(n);
		}
		sb.append("}");
		return sb.toString();
	}
}
