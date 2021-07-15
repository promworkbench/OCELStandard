import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.tbr.LogStaticReadingUtils;
import org.processmining.tbr.PetriNetUtils;
import org.processmining.tbr.TokenBasedReplay;
import org.processmining.tbr.TokenBasedReplayResultLog;
import org.processmining.tbr.TokenBasedReplayResultVisualization;

public class TestTbr {
	public static void main(String[] args) {
		Object[] petriImFm = null;
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
		System.out.println(gv);
	}
}
