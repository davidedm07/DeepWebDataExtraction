package bp;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class Arc implements Comparable<Arc> {

	static final Logger logger = LoggerFactory.getLogger();
	Node from;
	Node to;
	
	public Node getTo() {
		return to;
	}

	public static Set<Arc> getTransitiveClosure(Set<Arc> arcs) {
		logger.fine("Arcs: " + arcs);
		Set<Arc> closure = new TreeSet<Arc>();
		for (Arc one : arcs) {
			for (Arc two : arcs) {
				if (one.to.equals(two.from)) {
					Arc three = new Arc(one.from, two.to);
					if (!arcs.contains(three)) {
						closure.add(three);
					}
				}
			}
		}

		logger.fine("Delta arc closure: " + closure);
		if (closure.isEmpty()) {
			closure.addAll(arcs);
		} else {
			closure.addAll(arcs);
			closure = getTransitiveClosure(closure);
		}
		return closure;
	}
	
	public void setTo(Node destination) {
		this.to = destination;
	}


	public Node getFrom() {
		return from;
	}

	public boolean equals(Arc e) {
		return e.from.equals(from) && e.to.equals(to);
	}

	public void setFrom(Node source) {
		this.from = source;
	}


	public Arc(Node source, Node destination) {
		this.from = source;
		this.to = destination;
	}

	public String toString() {
		return from + "->" + to; 
	}
	
    public int compareTo(Arc e) {
    	if (e.from.equals(from)) return to.compareTo(e.to);
        return from.compareTo(e.from);
    }


}
