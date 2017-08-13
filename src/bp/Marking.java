package bp;

import java.util.HashSet;
import java.util.Set;


public class Marking {
	Set<Arc> deletedArcs;
	Set<Arc> strongArcs;

	public Set<Arc> getDeletedArcs() {
		return deletedArcs;
	}

	public void setDeletedArcs(Set<Arc> deletedArcs) {
		this.deletedArcs = deletedArcs;
	}

	public Set<Arc> getStrongArcs() {
		return strongArcs;
	}

	public void setStrongArcs(Set<Arc> strongArcs) {
		this.strongArcs = strongArcs;
	}
	
	public Set<Arc> getWeakArcs(DependencyGraph graph) {
		Set<Arc> rv = new HashSet<Arc>();
		rv.addAll(graph.getArcs());
		rv.removeAll(strongArcs);
		rv.removeAll(deletedArcs);
		return rv;
	}

	public Marking() {
		deletedArcs = new HashSet<Arc>();
		strongArcs = new HashSet<Arc>();
	}

	public Marking(Marking m) {
		deletedArcs = new HashSet<Arc>(m.deletedArcs);
		strongArcs = new HashSet<Arc>(m.strongArcs);
	}

	public Marking(Set<Arc> strongSet, Set<Arc> deletedSet) {
		strongArcs = strongSet;
		deletedArcs = deletedSet;
	}

	public boolean equals(Object o) {
		Set<Arc> se = ((Marking)o).strongArcs;
		Set<Arc> de = ((Marking)o).deletedArcs;
		return se.equals(strongArcs) && de.equals(deletedArcs);
	}

	public String toString() {
		return "Strong arcs: " + strongArcs + " - Deleted arcs: " + deletedArcs;
	}
}
