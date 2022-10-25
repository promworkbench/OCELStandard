package org.processmining.ocel.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

@Plugin(name = "Filter OCEL on activities - object types correspondence",
returnLabels = { "Object-Centric Event Log" },
returnTypes = { OcelEventLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class FilterActOtList {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static OcelEventLog applyPlugin(UIPluginContext context, OcelEventLog ocel) {
		FilterActOtListWizardStep wizStep = new FilterActOtListWizardStep(ocel);
		List<ProMWizardStep<FilterActOtListWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<FilterActOtListWizardParameters> listWizard = new ListWizard<>(wizStepList);
		FilterActOtListWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new FilterActOtListWizardParameters());
		
		return FilterActOtList.filterActOtList(ocel.preFilter, parameters.activitiesList);
	}
	
	public static Map<String, Map<String, Integer>> getActOtStatistics(OcelEventLog ocel) {
		Map<String, Map<String, Integer>> ret = new HashMap<String, Map<String, Integer>>();
		for (OcelEvent eve : ocel.events.values()) {
			for (OcelObject obj : eve.relatedObjects) {
				String otype = obj.objectType.name;
				if (!(ret.containsKey(otype))) {
					ret.put(otype, new HashMap<String, Integer>());
				}
				Map<String, Integer> objTypeMap = ret.get(otype);
				if (!(objTypeMap.containsKey(eve.activity))) {
					objTypeMap.put(eve.activity, 1);
				}
				else {
					objTypeMap.put(eve.activity, objTypeMap.get(eve.activity) + 1);
				}
			}
		}
		return ret;
	}
	
	public static String getActOtStatisticsStri(OcelEventLog log) {
		StringBuilder ret = new StringBuilder();
		Map<String, Map<String, Integer>> actOtStatistics = FilterActOtList.getActOtStatistics(log);
		for (String otype : actOtStatistics.keySet()) {
			ret.append(otype+"\n");
			for (String act : actOtStatistics.get(otype).keySet()) {
				ret.append(">>>"+act+">>>YES\n");
			}
		}
		return ret.toString();
	}
	
	public static OcelEventLog filterActOtList(OcelEventLog log, String filterString) {
		String[] lines = filterString.split("\n");
		Map<String, Set<String>> actAllowedOts = new HashMap<String, Set<String>>();
		String consideredObjectType = "";
		for (String line : lines) {
			String[] spli = line.split(">>>");
			if (spli.length >= 3) {
				String acti = spli[1];
				if (spli[2].equals("YES")) {
					if (!(actAllowedOts.containsKey(acti))) {
						actAllowedOts.put(acti, new HashSet<String>());
					}
					actAllowedOts.get(acti).add(consideredObjectType);
				}
			}
			else {
				consideredObjectType = spli[0];
			}
		}
		OcelEventLog filtered = log.cloneEmpty();
		for (OcelEvent eve : log.events.values()) {
			String acti = eve.activity;
			if (actAllowedOts.containsKey(acti)) {
				filtered.cloneEvent(eve, actAllowedOts.get(acti));
			}
		}
		filtered.register();
		return filtered;
	}
}
