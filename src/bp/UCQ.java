package bp;

import java.util.*;

public class UCQ extends Query {
	private CQ[] cqs;
	
	public UCQ(CQ[] cqs) {
		this.cqs = cqs;
	}
	
	public String getName() {
		if (cqs.length > 0)
			return cqs[0].getName();
		return "";
	}

	public int getArity() {
		if (cqs.length > 0)
			return cqs[0].getArity();
		return 0;
	}

	public CQ getCQ(int i) {
		return cqs[i];
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (CQ q : cqs) {
			if (!first) sb.append("\n"); 
			sb.append(q);
			first = false;
		}
		return sb.toString();
	}
	
	public CQ getTotalCQ() {
		List<Term> headList = new ArrayList<Term>();
		List<Atom> bodyList = new ArrayList<Atom>();
		for (CQ q : cqs) {
			CQ sacq = q.standardizedApart();
			Collections.addAll(headList, sacq.getHead().getArgumentList());
			Collections.addAll(bodyList, sacq.getBody());
		}
		Atom head = new Atom(getName(), headList.toArray(new Term[headList.size()]), false);
		Atom[] body = bodyList.toArray(new Atom[bodyList.size()]);
		return new CQ(head,body);
	}

	
	public int[] getNumBodyAtoms() {
		int[] numBodyAtoms = new int[cqs.length];
		int tot = 0;
		for (int i = 0 ; i < cqs.length ; i++) {
			tot += cqs[i].body.length;
			numBodyAtoms[i] = tot;
		}
		return numBodyAtoms;
	}

	@Override
	public CQ getCQ() {
		return getTotalCQ();
	}

	public CQ[] getCQs() {
		return cqs;
	}
	
	public int getNumCQs() {
		return cqs.length;
	}
}
