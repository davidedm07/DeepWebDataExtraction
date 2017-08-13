package bp;



import java.util.*;

import bp.Attribute.AccessLimitation;
import bp.util.Util;

public class SchemaParser {

	public Set<Schema> parseSources(String s) throws ParsingException {
		// remove useless characters
		s = Util.stringReplace("\n", "", s);
		s = Util.stringReplace("\r", "", s);
		s = Util.stringReplace(" ", "", s);
		s = Util.stringReplace("\t", "", s);
		Set<Schema> sources = new HashSet<Schema>();
		while (!s.equals("")) {
			String sourceString = null;
			int nextDot = s.indexOf(';');
			if (nextDot > -1) {
				sourceString = s.substring(0, nextDot);
				s = s.substring(nextDot + 1);
			} else {
				sourceString = s;
				s = "";
			}
			sources.add(parseSource(sourceString));
		}
		return sources;
	}

	public Schema parseSource(String s) throws ParsingException {
		int nextOpenParenthesis = s.indexOf('(');
		int nextComma = s.indexOf(',');
		int nextDash = s.indexOf('-');
		int end = s.length();
		nextOpenParenthesis = nextOpenParenthesis < 0 ? end + 1 : nextOpenParenthesis;
		nextComma = nextComma < 0 ? end + 1 : nextComma;
		nextDash = nextDash < 0 ? end + 1 : nextDash;

		String name = null;
		Attribute[] attributes = null;
		int[] indexes = new int[]{nextComma, nextDash, nextOpenParenthesis, end};
		Arrays.sort(indexes);
		int min = indexes[0];

		if (nextOpenParenthesis == min) {
			name = s.substring(0, nextOpenParenthesis);
			if (name.equals("")) throw new ParsingException("Source names cannot be empty");
			int nextClosedParenthesis = s.indexOf(')');
			if (nextClosedParenthesis == -1) throw new ParsingException("missing )");
			if (nextClosedParenthesis < nextOpenParenthesis) throw new ParsingException(") found before (");
			attributes = parseAttributes(s.substring(nextOpenParenthesis + 1, nextClosedParenthesis));
			s = s.substring(nextClosedParenthesis + 1);
			if (s.length() > 0) {
				throw new ParsingException("Ill-formed source declaration");
			}

		} else {
			// we reached the end
			throw new ParsingException("Ill-formed source declaration");
		}
		return new Schema(name, attributes);
	}

	public Attribute[] parseAttributes(String s) throws ParsingException {
		if (s.equals("")) throw new ParsingException("Source must have at least one attribute");
		List<Attribute> attributes = new ArrayList<Attribute>();
		boolean finished = false;
		while (!finished) {
			String attString = null;
			int nextComma = s.indexOf(',');
			if (nextComma > -1) {
				attString = s.substring(0, nextComma);
				s = s.substring(nextComma + 1);
			} else {
				attString = s;
				finished = true;
			}
			attributes.add(parseAttribute(attString));
		}
		return (Attribute[]) attributes.toArray(new Attribute[attributes.size()]);
	}

	public Attribute parseAttribute(String s) throws ParsingException {
		String attributeName;
		if (s.equals("")) throw new ParsingException("Attributes must not be empty");

		int nextDash = s.indexOf('-');
		if (nextDash > -1) {
			attributeName = s.substring(0, nextDash);
			s = s.substring(nextDash + 1);
			if (s.length() != 1) {
				throw new ParsingException("Malformed access limitation " + s + " for attribute " + attributeName);
			} else {
				char al = s.toUpperCase().charAt(0);
				if (al != 'B' && al != 'F') throw new ParsingException("Malformed access limitation " + s + " for attribute " + attributeName);
				return new Attribute(attributeName, al == 'B' ? AccessLimitation.BOUND : AccessLimitation.FREE);
			}
		} else {
			return new Attribute(s, AccessLimitation.FREE);			
		}
	}
}
