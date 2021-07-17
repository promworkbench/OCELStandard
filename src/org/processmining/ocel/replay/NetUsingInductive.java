package org.processmining.ocel.replay;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParametersForPetriNet;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.mining.InductiveMiner;
import org.processmining.plugins.inductiveminer2.mining.MiningParameters;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;

public class NetUsingInductive {
	public static Object[] obtainPetriNetUsingInductiveMiner(XLog log) throws
	Exception {
		Object[] ret = new Object[2];
		MiningParameters parameters = new MyMiningParameters();
		InductiveMinerPlugin im = new InductiveMinerPlugin();
		IMLog imlog = parameters.getIMLog(log);
		EfficientTree tree = InductiveMiner.mineEfficientTree(imlog, parameters,
		new Canceller() {
		public boolean isCancelled() {
		return false;
		}
		});
		AcceptingPetriNet petri = null;
		if (tree != null) {
		EfficientTreeReduce.reduce(tree, new
		EfficientTreeReduceParametersForPetriNet(false));
		petri = EfficientTree2AcceptingPetriNet.convert(tree);
		}
		Canceller canceller = new Canceller() { public boolean isCancelled() {
		return false; } };
		ReduceAcceptingPetriNetKeepLanguage.reduce(petri, canceller);
		Petrinet net = petri.getNet();
		Marking marking = petri.getInitialMarking();
		ret[0] = net;
		ret[1] = marking;
		return ret;
		}
}
