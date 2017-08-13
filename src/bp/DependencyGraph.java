package bp;

import java.util.*;
import java.util.logging.Logger;

import bp.Attribute.AccessLimitation;

/*
 * + Read source schema, e.g.: sa(A);s1(A-b,B);s2(A,B-b);
 * + Read query, e.g.: q(X):-sa(Y),s1(Y,X).
 * - Check query/source compatibility
 * + Transform query+schema into a dependency graph
 * - Possibly represent it graphically
 * + Optimize the binding state and possibly represent it graphically
 * + Transform the optimized binding state into a Datalog query+program
 */

public class DependencyGraph {

	static final Logger logger = LoggerFactory.getLogger();

	Set<Node> nodes;
	Set<Arc> arcs;
	Set<Arc> candidates;
	Set<Arc> circular;
	Set<Source> sources;
	CQ cq;
	Set<Schema> schemata;
	Map<Schema, Constant> constantMap = null;
	private int nodeCounter;
	List<Atom> nonQueryAtoms;


	// this one is only for positive queries
//	public DependencyGraph(Query query, Set<Schema> schemata) {
//		this(query, query, schemata, new HashMap<Schema, Constant>());
//	}
//	public DependencyGraph(Query query, Set<Schema> schemata,
//			Map<Schema, Constant> constantMap) {
//		this(query, query, schemata, new HashMap<Schema, Constant>());
//	}

	public DependencyGraph(Query query, Set<Schema> schemata) {
		this(query, schemata, new HashMap<Schema, Constant>());
	}

	public DependencyGraph(Query query, Set<Schema> schemata,
			Map<Schema, Constant> constantMap) {
		this.constantMap = constantMap;
		nodes = new TreeSet<Node>();
		arcs = new TreeSet<Arc>();
		candidates = new TreeSet<Arc>();
		circular = new TreeSet<Arc>();
		sources = new TreeSet<Source>();
		// originalQuery = query;
		nodeCounter = 1;
		cq = query.getCQ();
		this.schemata = schemata;
		// cq.eliminateConstants(schema);
		addBlackNodes();
		addWhiteNodes();
		addArcs();
		addCircular();
	}

	// detects all circular candidate strong arcs, i.e., those arcs u->v
	// that are contained in a d-path u ->+ u' such that
	// 1) u and u' are in the same source and
	// 2) all arcs in the d-path are candidate strong arcs
	private void addCircular() {
		for (Arc arc : candidates) {
			if (candidateDPathExists(arc.getTo(), arc.getFrom())) {
//				System.out.println("Arc " + arc + " is circular");
				circular.add(arc);
			}
		}
	}

	private boolean candidateDPathExists(Node from, Node to) {
		return dpathExists(from, to, candidates);
	}

	private boolean dpathExists(Node from, Node to, Set<Arc> arcset) {
		Set<Arc> arcsetCopy = new TreeSet<Arc>(arcset);
		if (from.getSource() == to.getSource())
			return true; // base case: empty d-path
		for (Arc arc : arcset) {
			if (arc.getFrom().getSource() == from.getSource()) {
				arcsetCopy.remove(arc);
				if (dpathExists(arc.to, to, arcsetCopy)) {
					return true;
				}
				arcsetCopy.add(arc);
			}
		}
		return false;
	}

	// Adding arcs from free nodes to bound nodes with the same attribute
	// Candidate arcs are arcs between black nodes that are joined in the query
	private void addArcs() {
		for (Node from : nodes) {
			for (Node to : nodes) {
				Attribute fa = from.getAttribute();
				Attribute ta = to.getAttribute();
				if (fa.getName().equals(ta.getName()) && !fa.isBound()
						&& ta.isBound()) {
					Arc e = new Arc(from, to);
					arcs.add(e);
					if (from.isBlack() && to.isBlack()
							&& from.getVariable().equals(to.getVariable())) {
						candidates.add(e);
					}
				}
			}
		}
	}

	// Add white nodes from relations not used in the query
	private void addWhiteNodes() {
		nonQueryAtoms = new ArrayList<Atom>();
		for (Schema s : schemata) {
			if (!cq.uses(s)) {
				Term[] al = new Term[s.getArity()];
				for (int i = 0; i < s.getArity(); i++)
					al[i] = new Variable();
				Atom atom = new Atom(s.getName(), al, false);
				Source source = new Source();
				int termPosition = 0;
				for (Attribute a : s.getAttributes()) {
					Node n = new Node("" + nodeCounter++);
					n.setAtom(atom);
					n.setPosition(termPosition);
					n.setAttribute(a);
					n.setSchema(s);
					n.setSource(source);
					n.setVariable((Variable) al[termPosition++]);
					nodes.add(n);
					source.add(n);
				}
				atom.setSource(source);
				nonQueryAtoms.add(atom);
				sources.add(source);
			}
		}
	}

	// Adding black nodes from the query
	private void addBlackNodes() {
		for (Atom atom : cq.getBody()) {
			Source source = new Source();
			int termPosition = 0;
			for (Term t : atom.getArgumentList()) {
				Node n = new BlackNode("" + nodeCounter++);
				n.setAtom(atom);
				n.setPosition(termPosition);
				n.setAttribute(atom.getAttribute(schemata, termPosition++));
				n.setSchema(getSchema(atom));
				n.setSource(source);
				if (n.getAttribute() == null) {
					logger.warning("null attribute" + atom + " " + t);
				}
				n.setVariable((Variable) t);
				nodes.add(n);
				source.add(n);
			}
			atom.setSource(source);
			sources.add(source);
		}
	}

	public Marking calculateGFP() {
		Set<Arc> strongArcs = new HashSet<Arc>();
		Set<Arc> deletedArcs = new HashSet<Arc>();

		// mark as strong all candidate strong arcs
		// that are not circular
		for (Arc e : candidates) {
			if (!circular.contains(e)) {
				strongArcs.add(e);
				logger.fine("mark as strong all candidate strong arcs "
						+ "that are not circular: " + e);
			}
		}

		// mark as deleted all arcs that are not strong
		for (Arc e : arcs) {
			// CORRECTION: mark as deleted all arcs that are not CANDIDATE
			// strong
			// if (!strongArcs.contains(e)) {
			if (!candidates.contains(e)) {
				deletedArcs.add(e);
				logger.fine("mark as deleted all arcs that are not strong: "
						+ e);
			}
		}

		Marking m = new Marking(strongArcs, deletedArcs);
//		System.out.println("Initial marking: " + m);
		logger.fine("*** Initialization: " + m);

		Marking m2;
		int iteration = 1;
		do {
			m2 = new Marking(m);
			m.setStrongArcs(unmarkStrong(m2));
			m.setDeletedArcs(unmarkDeleted(m2));
			logger.fine("*** Iteration " + iteration++ + ":    " + m);
		} while (!m.equals(m2));

		return m;
	}

	private Set<Arc> unmarkStrong(Marking m) {
		Set<Arc> strongArcs = new HashSet<Arc>(m.getStrongArcs());
		Set<Arc> s = m.getStrongArcs();
		Set<Arc> d = m.getDeletedArcs();

		for (Arc e : s) {
			for (Arc f : outArcs(e.getTo())) {
				if (!s.contains(f) && !d.contains(f)) {
					logger.fine("I unmark strong arc " + e + " having outArc "
							+ f + " which is neither strong nor deleted");
					strongArcs.remove(e);
					break;
				}
			}
		}
		return strongArcs;
	}

	private Set<Arc> unmarkDeleted(Marking m) {
		Set<Arc> deletedArcs = new HashSet<Arc>(m.getDeletedArcs());
		Set<Arc> s = m.getStrongArcs();
		Set<Arc> d = m.getDeletedArcs();

		for (Arc e : d) {
			if (e.getTo().isBlack()) {
				boolean strongExists = false;
				for (Arc f : s) {
					if (f.getTo().equals(e.getTo())) {
						strongExists = true;
						break;
					}
				}
				if (!strongExists) {
					deletedArcs.remove(e);
					logger.fine("I unmark deleted arc "
							+ e
							+ " since is there no strong arc with same destination");
				}
			} else {
				Set<Arc> out = outArcs(e.getTo());
//				System.out.println("out:" + out);
				out.removeAll(d);
				if (!out.isEmpty()) {
					deletedArcs.remove(e);
					logger.fine("I unmark deleted arc " + e
							+ " having outArcs " + out + " not deleted");
				}
			}
		}
		return deletedArcs;
	}

	public Set<Arc> outArcs(Node n) {
		Source source = getNodeSource(n);
		Set<Arc> out = new HashSet<Arc>();
		if (source != null)
			for (Arc e : arcs)
				for (Node m : source)
					if (m.equals(e.getFrom())) {
						out.add(e);
						break;
					}
		return out;
	}

	// finds n's source
	private Source getNodeSource(Node n) {
		for (Source s : sources)
			for (Node m : s)
				if (m.equals(n))
					return s;
		return null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		List<Schema> sortedSchema = new ArrayList<Schema>(schemata);
		Collections.<Schema> sort(sortedSchema);
		sb.append("Schema: " + sortedSchema);
		sb.append("\nSchema/constant map: " + constantMap);
		sb.append("\nQuery: " + cq);
		List<Node> sortedNodes = new ArrayList<Node>(nodes);
		Collections.<Node> sort(sortedNodes);
		sb.append("\nNodes: " + sortedNodes);
		List<Arc> sortedArcs = new ArrayList<Arc>(arcs);
		Collections.<Arc> sort(sortedArcs);
		sb.append("\nArcs: " + sortedArcs);
		List<Arc> sortedCandidates = new ArrayList<Arc>(candidates);
		Collections.<Arc> sort(sortedCandidates);
		// sb.append("\nCandidates: " + sortedCandidates);
		sb.append("\nSources: " + sources);
		return sb.toString();
	}

	public CQ getCq() {
		return cq;
	}

	public void setCq(CQ modifiedQuery) {
		this.cq = modifiedQuery;
	}

	public void removeDeletedArcs(Marking m) {
		arcs.removeAll(m.deletedArcs);
	}

	// per uso solo per le CQ positive
	public String getDatalogProgram(Set<Arc> strongArcs) {
//		return getDatalogProgram(strongArcs, "");
		return getDatalogProgram(cq, strongArcs, "");
	}

	public String getDatalogProgram(CQ cqNeg, Set<Arc> strongArcs, String cqNumber) {
		StringBuffer sb = new StringBuffer();
		Map<String, Integer> counterMap = new HashMap<String, Integer>();
		Map<Variable, Constant> cMap = new TreeMap<Variable, Constant>();
		// query rule
		// if (originalQuery instanceof CQ)
		{ // CQ
			int i = 0;
			sb.append(" " + cq.getHead());
			sb.append(" :- ");
			for (Atom atom : cq.getBody()) {
				if (atom.getSchema().isFake()) {
					cMap.put((Variable) atom.argumentList[0],
							constantMap.get(atom.getSchema()));
					i++;
				} else {
					Integer number = counterMap.get(atom.getName());
					if (number == null)
						number = 0;
					counterMap.put(atom.getName(), ++number);
					atom.setOccurrenceNumber(number);
					// sb.append(l.getHattedName() + number +
					// l.getArgumentListString());
					sb.append(atom.getHattedNumberedName(cqNumber)
							+ atom.substitute(cMap).getArgumentListString());
					if (i++ < cq.getBody().length - 1)
						sb.append(", ");
//					else
//						sb.append(".\n");
				}
			}
			
			// here we add the negative literals
			for (Atom atom : cqNeg.getBody()) {
				sb.append(", ");
				sb.append(Atom.negString + atom.getName() + atom.substitute(cMap).getArgumentListString());				
			}
			sb.append(".\n");
		}

		// List of atoms in the body of the query
		List<Atom> atomList = new ArrayList<Atom>(Arrays.asList(cq.getBody()));
		// plus one atom for each white source with incoming or outgoing arcs
		for (Atom atom : nonQueryAtoms) {
			for (Arc arc : arcs) {
				if ((!arc.getTo().isBlack() && arc.getTo().getSchema().equals(
						atom.getSchema()))
						|| (!arc.getFrom().isBlack() && arc.getFrom().getSchema().equals(
								atom.getSchema()))) {
					atomList.add(atom);
					break;
				}
			}
		}

		// One cache rule for each negative literal
		// for (Atom atom : cqNeg.getBody()) {
		// // Rule head
		// sb.append(" " + atom.getHattedName() + "(");
		// String al = "";
		// int len = atom.getArgumentList().length;
		// for (int i = 0; i < len; i++) {
		// Variable v = new Variable();
		// al+=v;
		// if (i < len - 1)
		// al+=",";
		// }
		// sb.append(al);
		// sb.append(") :- ");
		// sb.append(atom.getName() + "(");
		// sb.append(al);
		//			sb.append(").\n");
		//		}
		
		
		// One rule for every literal in the body of the query
		// and for every white source with incoming or outgoing arcs
		for (Atom atom : atomList) {
			if (atom.getSchema().isFake())
				continue;
			// Variable.resetCounter();
			Map<Integer, Variable> variableMap = new HashMap<Integer, Variable>();

			// Rule head
			sb.append(" " + atom.getHattedNumberedName(cqNumber) + "(");
			// Modifica per il Carbotta
			atom.getSource().setPredicateName(
					atom.getHattedNumberedName(cqNumber));
			int len = atom.getArgumentList().length;
			for (int i = 0; i < len; i++) {
				Variable v = new Variable();
				variableMap.put(i, v);
				sb.append(v);
				if (i < len - 1)
					sb.append(",");
			}
			sb.append(") :- ");

			// Rule body: first literal (the actual source)
			sb.append(atom.getName() + "(");
			for (int i = 0; i < len; i++) {
				Variable v = variableMap.get(i);
				sb.append(v);
				if (i < len - 1)
					sb.append(",");
				else
					sb.append(")");
			}

			Set<Node> boundedNodes = getBoundedNodes(atom);
			logger.finer("boundednodes: " + boundedNodes);

			// Rule body: one literal per bounded node
			for (Node n : boundedNodes) {
				// sb.append(", n" + n.getId() + "(" +
				// variableMap.get(n.getPosition()) + ")");
				// sb.append(", var" + variableMap.get(n.getPosition()) + "(" +
				// variableMap.get(n.getPosition()) + ")");
				sb.append(", dom" + n.getAttribute().getName()
						+ variableMap.get(n.getPosition()) + "("
						+ variableMap.get(n.getPosition()) + ")");
			}
			sb.append(".\n");

			// Creating rules for bounded nodes
			for (Node n : boundedNodes) {
				boolean strongInited = false;
				for (Arc arc : arcs) {
					if (arc.getTo().equals(n)) {
						if (!strongArcs.contains(arc)) { // arcs are weak:
							// one new rule per
							// arc
							// sb.append(" n" + n.getId() + "(" +
							// variableMap.get(n.getPosition()) + ") :- ");
							// sb.append(" var" +
							// variableMap.get(n.getPosition()) +
							// "(" + variableMap.get(n.getPosition()) + ") :-
							// ");
							sb.append(" dom" + n.getAttribute().getName()
									+ variableMap.get(n.getPosition()) + "("
									+ variableMap.get(n.getPosition())
									+ ") :- ");
						} else { // arcs are strong: one rule with one
							// conjunct in the body per arc
							// with the first arc, we write the head
							// if (!strongInited) sb.append(" n" + n.getId() +
							// "(" + variableMap.get(n.getPosition()) + ") :-
							// ");
							// if (!strongInited) sb.append(" var" +
							// variableMap.get(n.getPosition()) + "(" +
							// variableMap.get(n.getPosition()) + ") :- ");
							if (!strongInited)
								sb.append(" dom" + n.getAttribute().getName()
										+ variableMap.get(n.getPosition())
										+ "("
										+ variableMap.get(n.getPosition())
										+ ") :- ");
							else
								sb.append(",");
							strongInited = true;
						}
						Atom fromAtom = arc.getFrom().getAtom();
						Schema fromSchema = arc.getFrom().getSchema();
						if (fromSchema.isFake()) {
							sb.append(variableMap.get(n.getPosition()) + "="
									+ constantMap.get(fromSchema));
						} else {
							String fromName;
							if (fromAtom == null) {
								fromName = fromSchema.getName();
							} else {
								fromName = fromAtom.getHattedNumberedName(cqNumber);
							}
							sb.append(fromName + "(");

							int fromLen = fromSchema.getArity();
							for (int i = 0; i < fromLen; i++) {
								// System.out.println(l + ", " + fromLit + ", "
								// + a + " " + fromLit.getSource().get(i));
								if (arc.getFrom().equals(
										arc.getFrom().getSource().get(i))) {
									Variable v = variableMap.get(n.getPosition());
									Constant c = cMap.get(v);
									if (c == null)
										sb.append(v);
									else
										sb.append(c);
								} else {
									// sb.append(new Variable());
									sb.append("_");
								}
								if (i < fromLen - 1)
									sb.append(",");
								else
									sb.append(")");

							}

						}
						if (!strongInited)
							sb.append(".\n");
					}
				}
				if (strongInited)
					sb.append(".\n");
			}
		}

		// // One rule for each added source
		// for (Schema s : constantMap.keySet()) {
		// sb.append(" " + s.getName() + "(" + constantMap.get(s) + ").\n");
		// }

		return sb.toString();
	}

	public boolean isQueryable(Source s, Set<Node> reachedNodes) {
		for (Node n : s) {
			if (n.getAttribute().getAccessLimitation().equals(
					AccessLimitation.BOUND)
					&& !reachedNodes.contains(n))
				return false;
		}
		return true;
	}

	public boolean isAnswerable() {
		return firstNotAnswerableNode() == null;
	}

	public Node firstNotAnswerableNode() {
		Set<Node> initialNodes = new TreeSet<Node>();
		Set<Node> augmentedNodes = new TreeSet<Node>();
		Set<Node> totalNodesBefore = new TreeSet<Node>();
		Set<Node> totalNodesAfter = new TreeSet<Node>();
		int iter = 0;
		do {
			totalNodesBefore.addAll(totalNodesAfter);
			for (Source s : sources) {
				if (isQueryable(s, initialNodes)) {
					// System.out.println(s + " is queryable");
					initialNodes.addAll(s);
				}
			}
			// System.out.println("Nodes: " + nodes);
			// System.out.println("Nodes in free sources: " + initialNodes);
			for (Arc arc : arcs) {
				if (initialNodes.contains(arc.getFrom())) {
					augmentedNodes.add(arc.getTo());
				}
			}
			totalNodesAfter.addAll(augmentedNodes);
			totalNodesAfter.addAll(initialNodes);
			initialNodes.addAll(augmentedNodes);
		} while (!totalNodesAfter.equals(totalNodesBefore) && iter < 100);

		for (Node n : nodes) {
			if (n.isBlack() && !totalNodesAfter.contains(n)) {
				// System.out.println("node " + n + " is not reachable");
				return n;
			}
		}
		return null;
	}

	private Set<Node> getBoundedNodes(Atom l) {
		Schema s = getSchema(l);
		Set<Node> boundedNodes = new HashSet<Node>();
		int position = 0;
		for (Attribute a : s.getAttributes()) {
			if (a.isBound()) {
				boundedNodes.add(findNode(s, position, l));
			}
			position++;
		}
		return boundedNodes;
	}

	private Node findNode(Schema s, int position, Atom l) {
		for (Node n : nodes) {
			if (n.getSchema().equals(s)
					&& n.getAttribute().equals(s.getAttributes()[position])
					&& n.getPosition() == position && n.getAtom() == l)
				return n;
		}
		return null;
	}

	public Schema getSchema(Atom l) {
		for (Schema s : schemata) {
			if (s.getName().equals(l.getName())) {
				return s;
			}
		}
		return null;
	}

	public Set<Arc> getArcs() {
		return arcs;
	}

	public Set<Source> getSources() {
		return sources;
	}

}
