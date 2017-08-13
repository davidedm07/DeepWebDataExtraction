package bp;

import java.util.*;

import bp.util.Util;

import junit.framework.TestCase;

public class BindingPatterns extends TestCase {

	public static void main(String[] argv) {
		// Esempio 6 di Li&Chang
		// String sourcesString = "s1(A-b,B,C);s2(C-b,D,E);s3(E-b,F,A);s4(E,G)";
		// String queryString = "q(A,C,E):-s1(A,b0,C),s2(C,D,E),s3(E,F,A).";
		// genera Datalog ricorsivo!!!

		// String sourcesString = "s1(A-b,B);s2(A,B-b)";
		// String queryString = "q(X):-s1(a,X).";

		// Esempio di strong che diventa weak
		// String sourcesString = "s1(A-b,B);s2(A,B-b)";
		// String queryString = "q(X,Y):-s1(a,X),s2(A,Y).";

		// String sourcesString = "s1(A-b,B);s2(A,B-b)";
		// String queryString = "q(X):-s1(X,Y),s2(X,Y).";

		String sourcesString = "s1(A-b,B);s2(A,B-b)";
		String queryString = "q(X):-s1(a,X),s1(b,X).";

		// String sourcesString = "s1(A-b);s2(A);s3(A)";
		// String queryString = "q(X):-s1(X).";

		// String sourcesString =
		// "s1(A-b,C);s2(A,B,C-b);s3(C-b,D);s4(C,E);s5(E-b,F)";
		// String queryString = "q(D):-s1(a0,C),s3(C,D)." +
		// "q(D):-s2(a0,B,C),s3(C,D).";

		(new BindingPatterns()).findOptimizedQueryPlan("Main test",
				sourcesString, queryString);
	}

	public void findOptimizedQueryPlan(String name, String sourcesString,
			String queryString) {
		System.out.println("*** " + name + " ***");
		Variable.resetCounter();
		SchemaParser sourceParser = new SchemaParser();
		Set<Schema> schemata = null;
		try {
			schemata = sourceParser.parseSources(sourcesString);
			System.out.println(schemata);
		} catch (ParsingException e) {
			fail(e.getMessage());
		}

		QueryParser queryParser = new QueryParser();
		UCQ query = null;
		try {
			query = queryParser.parseUoCQ(queryString);
			System.out.println(query);
		} catch (ParsingException e) {
			fail(e.getMessage());
		}

		Map<Constant, Schema> constantMap = new TreeMap<Constant, Schema>();
		for (CQ cq : query.getCQs()) {
			constantMap = cq.eliminateConstants(schemata, constantMap);
			System.out.println(constantMap);
			System.out.println(schemata);
		}
		Map<Schema, Constant> invertedConstantMap = new TreeMap<Schema, Constant>();
		for (Constant c : constantMap.keySet()) {
			invertedConstantMap.put(constantMap.get(c), c);
		}
		StringBuffer datalogProgram = new StringBuffer();
		int i = 1;
		for (CQ cq : query.getCQs()) {
			CQ cqPos = cq.getPositive();
			CQ cqNeg = cq.getNegative();
			System.out.println("positive subquery: " + cqPos);
			System.out.println("negative subquery: " + cqNeg);
			DependencyGraph graph = new DependencyGraph(cqPos, schemata,
					invertedConstantMap);
			System.out.println(graph);
			Marking m = graph.calculateGFP();

			graph.removeDeletedArcs(m);
			System.out.println("Final marking: " + m);
			Set<Arc> weakArcs = new HashSet<Arc>(graph.getArcs());
			weakArcs.removeAll(m.getStrongArcs());
			System.out.println("Final weak arcs: " + weakArcs);
			String datalog = graph.getDatalogProgram(cqNeg, m.getStrongArcs(), "" + i++);
			datalogProgram.append(datalog);
			// System.out.println("* Partial Datalog:\n" + datalog);
		}
		System.out.println("Datalog program:\n"
				+ Util.getSortedByLine(datalogProgram));

	}

}
