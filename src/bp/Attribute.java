package bp;

public class Attribute {
	
	AccessLimitation accessLimitation;
	String name;
	
	public enum AccessLimitation {
		BOUND, FREE
	}

	public Attribute(Attribute a) {
		this.name = a.name;
		this.accessLimitation = a.accessLimitation;
	}

	public Attribute(String name,AccessLimitation accessLimitation) {
		this.name = name;
		this.accessLimitation = accessLimitation;
	}

	public AccessLimitation getAccessLimitation() {
		return accessLimitation;
	}

	public void setAccessLimitation(AccessLimitation accessLimitation) {
		this.accessLimitation = accessLimitation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isBound() {
		return accessLimitation == AccessLimitation.BOUND;
	}

	public String toString() {
		return name + "/" + (accessLimitation == AccessLimitation.FREE ? "f" : "b");
	}

	public boolean equals(Object o) {
		Attribute a = (Attribute) o;
		return a.name.equals(name) && a.accessLimitation == accessLimitation;
	}

}
