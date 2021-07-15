package org.processmining.tbr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Perform Token-Based Replay",
returnLabels = { "Token-Based Replay" },
returnTypes = { TokenBasedReplayResultLog.class },
parameterLabels = { "Event log", "Accepting Petri net" },
help = "Token-Based Replay",
userAccessible = true)
public class TokenBasedReplay {
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static TokenBasedReplayResultLog apply(PluginContext context, XLog log, AcceptingPetriNet acceptingPetriNet) {
		List<Marking> finalMarkings = new ArrayList<Marking>(acceptingPetriNet.getFinalMarkings());
		return TokenBasedReplay.applyTokenBasedReplay(log, acceptingPetriNet.getNet(), acceptingPetriNet.getInitialMarking(), finalMarkings.get(0), "concept:name");
	}
	
	public static TokenBasedReplayResultLog applyTokenBasedReplay(XLog log, PetrinetGraph net, Marking im, Marking fm, String activityKey) {
		Map<Place, Map<Place, List<Transition>>> invisiblesMap = TokenBasedReplay.getInvisiblesDictionary(net);
		Map<String, Transition> transitionsMap = new HashMap<String, Transition>();
		for (Transition t : net.getTransitions()) {
			if (!(t.isInvisible())) {
				transitionsMap.put(t.getLabel(), t);
			}
		}
		Map<Transition, Marking> preMarkingDict = TokenBasedReplay.getPreMarking(net);
		Map<Transition, Marking> postMarkingDict = TokenBasedReplay.getPostMarking(net);
		
		Map<String, TokenBasedReplayResultTrace> intermediateResults = new HashMap<String, TokenBasedReplayResultTrace>();
		List<TokenBasedReplayResultTrace> traceResult = new ArrayList<TokenBasedReplayResultTrace>();
		for (XTrace trace : log) {
			StringBuilder activities = new StringBuilder();
			for (XEvent eve : trace) {
				XAttributeMap attributeMap = eve.getAttributes();
				if (activities.length() > 0) {
					activities.append(",");
				}
				activities.append(attributeMap.get(activityKey).toString());
			}
			String activities2 = activities.toString();
			if (intermediateResults.containsKey(activities2)) {
				traceResult.add(intermediateResults.get(activities2));
			}
			else {
				TokenBasedReplayResultTrace replayedTrace = TokenBasedReplay.applyTokenBasedReplayToVariant(activities2, net, im, fm, invisiblesMap, transitionsMap, preMarkingDict, postMarkingDict);
				intermediateResults.put(activities2, replayedTrace);
				traceResult.add(replayedTrace);
			}
		}
		Object[] netImFm = new Object[3];
		netImFm[0] = net;
		netImFm[1] = im;
		netImFm[2] = fm;
		TokenBasedReplayResultLog ret = new TokenBasedReplayResultLog(traceResult, netImFm);
		System.out.println(ret.toString());
		return ret;
	}
	
	public static TokenBasedReplayResultTrace applyTokenBasedReplayToVariant(String var, PetrinetGraph net, Marking im, Marking fm, Map<Place, Map<Place, List<Transition>>> invisiblesDictionary, Map<String, Transition> transitionsMap, Map<Transition, Marking> preDict, Map<Transition, Marking> postDict) {
		String[] activities = var.split(",");
		Marking m = new Marking();
		for (Place p : im.baseSet()) {
			m.add(p, im.occurrences(p));
		}
		int consumed = 0;
		int produced = 0;
		int missing = 0;
		int remaining = 0;
		Map<Place, Integer> consumedPerPlace = new HashMap<Place, Integer>();
		Map<Place, Integer> producedPerPlace = new HashMap<Place, Integer>();
		Map<Place, Integer> missingPerPlace = new HashMap<Place, Integer>();
		Map<Place, Integer> remainingPerPlace = new HashMap<Place, Integer>();
		for (Place p : net.getPlaces()) {
			consumedPerPlace.put(p, 0);
			producedPerPlace.put(p, 0);
			missingPerPlace.put(p, 0);
			remainingPerPlace.put(p, 0);
		}
		for (Place p : im.baseSet()) {
			produced += im.occurrences(p);
			producedPerPlace.put(p, producedPerPlace.get(p)+im.occurrences(p));
		}
		for (Place p : fm.baseSet()) {
			consumed += fm.occurrences(p);
			consumedPerPlace.put(p, consumedPerPlace.get(p)+fm.occurrences(p));
		}
		List<Transition> visitedTransitions = new ArrayList<Transition>();
		List<Marking> visitedMarkings = new ArrayList<Marking>();
		Set<String> missingActivitiesInModel = new HashSet<String>();
		for (String act : activities) {
			if (transitionsMap.containsKey(act)) {
				Transition trans = transitionsMap.get(act);
				Marking preMarking = preDict.get(trans);
				Marking postMarking = postDict.get(trans);
				Set<Transition> enabledTransitions = TokenBasedReplay.getEnabledTransitions(m, preDict);
				if (!(enabledTransitions.contains(trans))) {
					List<Transition> newVisitedTransitions = new ArrayList<Transition>(visitedTransitions);
					Marking internalMarking = new Marking();
					for (Place p : m.baseSet()) {
						internalMarking.add(p, m.occurrences(p));
					}
					Integer internalConsumed = new Integer(consumed);
					Integer internalProduced = new Integer(produced);
					while (!(enabledTransitions.contains(trans))) {
						List<Transition> transList = TokenBasedReplay.enableTransThroughInvisibles(internalMarking, preMarking, invisiblesDictionary);
						if (transList == null) {
							break;
						}
						else {
							for (Transition internalTrans : transList) {
								Marking internalTransPreMarking = preDict.get(internalTrans);
								Marking internalTransPostMarking = postDict.get(internalTrans);
								Set<Transition> internalEnabledTrans = TokenBasedReplay.getEnabledTransitions(internalMarking, preDict);
								
								if (internalEnabledTrans.contains(internalTrans)) {
									newVisitedTransitions.add(internalTrans);
									/*System.out.println("");
									System.out.println(internalEnabledTrans);
									System.out.println(internalTrans);
									System.out.println(internalMarking);
									System.out.println("pp");
									System.out.println(internalTransPreMarking);
									System.out.println(internalTransPostMarking);*/
									internalMarking = TokenBasedReplay.fireTransition(internalMarking, internalTrans, preDict, postDict);
									//System.out.println(internalMarking);
									for (Place p : internalTransPreMarking.baseSet()) {
										internalConsumed += internalTransPreMarking.occurrences(p);
										consumedPerPlace.put(p, consumedPerPlace.get(p) + internalTransPreMarking.occurrences(p));
									}
									for (Place p : internalTransPostMarking.baseSet()) {
										internalProduced += internalTransPostMarking.occurrences(p);
										producedPerPlace.put(p, producedPerPlace.get(p) + internalTransPostMarking.occurrences(p));
									}
								}
								else {
									transList = null;
									break;
								}
							}
						}
						if (transList == null) {
							break;
						}
						enabledTransitions = TokenBasedReplay.getEnabledTransitions(internalMarking, preDict);
					}
					if (enabledTransitions.contains(trans)) {
						m = internalMarking;
						consumed = internalConsumed;
						produced = internalProduced;
						visitedTransitions = newVisitedTransitions;
					}
				}
				if (!(enabledTransitions.contains(trans))) {
					for (Place p : preMarking.baseSet()) {
						Integer diff = preMarking.occurrences(p);
						if (m.contains(p)) {
							diff -= m.occurrences(p);
						}
						if (diff > 0) {
							m.add(p, diff);
							missing += diff;
							missingPerPlace.put(p, missingPerPlace.get(p) + diff);
						}
					}
				}
				for (Place p : preMarking.baseSet()) {
					consumed += preMarking.occurrences(p);
					consumedPerPlace.put(p, consumedPerPlace.get(p) + preMarking.occurrences(p));
				}
				for (Place p : postMarking.baseSet()) {
					produced += postMarking.occurrences(p);
					producedPerPlace.put(p, producedPerPlace.get(p) + postMarking.occurrences(p));
				}
				m = TokenBasedReplay.fireTransition(m, trans, preDict, postDict);
				visitedMarkings.add(m);
				visitedTransitions.add(trans);
			}
			else {
				missingActivitiesInModel.add(act);
			}
		}
		
		if (!TokenBasedReplay.markingEquals(m, fm)) {
			Marking internalMarking = new Marking();
			for (Place p : m.baseSet()) {
				internalMarking.add(p, m.occurrences(p));
			}
			Integer internalConsumed = new Integer(consumed);
			Integer internalProduced = new Integer(produced);
			List<Transition> newVisitedTransitions = new ArrayList<Transition>(visitedTransitions);
			while (!TokenBasedReplay.markingEquals(m, fm)) {
				List<Transition> transList = TokenBasedReplay.reachFmThroughInvisibles(m, fm, invisiblesDictionary);
				if (transList == null) {
					break;
				}
				else {
					for (Transition internalTrans : transList) {
						Marking internalPreMarking = preDict.get(internalTrans);
						Marking internalPostMarking = postDict.get(internalTrans);
						Set<Transition> enabledTransitions = TokenBasedReplay.getEnabledTransitions(internalMarking, preDict);
						if (enabledTransitions.contains(internalTrans)) {
							newVisitedTransitions.add(internalTrans);
							internalMarking = TokenBasedReplay.fireTransition(internalMarking, internalTrans, preDict, postDict);
							for (Place p : internalPreMarking.baseSet()) {
								internalConsumed += internalPreMarking.occurrences(p);
								consumedPerPlace.put(p, consumedPerPlace.get(p) + internalPreMarking.occurrences(p));
							}
							for (Place p : internalPostMarking.baseSet()) {
								internalProduced += internalPostMarking.occurrences(p);
								producedPerPlace.put(p, producedPerPlace.get(p) + internalPostMarking.occurrences(p));
							}
						}
						else {
							transList = null;
							break;
						}
					}
					if (transList == null) {
						break;
					}
				}
				if (TokenBasedReplay.markingEquals(internalMarking, fm)) {
					m = internalMarking;
					consumed = internalConsumed;
					produced = internalProduced;
					visitedTransitions = newVisitedTransitions;
				}
			}
		}
		
		for (Place place : fm.baseSet()) {
			if (!(m.contains(place))) {
				missing += fm.occurrences(place);
				missingPerPlace.put(place, missingPerPlace.get(place) + fm.occurrences(place));
			}
			else if (m.occurrences(place) < fm.occurrences(place)) {
				missing += fm.occurrences(place) - m.occurrences(place);
				missingPerPlace.put(place, missingPerPlace.get(place) + fm.occurrences(place) - m.occurrences(place));
			}
		}
		for (Place place : m.baseSet()) {
			if (!(fm.contains(place))) {
				remaining += m.occurrences(place);
				remainingPerPlace.put(place, remainingPerPlace.get(place) + m.occurrences(place));
			}
			else if (m.occurrences(place) > fm.occurrences(place)) {
				remaining += m.occurrences(place) - fm.occurrences(place);
				remainingPerPlace.put(place, remainingPerPlace.get(place) + m.occurrences(place) - fm.occurrences(place));
			}
		}
		Double fitMC = new Double(0.0);
		Double fitRP = new Double(0.0);
		if (consumed > 0) {
			fitMC = 1.0 - new Float(missing) / new Float(consumed);
		}
		if (produced > 0) {
			fitRP = 1.0 - new Float(remaining) / new Float(produced);
		}
		Double fitness = 0.5*fitMC + 0.5*fitRP;
		Boolean isFit = new Boolean(missingActivitiesInModel.size() == 0 && missing == 0);
		TokenBasedReplayResultTrace ret = new TokenBasedReplayResultTrace(consumed, produced, missing, remaining, fitness, isFit, visitedTransitions, visitedMarkings, missingActivitiesInModel, consumedPerPlace, producedPerPlace, missingPerPlace, remainingPerPlace);
		System.out.println(ret);
		return ret;
	}
	
	public static boolean markingEquals(Marking m1, Marking m2) {
		for (Place p : m1) {
			if (!(m2.contains(p)) || m2.occurrences(p) != m1.occurrences(p)) {
				return false;
			}
		}
		for (Place p : m2) {
			if (!(m1.contains(p)) || m2.occurrences(p) != m1.occurrences(p)) {
				return false;
			}
		}
		return true;
	}
	
	public static List<Transition> reachFmThroughInvisibles(Marking marking, Marking finalMarking, Map<Place, Map<Place, List<Transition>>> invisiblesDictionary) {
		Set<Place> diff1 = new HashSet<Place>();
		Set<Place> diff2 = new HashSet<Place>();
		for (Place p : marking) {
			if (!(finalMarking.contains(p))) {
				diff1.add(p);
			}
		}
		for (Place p : finalMarking) {
			if (!(marking.contains(p)) || marking.occurrences(p) < finalMarking.occurrences(p)) {
				diff2.add(p);
			}
 		}
		for (Place p : diff1) {
			for (Place p2 : diff2) {
				if (invisiblesDictionary.get(p).containsKey(p2)) {
					return invisiblesDictionary.get(p).get(p2);
				}
			}
		}
		return null;
	}
	
	public static List<Transition> enableTransThroughInvisibles(Marking marking, Marking preMarking, Map<Place, Map<Place, List<Transition>>> invisiblesDictionary) {
		Set<Place> diff1 = new HashSet<Place>();
		Set<Place> diff2 = new HashSet<Place>();
		for (Place p : marking) {
			if (!(preMarking.contains(p))) {
				diff1.add(p);
			}
		}
		for (Place p : preMarking) {
			if (!(marking.contains(p)) || marking.occurrences(p) < preMarking.occurrences(p)) {
				diff2.add(p);
			}
		}
		for (Place p : diff1) {
			for (Place p2 : diff2) {
				if (invisiblesDictionary.get(p).containsKey(p2)) {
					return invisiblesDictionary.get(p).get(p2);
				}
			}
		}
		return null;
	}
	
	public static Set<Transition> getEnabledTransitions(Marking m, Map<Transition, Marking> preDict) {
		Set<Transition> enabledTransitions = new HashSet<Transition>();
		for (Transition t : preDict.keySet()) {
			boolean isOk = true;
			Marking preMarking = preDict.get(t);
			for (Place p : preMarking) {
				if (!(m.contains(p) && preMarking.occurrences(p) <= m.occurrences(p))) {
					isOk = false;
				}
			}
			if (isOk) {
				enabledTransitions.add(t);
			}
		}
		return enabledTransitions;
	}
	
	public static Marking fireTransition(Marking m, Transition t, Map<Transition, Marking> preDict, Map<Transition, Marking> postDict) {
		Marking preMarking = preDict.get(t);
		Marking postMarking = postDict.get(t);
		Marking ret = new Marking();
		for (Place p : preMarking.baseSet()) {
			if (!(m.contains(p)) || m.occurrences(p) < preMarking.occurrences(p)) {
				return null;
			}
		}
		for (Place p : postMarking.baseSet()) {
			ret.add(p, postMarking.occurrences(p));
		}
		for (Place p : m.baseSet()) {
			if (m.occurrences(p) > preMarking.occurrences(p)) {
				ret.add(p, m.occurrences(p) - preMarking.occurrences(p));
			}
		}
		return ret;
	}
	
	public static Map<Transition, Marking> getPreMarking(PetrinetGraph net) {
		Map<Transition, Marking> ret = new HashMap<Transition, Marking>();
		for (Transition t : net.getTransitions()) {
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inArcs = net.getInEdges(t);
			Marking m = new Marking();
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : inArcs) {
				m.add((Place)arc.getSource(), 1);
			}
			ret.put(t, m);
		}
		return ret;
	}
	
	public static Map<Transition, Marking> getPostMarking(PetrinetGraph net) {
		Map<Transition, Marking> ret = new HashMap<Transition, Marking>();
		for (Transition t : net.getTransitions()) {
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outArcs = net.getOutEdges(t);
			Marking m = new Marking();
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : outArcs) {
				m.add((Place)arc.getTarget(), 1);
			}
			ret.put(t, m);
		}
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
