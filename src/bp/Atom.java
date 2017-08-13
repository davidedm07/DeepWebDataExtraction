package bp;

import java.util.*;

public class Atom implements Comparable<Object> {
	public final static String negString = "~"; 
    protected String name;
    protected Term[] argumentList;
    protected boolean isNegative;
    private int occurrenceNumber;

	private Source source;

    public Variable[] getVariables() {
    	List<Variable> vars = new ArrayList<Variable>();
    	for (Term t : argumentList) {
    		if (t instanceof Variable) {
    			vars.add((Variable)t);
    		}
    	}
        return (Variable[])vars.toArray(new Variable[vars.size()]);
    }

    public Term[] getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(Term[] argumentList) {
        this.argumentList = argumentList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArity() {
        return argumentList.length;
    }

    /**
     * Constructs a Literal with the given name, argumentList and negative flag
     * @param name the name
     * @param argumentList the argument list
     * @param isNegative true if it is negated, false otherwise
     */
    public Atom(String name, Term[] argumentList, boolean isNegative) {
        this.name = name;
        this.argumentList = argumentList;
        this.isNegative = isNegative;
    }

    public String toString() {
		return (isNegative ? negString : "") + name + getArgumentListString();
    }

    public String getArgumentListString() {
		StringBuffer sb = new StringBuffer("(");
		int i = 0;
		for (Term t : argumentList) {
			sb.append(t);
			if (i++ < argumentList.length - 1) sb.append(",");
		}
		return sb + ")";    	   	
    }
    
    public boolean equals(Object o) {
        Atom literal = (Atom) o;
        if (!name.equals(literal.getName())) return false;
        if (argumentList.length != literal.argumentList.length) return false;
        if (name.equals("=") && argumentList.length == 2) {
            return (argumentList[0].equals(literal.argumentList[0]) && argumentList[1].equals(literal.argumentList[1]))
                    ||
                    (argumentList[0].equals(literal.argumentList[1]) && argumentList[1].equals(literal.argumentList[0]));
        }
        for (int i = 0; i < argumentList.length; i++) {
            if (!argumentList[i].equals(literal.argumentList[i])) return false;
        }
        return true;
    }

    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

	public String getHattedName() {
		return name + "^";
	}

	public String getHattedNumberedName(String n) {
		//return name + "^" + (occurrenceNumber == 0 ? "" : occurrenceNumber + "_" + n);
		return name + "_" + n + "^" + occurrenceNumber;
	}

	public int getOccurrenceNumber() {
		return occurrenceNumber;
	}

	public void setOccurrenceNumber(int occurrenceNumber) {
		this.occurrenceNumber = occurrenceNumber;
	}

	public void setSource(Source source) {
		this.source = source;		
	}

	public Source getSource() {
		return source;
	}

	public Schema getSchema() {
		if (source.size() == 0) return null;
		return source.get(0).getSchema();
	}
	
	public Atom substitute(Map<Variable,Constant> cMap) {
		Term[] newAL = new Term[argumentList.length];
		for (int i = 0 ; i < newAL.length ; i++) {
			Constant c = cMap.get(argumentList[i]);
			if (c==null) newAL[i] = argumentList[i];
			else newAL[i] = c;
		}
		return new Atom(name, newAL, isNegative);
	}
	
	public Attribute getAttribute(Set<Schema> schemata, int position) {
		for (Schema s : schemata) {
			if (s.getName().equals(name)) {
				return s.getAttributes()[position];
			}
		}
		return null;
	}

}
