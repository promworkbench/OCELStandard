package org.processmining.tbr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class TokenBasedReplay {
	public static TokenBasedReplayResultLog applyTokenBasedReplay(XLog log, PetrinetGraph net, Marking im, Marking fm) {
		Map<Place, Map<Place, List<Transition>>> invisiblesMap = TokenBasedReplay.getInvisiblesDictionary(net);
		Map<String, Transition> transitionsMap = new HashMap<String, Transition>();
		for (Transition t : net.getTransitions()) {
			if (!(t.isInvisible())) {
				transitionsMap.put(t.getLabel(), t);
			}
		}
		Map<String, TokenBasedReplayResultTrace> intermediateResults = new HashMap<String, TokenBasedReplayResultTrace>();
		List<TokenBasedReplayResultTrace> traceResult = new ArrayList<TokenBasedReplayResultTrace>();
		for (XTrace trace : log) {
			StringBuilder activities = new StringBuilder();
			for (XEvent eve : trace) {
				XAttributeMap attributeMap = eve.getAttributes();
				if (activities.length() > 0) {
					activities.append(",");
				}
				activities.append(attributeMap.get("concept:name").toString());
			}
			String activities2 = activities.toString();
			if (intermediateResults.containsKey(activities2)) {
				traceResult.add(intermediateResults.get(activities2));
			}
			else {
				TokenBasedReplayResultTrace replayedTrace = TokenBasedReplay.applyTokenBasedReplayToVariant(activities2, net, im, fm, invisiblesMap, transitionsMap);
				intermediateResults.put(activities2, replayedTrace);
				traceResult.add(replayedTrace);
			}
		}
		TokenBasedReplayResultLog ret = new TokenBasedReplayResultLog();
		return ret;
	}
	
	public static TokenBasedReplayResultTrace applyTokenBasedReplayToVariant(String var, PetrinetGraph net, Marking im, Marking fm, Map<Place, Map<Place, List<Transition>>> invisiblesDictionary, Map<String, Transition> transitionsMap) {
		String[] activities = var.split(",");
		TokenBasedReplayResultTrace ret = new TokenBasedReplayResultTrace();
		Marking m = new Marking();
		for (Place p : im) {
			m.add(p, im.occurrences(p));
		}
		int consumed = 0;
		int produced = 0;
		int missing = 0;
		int remaining = 0;
		Map<Place, Integer> consumedPerPlace = new HashMap<Place, Integer>();
		return ret;
	}
	
	public static Map<Place, Map<Place, List<Transition>>> getInvisiblesDictionary(PetrinetGraph net) {
		Map<Place, Map<Place, List<Transition>>> ret = new HashMap<Place, Map<Place, List<Transition>>>();
		for (Place p : net.getPlaces()) {
			ret.put(p, new HashMap<Place, List<Transition>>());
		}
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible()) {
				List<Transition> thisTransList = new ArrayList<Transition>();
				thisTransList.add(t);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inArcs = net.getInEdges(t);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outArcs = net.getOutEdges(t);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a : inArcs) {
					Place p = (Place)a.getSource();
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a2 : outArcs) {
						Place p2 = (Place)a2.getTarget();
						ret.get(p).put(p2, new ArrayList<Transition>(thisTransList));
					}
				}
			}
		}
		boolean cont = true;
		while (cont) {
			cont = false;
			for (Place p1 : ret.keySet()) {
				List<Place> places2 = new ArrayList<Place>(ret.get(p1).keySet());
				for (Place p2 : places2) {
					for (Place p3 : ret.get(p2).keySet()) {
						if (!(ret.get(p1).containsKey(p3))) {
							List<Transition> thisList = new ArrayList<Transition>(ret.get(p1).get(p2));
							thisList.addAll(ret.get(p2).get(p3));
							ret.get(p1).put(p3, thisList);
							cont = true;
						}
					}
				}
			}
		}
		return ret;
	}
}
