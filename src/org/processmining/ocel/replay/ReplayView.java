package org.processmining.ocel.replay;

import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.tbr.TokenBasedReplay;
import org.processmining.tbr.TokenBasedReplayResultLog;

public class ReplayView {
	public XLog filteredLog;
	public Set<String> activities;
	public PetrinetGraph net;
	public Marking im;
	public Marking fm;
	public TokenBasedReplayResultLog tbrResults;
	
	public ReplayView(XLog log, Set<String> activities) {
		this.filteredLog = FilterLogOnActivities.filterLogOnActivities(log, activities);
		this.activities = activities;
		this.minePetriUsingInductive();
		this.performTbr();
	}
	
	public void minePetriUsingInductive() {
		try {
			Object[] ret = NetUsingInductive.obtainPetriNetUsingInductiveMiner(this.filteredLog);
			this.net = (PetrinetGraph) ret[0];
			this.im = (Marking) ret[1];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.fm = new Marking();
		
		for (Place p : net.getPlaces()) {
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = net.getOutEdges(p);
			if (outEdges.size() == 0) {
				fm.add(p, 1);
			}
		}
	}
	
	public void performTbr() {
		this.tbrResults = TokenBasedReplay.applyTokenBasedReplay(this.filteredLog, this.net, this.im, this.fm, "concept:name");
	}
}
