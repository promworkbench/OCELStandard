package org.processmining.tbr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class TokenBasedReplayResultLog {
	public PetrinetGraph net;
	public Marking initialMarking;
	public Marking finalMarking;
 	public List<TokenBasedReplayResultTrace> resultsPerTrace;
	public int totalConsumed;
	public int totalProduced;
	public int totalMissing;
	public int totalRemaining;
	public Map<Transition, Integer> transExecutions;
	public Map<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>, Integer> arcExecutions;
	public Map<Place, Integer> totalConsumedPerPlace;
	public Map<Place, Integer> totalProducedPerPlace;
	public Map<Place, Integer> totalMissingPerPlace;
	public Map<Place, Integer> totalRemainingPerPlace;
	public int totalTraces;
	public int fitTraces;
	public double logFitness;
	
	public TokenBasedReplayResultLog(List<TokenBasedReplayResultTrace> resultsPerTrace, Object[] netImFm) {
		this.resultsPerTrace = resultsPerTrace;
		this.net = (PetrinetGraph)netImFm[0];
		this.initialMarking = (Marking)netImFm[1];
		this.finalMarking = (Marking)netImFm[2];
		this.totalConsumed = 0;
		this.totalProduced = 0;
		this.totalMissing = 0;
		this.totalRemaining = 0;
		this.transExecutions = new HashMap<Transition, Integer>();
		this.arcExecutions = new HashMap<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>, Integer>();
		this.totalConsumedPerPlace = new HashMap<Place, Integer>();
		this.totalProducedPerPlace = new HashMap<Place, Integer>();
		this.totalMissingPerPlace = new HashMap<Place, Integer>();
		this.totalRemainingPerPlace = new HashMap<Place, Integer>();
		this.totalTraces = this.resultsPerTrace.size();
		this.fitTraces = 0;
		this.logFitness = 0.0;
		for (Transition t : net.getTransitions()) {
			this.transExecutions.put(t, 0);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inArcs = net.getInEdges(t);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outArcs = net.getOutEdges(t);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : inArcs) {
				this.arcExecutions.put(arc, 0);
			}
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : outArcs) {
				this.arcExecutions.put(arc, 0);
			}
		}
		for (Place p : net.getPlaces()) {
			this.totalConsumedPerPlace.put(p, 0);
			this.totalProducedPerPlace.put(p, 0);
			this.totalMissingPerPlace.put(p, 0);
			this.totalRemainingPerPlace.put(p, 0);
		}
		this.compute();
	}
	
	public void compute() {
		for (TokenBasedReplayResultTrace res : resultsPerTrace) {
			if (res.isFit) {
				this.fitTraces++;
			}
			this.totalConsumed += res.consumed;
			this.totalProduced += res.produced;
			this.totalMissing += res.missing;
			this.totalRemaining += res.remaining;
			double fitMC = 0.0;
			double fitRP = 0.0;
			if (this.totalConsumed > 0) {
				fitMC = 1.0 - new Double(this.totalMissing) / new Double(this.totalConsumed);
			}
			if (this.totalProduced > 0) {
				fitRP = 1.0 - new Double(this.totalRemaining) / new Double(this.totalProduced);
			}
			this.logFitness = 0.5*fitMC + 0.5*fitRP;
			for (Transition t : res.visitedTransitions) {
				this.transExecutions.put(t, this.transExecutions.get(t) + 1);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inArcs = net.getInEdges(t);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outArcs = net.getOutEdges(t);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : inArcs) {
					this.arcExecutions.put(arc, this.arcExecutions.get(arc) + 1);
				}
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : outArcs) {
					this.arcExecutions.put(arc, this.arcExecutions.get(arc) + 1);
				}				
			}
			for (Place p : this.net.getPlaces()) {
				this.totalConsumedPerPlace.put(p, this.totalConsumedPerPlace.get(p) + res.consumedPerPlace.get(p));
				this.totalProducedPerPlace.put(p, this.totalProducedPerPlace.get(p) + res.producedPerPlace.get(p));
				this.totalMissingPerPlace.put(p, this.totalMissingPerPlace.get(p) + res.missingPerPlace.get(p));
				this.totalRemainingPerPlace.put(p, this.totalRemainingPerPlace.get(p) + res.remainingPerPlace.get(p));
			}
		}
	}
	
	public String toString() {
		String ret = String.format("total traces = %d; fit traces = %d; c = %d; p = %d, m = %d, r = %d, fitness = %f", this.totalTraces, this.fitTraces, this.totalConsumed, this.totalProduced, this.totalMissing, this.totalRemaining, this.logFitness);
		return ret;
	}
}
