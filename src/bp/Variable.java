package bp;

public class Variable extends Term {

	private static long counter = 0;

	public static void resetCounter() {
		counter = 0;
	}
	
	public Variable(String name) {
		super(name);
	}

	public Variable() {
		super("X_" + ++counter);
	}

	public String toString() {
		return name;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Variable)) return false;
		Variable variable = (Variable)o;
		return name.equals(variable.getName());
	}

//	public int hashCode() {
//		// TODO Auto-generated method stub
//		return toString().hashCode();
//	}

}
