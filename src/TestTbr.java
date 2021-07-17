import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.ocel.replay.ReplayView;
import org.processmining.tbr.LogStaticReadingUtils;

public class TestTbr {
	public static void main(String[] args) {
		/*Object[] petriImFm = null;
		try {
			petriImFm = PetriNetUtils.importFromFile("C:/receipt_prom.pnml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PetrinetGraph net = (PetrinetGraph)petriImFm[0];
		Marking im = (Marking)petriImFm[1];
		Marking fm = (Marking)petriImFm[2];
		XLog log = null;
		try {
			log = LogStaticReadingUtils.readSingleLog("C:/receipt.xes");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TokenBasedReplayResultLog result = TokenBasedReplay.applyTokenBasedReplay(log, net, im, fm, "concept:name");
		String gv = TokenBasedReplayResultVisualization.getGraphviz(result);
		System.out.println(gv);*/
		XLog log = null;
		try {
			log = LogStaticReadingUtils.readSingleLog("C:/receipt.xes");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String> activities = new HashSet<String>();
		activities.add("Confirmation of receipt");
		ReplayView replayView = new ReplayView(log, activities);
		System.out.println(replayView.net.getPlaces());
		System.out.println(replayView.im);
		System.out.println(replayView.fm);
		System.out.println(replayView.tbrResults.totalConsumed + replayView.tbrResults.totalRemaining);
		System.out.println(replayView.tbrResults.totalProduced + replayView.tbrResults.totalMissing);
	}
}
