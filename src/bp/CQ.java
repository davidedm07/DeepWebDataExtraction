package bp;

import java.util.*;
import java.util.logging.Logger;

import bp.Attribute.AccessLimitation;

public class CQ extends Query {
	static final Logger logger = LoggerFactory.getLogger();
	Atom head;
	Atom[] body;
	int sourceCounter = 1;

	String getName() {
		return head.getName();
	}

	public CQ(Atom head, Atom[] body) {
		this.head = head;
		this.body = body;
	}

	public CQ(CQ q) {
		this.head = q.head;
		this.body = q.body;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(getName() + "(");
		Variable[] vars = head.getVariables();
		int i = 0;
		for (Variable v : vars) {
			sb.append(v);
			if (i++ < vars.length - 1) {
				sb.append(",");
			}
		}
		sb.append(") :- ");
		i = 0;
		for (Atom l : body) {
			sb.append(l);
			if (i++ < body.length - 1)
				sb.append(", ");
		}
		return sb.toString() + ".";
	}

	public Atom[] getBody() {
		return body;
	}

	public void setBody(Atom[] body) {
		this.body = body;
	}

	public Atom getHead() {
		return head;
	}

	public void setHead(Atom head) {
		this.head = head;
	}

	public boolean uses(Schema s) {
		for (Atom l : body) {
			if (l.getName().equals(s.getName())
					&& l.argumentList.length == s.getAttributes().length)
				return true;
		}
		return false;
	}

	public CQ standardizedApart() {
		Map<Variable, Variable> variableMap = new TreeMap<Variable, Variable>();
		Variable[] vars = head.getVariables();
		for (Variable v : vars) {
			if (variableMap.get(v) == null)
				variableMap.put(v, new Variable());
		}

		Term[] saArgumentList = new Term[head.argumentList.length];
		for (int i = 0; i < head.argumentList.length; i++) {
			if (head.argumentList[i] instanceof Variable) {
				saArgumentList[i] = variableMap.get(head.argumentList[i]);
			} else {
				saArgumentList[i] = head.argumentList[i];
			}
		}
		Atom saHead = new Atom(head.getName(), saArgumentList, false);

		Atom[] saBody = new Atom[body.length];
		for (int j = 0; j < body.length; j++) {
			Atom l = body[j];
			vars = l.getVariables();
			for (Variable v : vars) {
				if (variableMap.get(v) == null)
					variableMap.put(v, new Variable());
			}
			saArgumentList = new Term[l.argumentList.length];
			for (int i = 0; i < l.argumentList.length; i++) {
				if (l.argumentList[i] instanceof Variable) {
					saArgumentList[i] = variableMap.get(l.argumentList[i]);
				} else {
					saArgumentList[i] = l.argumentList[i];
				}
			}
			saBody[j] = new Atom(l.getName(), saArgumentList, l.isNegative);
		}
		return new CQ(saHead, saBody);
	}

	public int getArity() {
		return head.argumentList.length;
	}

	@Override
	public CQ getCQ() {
		return this;
	}

	public Map<Constant, Schema> eliminateConstants(Set<Schema> schemata) {
		return eliminateConstants(schemata, new TreeMap<Constant, Schema>());
	}

	public Map<Constant, Schema> eliminateConstants(Set<Schema> schemata,
			Map<Constant, Schema> constantMap) {
		if (constantMap == null)
			constantMap = new TreeMap<Constant, Schema>();
		// Variable.resetCounter();

		// Adding one new source Sa for each constant "a" in the query
		// and a conjunct "Sa(Xa)" to the query, and replacing "a" with "Xa" in
		// the query
		List<Atom> bodyList = new ArrayList<Atom>();
		Map<Constant, Variable> cvMap = new TreeMap<Constant, Variable>();
		for (Atom atom : body) {
			List<Term> termList = new ArrayList<Term>();
			int termPosition = 0;
			for (Term t : atom.getArgumentList()) {
				if (t instanceof Constant) {
					Variable v = cvMap.get(t);
					String sourceName;
					if (v == null) {
						v = new Variable();
						if (!constantMap.containsKey(t)) {
							sourceName = "newRel" + sourceCounter++;
							Attribute a = new Attribute(atom.getAttribute(
									schemata, termPosition).getName(),
									AccessLimitation.FREE);
							Schema s = new NewSchema(sourceName,
									new Attribute[] { a });
							schemata.add(s);
							constantMap.put((Constant) t, s);
						} else {
							sourceName = constantMap.get(t).getName();
						}
						cvMap.put((Constant) t, v);
						bodyList.add(new Atom(sourceName, new Term[] { v } ,false));
					} else {
						sourceName = constantMap.get(t).getName();
					}
					termList.add(v);
				} else {
					termList.add(t);
				}
				termPosition++;
			}
			bodyList.add(new Atom(atom.getName(),
					(Term[]) termList.toArray(new Term[termList.size()]), atom.isNegative));
		}
		body = (Atom[]) bodyList.toArray(new Atom[bodyList.size()]);
		logger.info("Rewritten query: " + toString());
		logger.info("Rewritten schema: " + schemata);
		return constantMap;
	}

	public List<Atom> getHeadAndBody() {
		List<Atom> list = new ArrayList<Atom>();
		list.add(head);
		list.addAll(Arrays.asList(body));
		return list;
	}

	public CQ removeEqualities() {
		List<Atom> bodyList = new ArrayList<Atom>(Arrays.asList(body));
		bodyList = reduce(bodyList);
		return new CQ(head,
				(Atom[]) bodyList.toArray(new Atom[bodyList.size()]));
	}

	private List<Atom> reduce(List<Atom> bodyList) {
		List<Atom> bodyList2 = new ArrayList<Atom>(bodyList);
		do {
			bodyList = bodyList2;
			System.out.println("prima " + bodyList);
			bodyList2 = reduce2(bodyList);
			System.out.println("dopo " + bodyList2);
		} while (!bodyList.equals(bodyList2));
		return bodyList;
	}

	private List<Atom> reduce2(List<Atom> bodyList) {
		List<Atom> bodyList2 = new ArrayList<Atom>(bodyList);
		for (Atom atom : bodyList2) {
			if (atom.getName().equals("=") && atom.getArity() == 2) {
				Term from = atom.getArgumentList()[0];
				Term to = atom.getArgumentList()[1];
				if (from instanceof Variable) {
					bodyList.remove(bodyList.indexOf(atom));
					// bodyList.remove(atom);
					return replaceVariable((Variable) from, to, bodyList);
				} else if (to instanceof Variable) {
					bodyList.remove(bodyList.indexOf(atom));
					// bodyList.remove(atom);
					return replaceVariable((Variable) to, from, bodyList);
				}
			}
		}
		return bodyList;
	}

	public List<Atom> replaceVariable(Variable from, Term to,
			List<Atom> bodyList) {
		List<Atom> bodyList2 = new ArrayList<Atom>();
		for (Atom atom : bodyList) {
			Term[] newAL = new Term[atom.argumentList.length];
			for (int i = 0; i < newAL.length; i++) {
				if (atom.argumentList[i].equals(from)) {
					newAL[i] = to;
				} else
					newAL[i] = atom.argumentList[i];
			}
			bodyList2.add(new Atom(atom.name, newAL, atom.isNegative));
		}
		return bodyList2;
	}

	// returns the positive part of a CQnot
	public CQ getPositive() {
		List<Atom> bodyList = new ArrayList<Atom>();
		for (Atom l : body) {
			if (l.isNegative) continue;
			bodyList.add(l);
		}
		return new CQ(head,	(Atom[]) bodyList.toArray(new Atom[bodyList.size()]));
	}

	// returns the negative part of a CQnot
	public CQ getNegative() {
		List<Atom> bodyList = new ArrayList<Atom>();
		for (Atom l : body) {
			if (!l.isNegative) continue;
			bodyList.add(l);
		}
		return new CQ(head,	(Atom[]) bodyList.toArray(new Atom[bodyList.size()]));
	}
}
