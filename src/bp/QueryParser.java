package bp;

import java.util.*;

import bp.util.Util;

public class QueryParser {

	public UCQ parseUoCQ(String s) throws ParsingException {
		// remove useless characters
		s = Util.stringReplace("\n", "", s);
		s = Util.stringReplace("\r", "", s);
		s = Util.stringReplace(" ", "", s);
		s = Util.stringReplace("\t", "", s);
		List<CQ> queryList = new ArrayList<CQ>();
		int nextDot;
		while ((nextDot = s.indexOf('.')) >= 0) {
			CQ cq = parseCQ(s.substring(0,nextDot + 1));
			queryList.add(cq);
			s = s.substring(nextDot + 1);
		}
		if (!s.equals("")) throw new ParsingException("Query set ended incorrectly.");
		return new UCQ((CQ[]) queryList.toArray(new CQ[queryList.size()]));
	}

	public CQ parseCQ(String s) throws ParsingException {
		// remove useless characters
		s = Util.stringReplace("\n", "", s);
		s = Util.stringReplace("\r", "", s);
		s = Util.stringReplace(" ", "", s);
		s = Util.stringReplace("\t", "", s);
		if (s.charAt(s.length()-1) != '.') throw new ParsingException("Query must end with .");
		s = s.substring(0, s.length()-1);
		int nextArrow = s.indexOf(":-");
		if (nextArrow < 0) throw new ParsingException("Head and body must be separated by =");
		Atom head = parseHead(s.substring(0, nextArrow));
		if (head.getArity() != head.getVariables().length) throw new ParsingException("The arguments of a query head must be all different variables");
		Atom[] body = parseBody(s.substring(nextArrow + 2));
		return new CQ(head, body);
	}

	public Atom parseHead(String s) throws ParsingException {
		int nextOpenParenthesis = s.indexOf('(');
		Atom l;
		if (nextOpenParenthesis > -1) {
			l = parseLiteral(s.substring(0, nextOpenParenthesis));
			l.setArgumentList(parseArgumentList(s.substring(nextOpenParenthesis + 1,s.length()-1)));
		} else {
			l = parseLiteral(s);
			l.setArgumentList(new Term[0]);
		}
		return l;
	}

	public Atom[] parseBody(String s) throws ParsingException {
		List<Atom> literals = new ArrayList<Atom>();
		while (!s.equals("")) {
			int nextOpenParenthesis = s.indexOf('(');
			int nextComma = s.indexOf(',');
			int end = s.length();
			nextOpenParenthesis = nextOpenParenthesis < 0 ? end + 1 : nextOpenParenthesis;
			nextComma = nextComma < 0 ? end + 1 : nextComma;
			String predicateName = null;
			Term[] argumentList = null;
			int[] indexes = new int[]{nextComma, nextOpenParenthesis, end};
			Arrays.sort(indexes);
			int min = indexes[0];
			
			if (nextOpenParenthesis == min) {
				predicateName = s.substring(0, nextOpenParenthesis);
				if (predicateName.equals("")) throw new ParsingException("Predicate names cannot be empty");
				int nextClosedParenthesis = s.indexOf(')');
				if (nextClosedParenthesis == -1) throw new ParsingException("missing )");
				if (nextClosedParenthesis < nextOpenParenthesis) throw new ParsingException(") found before (");
				argumentList = parseArgumentList(s.substring(nextOpenParenthesis + 1, nextClosedParenthesis));
				s = s.substring(nextClosedParenthesis + 1);
				if (s.length() > 0) {
					if (s.charAt(0) != ',')
						throw new ParsingException("Literals must be separated by commas");
					else
						s = s.substring(1);
				}

			} else if (nextComma == min) {
				predicateName = s.substring(0, nextComma);
				argumentList = new Term[0];
				s = s.substring(nextComma + 1);

			} else {
				// we reached the end
				predicateName = s;
				argumentList = new Term[0];
				s = "";
			}
			Atom literal = parseLiteral(predicateName);
			literal.setArgumentList(argumentList);
			literals.add(literal);
		}
		return (Atom[]) literals.toArray(new Atom[literals.size()]);
	}

	public Term[] parseArgumentList(String s) throws ParsingException {
		if (s.equals("")) return new Term[0];
		List<Term> argumentList = new ArrayList<Term>();
		boolean finished = false;
		while (!finished) {
			String termString = null;
			int nextComma = s.indexOf(',');
			if (nextComma > -1) {
				termString = s.substring(0, nextComma);
				s = s.substring(nextComma + 1);
			} else {
				termString = s;
				finished = true;
			}
			argumentList.add(parseTerm(termString));
		}

		return (Term[]) argumentList.toArray(new Term[argumentList.size()]);
	}

	public Term parseTerm(String s) throws ParsingException {
		if (s.equals("")) throw new ParsingException("Terms must not be empty");

		if (s.matches("[a-z]\\w*")) {
			return new Constant(s);
		} else if (s.matches("[A-Z]\\w*")) {
			return new Variable(s);
		} else if (s.equals("_")) {
			return new Variable();
		} else {
			throw new ParsingException("Wrong term name: " + s);
		}
	}

	public Atom parseLiteral(String s) throws ParsingException {
		boolean isNegative = false;
		if (s.startsWith(Atom.negString)/* && !s.startsWith("~=")*/) {
		isNegative = true;
		s = s.substring(1);
		}
		
		if (s.equals("")) throw new ParsingException("Predicate names cannot be empty");
		if (!s.matches("[a-z]\\w*")) { // && !s.equals("=") && !s.equals("~=")) {
			throw new ParsingException("Wrong predicate name: " + s);
		}
		return new Atom(s, null, isNegative);
//		return new Atom(s, null);
	}

}
