package bp;

import java.util.Map;
import java.util.TreeMap;

public class Node implements Comparable<Node> {
		
	private String id;
	private Color color;
	
	private Variable variable;
	private Schema schema;
	private Source source;
	private Attribute attribute;
	private int position;
	private Atom atom;
	private CQ cq;
	
	private boolean extensional;
	private Node parent;

	private static Map<String,Integer> counterMap = new TreeMap<String,Integer>();
	
	enum Color {
		BLACK, WHITE
	}	
	
	public Node(String id) {
		this.id = id;
		this.color = Color.WHITE;
		this.parent = this;
	}

	public Node(String id, Color color) {
		this.id = id;
		this.color = color;
		this.parent = this;
	}

	public boolean isBlack() {
		return color == Color.BLACK;
	}

	public boolean equals(Node n) {
		return id.equals(n.id);
	}

	public String toString() {
		return id;
//		if (attribute!=null)
//			return id + (attribute.isBound() ? "b" : "f") + (isBlack() ? "B" : "W") + "(" + atom.getName() + ":" + (position+1) +")";
//		return id + "(" + atom.getName() + ":" + (position+1) +")";
	}

	public int compareTo(Node o) {
        return toString().compareTo(o.toString());
    }

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	public Variable getVariable() {
		return variable;
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema source) {
		this.schema = source;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setAtom(Atom atom) {
		this.atom = atom;		
	}

	public Atom getAtom() {
		return atom;
	}

	public String getId() {
		return id;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public CQ getCq() {
		return cq;
	}

	public void setCq(CQ cq) {
		this.cq = cq;
	}

	public boolean isExtensional() {
		return extensional;
	}

	public void setExtensional(boolean extensional) {
		this.extensional = extensional;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getReplica() {
		Integer number = counterMap.get(id);
		if (number == null) {
			number = 0;
		}
		counterMap.put(id, ++number);
		Node node = new Node(id + "." + number);
		node.color = color;
		node.variable = variable;
		node.schema = schema;
		node.source = source;
		node.attribute = attribute;
		node.position = position;
		node.atom = atom;
		node.cq = cq;
		node.extensional = extensional;
		node.parent = parent;
		return node;
	}
	
	public static void resetCounter() {
		counterMap = new TreeMap<String,Integer>();
	}

}
