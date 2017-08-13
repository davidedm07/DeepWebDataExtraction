package bp;

import java.util.ArrayList;

public class Source extends ArrayList<Node> implements Comparable<Source> {

	private int id;
	private String predicateName;
	static int counter = 0;

	public Source() {
		super();
		id = ++counter;
	}

	public Source(Source source) {
		super(source);
		id = ++counter;
	}

	public int compareTo(Source source) {
		return toString().compareTo(source.toString());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append((id < 10 ? "0" + id : id) + "{");
		boolean first = true;
		for (Node n : this) {
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(n);
		}
		sb.append("}");
		return sb.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static void resetCounter() {
		counter = 0;
	}

	public String getPredicateName() {
		return predicateName;
	}

	public void setPredicateName(String predicateName) {
		this.predicateName = predicateName;
	}
}