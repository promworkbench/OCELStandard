package org.processmining.ocel.discovery;

import java.util.Comparator;

public class ModelEdgeComparator implements Comparator<ModelEdge> {

	public int compare(ModelEdge o1, ModelEdge o2) {
		// TODO Auto-generated method stub
		if (o1.realizations.size() > o2.realizations.size()) {
			return -1;
		}
		else if (o1.realizations.size() < o2.realizations.size()) {
			return 1;
		}
		return 0;
	}

}
