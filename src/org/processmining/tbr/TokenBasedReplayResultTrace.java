package org.processmining.tbr;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class TokenBasedReplayResultTrace {
	int consumed;
	int produced;
	int missing;
	int remaining;
	Double fitness;
	Boolean isFit;
	List<Transition> visitedTransitions;
	List<Marking> visitedMarkings;
	Set<String> missingActivitiesInModel;
	Map<Place, Integer> consumedPerPlace;
	Map<Place, Integer> producedPerPlace;
	Map<Place, Integer> missingPerPlace;
	Map<Place, Integer> remainingPerPlace;
	
	public TokenBasedReplayResultTrace(int consumed, int produced, int missing, int remaining, Double fitness, Boolean isFit, List<Transition> visitedTransitions, List<Marking> visitedMarkings, Set<String> missingActivitiesInModel, Map<Place, Integer> consumedPerPlace, Map<Place, Integer> producedPerPlace, Map<Place, Integer> missingPerPlace, Map<Place, Integer> remainingPerPlace) {
		this.consumed = consumed;
		this.produced = produced;
		this.missing = missing;
		this.remaining = remaining;
		this.fitness = fitness;
		this.isFit = isFit;
		this.visitedTransitions = visitedTransitions;
		this.visitedMarkings = visitedMarkings;
		this.missingActivitiesInModel = missingActivitiesInModel;
		this.consumedPerPlace = consumedPerPlace;
		this.producedPerPlace = producedPerPlace;
		this.missingPerPlace = missingPerPlace;
		this.remainingPerPlace = remainingPerPlace;
	}
	
	public String toString() {
		return String.format("isFit=%b; c=%d; p=%d; m=%d; r=%d; fitness=%f", isFit, consumed, produced, missing, remaining, fitness);
	}
}
