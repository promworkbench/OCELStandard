package org.processmining.tbr;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class TokenBasedReplayResultVisualization {
	public static Double[] rgbColor(double percent) {
		Double[] ret = new Double[3];
		ret[0] = 255*percent;
		ret[1] = 255*(1 - percent);
		ret[2] = 255*(1 - percent);
		return ret;
	}
	
	public static String hexFromRGB(double r, double g, double b) {
		String[] hex = new String[3];
		hex[0] = Integer.toHexString(new Double(Math.floor(r)).intValue());
		hex[1] = Integer.toHexString(new Double(Math.floor(g)).intValue());
		hex[2] = Integer.toHexString(new Double(Math.floor(b)).intValue());
		int i = 0;
		while (i < hex.length) {
			if (((String)hex[i]).length() == 1) {
				hex[i] = "0" + hex[i];
			}
			i++;
		}
		return ("#"+((String)hex[0])+((String)hex[1])+((String)hex[2])).toLowerCase();
	}
	
	public static String getGraphviz(TokenBasedReplayResultLog tbrResult) {
		StringBuilder ret = new StringBuilder();
		PetrinetGraph net = tbrResult.net;
		Marking im = tbrResult.initialMarking;
		Marking fm = tbrResult.finalMarking;
		Map<PetrinetNode, String> uidMap = new HashMap<PetrinetNode, String>();
		Integer transMaxFrequency = -1;
		Integer arcMaxFrequency = -1;
		for (Transition trans : tbrResult.transExecutions.keySet()) {
			transMaxFrequency = Math.max(transMaxFrequency, tbrResult.transExecutions.get(trans));
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : tbrResult.arcExecutions.keySet()) {
			arcMaxFrequency = Math.max(arcMaxFrequency, tbrResult.arcExecutions.get(arc));
		}
		ret.append("digraph G {\n");
		ret.append("rankdir=\"LR\"\n");
		for (Place place : net.getPlaces()) {
			String fillColor = "white";
			String nUid = "place"+place.getId().toString().replaceAll(" ", "").replaceAll("-", "");
			if (im.contains(place)) {
				fillColor = "green";
			}
			else if (fm.contains(place)) {
				fillColor = "orange"; 
			}
			String placeLabel = String.format("p=%d;m=%d\nc=%d;r=%d", tbrResult.totalProducedPerPlace.get(place), tbrResult.totalMissingPerPlace.get(place), tbrResult.totalConsumedPerPlace.get(place), tbrResult.totalRemainingPerPlace.get(place));
			ret.append(nUid+" [shape=ellipse, label=\""+placeLabel+"\", style=filled, fillcolor="+fillColor+"]\n");
			uidMap.put(place, nUid);
		}
		for (Transition trans : net.getTransitions()) {
			String nUid = "trans"+trans.getId().toString().replaceAll(" ", "").replaceAll("-", "");
			double perc = 1.0 - new Double(tbrResult.transExecutions.get(trans))/new Double(transMaxFrequency);
			Double[] rgb = TokenBasedReplayResultVisualization.rgbColor(perc);
			String rgbHex = TokenBasedReplayResultVisualization.hexFromRGB(rgb[0], rgb[1], rgb[2]);
			if (trans.isInvisible()) {
				ret.append(nUid+" [shape=box, label=\" \", style=filled, fillcolor=black]\n");
			}
			else {
				ret.append(nUid+" [shape=box, label=\""+trans.getLabel()+"\n"+tbrResult.transExecutions.get(trans)+"\"]\n");
			}
			uidMap.put(trans, nUid);
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : tbrResult.arcExecutions.keySet()) {
			PetrinetNode arcSource = arc.getSource();
			PetrinetNode arcTarget = arc.getTarget();
			String uuid1 = uidMap.get(arcSource);
			String uuid2 = uidMap.get(arcTarget);
			double penwidth = 0.5 + Math.log10(1 + tbrResult.arcExecutions.get(arc));
			ret.append(uuid1+" -> "+uuid2+" [label=\""+tbrResult.arcExecutions.get(arc)+"\", penwidth=\""+penwidth+"\"]\n");
		}
		ret.append("}\n");
		return ret.toString();
	}
}
