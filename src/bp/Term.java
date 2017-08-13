package bp;

public abstract class Term implements Comparable<Object> {
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Term(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        Term term = (Term) o;
        return toString().compareTo(term.toString());
    }

	@Override
	public boolean equals(Object obj) {
		return name.equals(obj.toString());
	}


}
