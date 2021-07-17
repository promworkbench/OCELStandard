package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.ocel.flattening.Flattening;
import org.processmining.ocel.ocelobjects.OcelObjectType;
import org.processmining.ocel.replay.ReplayView;

public class ModelWithTbrResults {
	public AnnotatedModel model;
	public Set<String> activities;
	public Map<OcelObjectType, ReplayView> replayViews;
	
	public ModelWithTbrResults(AnnotatedModel model, Set<String> activities) {
		this.model = model;
		this.activities = activities;
		this.replayViews = new HashMap<OcelObjectType, ReplayView>();
		for (OcelObjectType ot : model.ocel.objectTypes.values()) {
			XLog log = Flattening.flatten(model.ocel, ot.name);
			ReplayView replayView = new ReplayView(log, activities);
			replayViews.put(ot, replayView);
		}
	}
}
