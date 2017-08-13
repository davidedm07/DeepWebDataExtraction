package bp;

public class NewSchema extends Schema {

	public NewSchema(String name, Attribute[] attributes) {
		super(name, attributes);
		fake = true;
	}
}
