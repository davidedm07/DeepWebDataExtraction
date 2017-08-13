package bp;


public class Schema implements Comparable<Schema> {

	private String name;
	private Attribute[] attributes;
	protected boolean fake = false;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Schema(String name, Attribute[] attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(name + "(");
		int i = 0;
		for (Attribute a : attributes) {
			sb.append(a);
			if (i++ < attributes.length - 1) sb.append(",");
		}
		return sb.toString() + ")";
	}

	public int compareTo(Schema s) {
		return toString().compareTo(s.toString());
	}

	public boolean equals(Schema source) {
		//Source source = (Source) o;
		if (!name.equals(source.name)) return false;
		if (attributes.length != source.attributes.length) return false;
		for (int i = 0; i < attributes.length; i++) {
			if (!attributes[i].equals(source.attributes[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return toString().hashCode();
	}

	public int getArity() {
		return attributes.length;
	}
	
	public boolean isFake() {
		return fake;
	}
}
