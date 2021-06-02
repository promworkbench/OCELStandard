package org.processmining.ocel.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.utils.Separator;

public class ModelEdges {
	OcelEventLog ocel;
	public List<ModelEdge> edges;
	
	public ModelEdges(OcelEventLog ocel) {
		this.ocel = ocel;
		this.calculate();
	}
	
	public void calculate() {
		Map<String, ModelEdge> edges0 = new HashMap<String, ModelEdge>();
		for (String o : ocel.objects.keySet()) {
			OcelObject obj = ocel.objects.get(o);
			String otype = obj.objectType.name;
			int i = 0;
			while (i < obj.sortedRelatedEvents.size()-1) {
				OcelEvent eve1 = obj.sortedRelatedEvents.get(i);
				OcelEvent eve2 = obj.sortedRelatedEvents.get(i+1);
				String e1 = eve1.id;
				String e2 = eve2.id;
				String a1 = eve1.activity;
				String a2 = eve2.activity;
				String key = a1 + Separator.SEPARATOR + a2 + Separator.SEPARATOR + otype;
				if (!edges0.containsKey(key)) {
					edges0.put(key, new ModelEdge(a1, a2, obj.objectType));
				}
				edges0.get(key).addRealization(e1, e2);
				i++;
			}
		}
		edges = new ArrayList<ModelEdge>(edges0.values());
		Collections.sort(edges, new ModelEdgeComparator());
	}
}
