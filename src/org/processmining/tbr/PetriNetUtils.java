package org.processmining.tbr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PetriNetUtils {
	public static Pnml importPnmlFromStream(InputStream input,
			String filename, long fileSizeInBytes) throws
			XmlPullParserException, IOException {
			FullPnmlElementFactory pnmlFactory = new FullPnmlElementFactory();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(input, null);
			int eventType = xpp.getEventType();
			Pnml pnml = new Pnml();
			synchronized (pnmlFactory) {
				pnml.setFactory(pnmlFactory);
				/*
				* Skip whatever we find until we've found a start tag.
				*/
				while (eventType != XmlPullParser.START_TAG) {
					eventType = xpp.next();
				}
				/*
				* Check whether start tag corresponds to PNML start tag.
				*/
				if (xpp.getName().equals(Pnml.TAG)) {
				/*
				* Yes it does. Import the PNML element.
				*/
					pnml.importElement(xpp, pnml);
				} else {
				/*
				* No it does not. Return null to signal failure.
				*/
				pnml.log(Pnml.TAG, xpp.getLineNumber(), "Expected pnml");
				}
				if (pnml.hasErrors()) {
					return null;
				}
				return pnml;
			}
		}
	
	public static Object[] connectNet(Pnml pnml, PetrinetGraph net) {
		/*
		* Return the net and the marking.
		*/
		Marking marking = new Marking();
		Collection<Marking> finalMarkings = new HashSet<Marking>();
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		
		pnml.convertToNet(net, marking, finalMarkings, layout);
		Marking fm = new Marking();
		
		for (Place p : net.getPlaces()) {
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = net.getOutEdges(p);
			if (outEdges.size() == 0) {
				fm.add(p, 1);
			}
		}
		Object[] objects = new Object[3];
		objects[0] = net;
		objects[1] = marking;
		objects[2] = fm;
		return objects;
	}
	
	public static Object[] importFromStream(InputStream input,
		String filename, long fileSizeInBytes) throws
		XmlPullParserException, IOException {
		Pnml pnml = importPnmlFromStream(input, filename, fileSizeInBytes);
		if (pnml == null) {
		/*
		* No PNML found in file. Fail.
		*/
			return null;
		}
		PetrinetGraph net = PetrinetFactory.newPetrinet(pnml.getLabel());
		return connectNet(pnml, net);
	}
	
	public static Object[] importFromFile(String filename) throws Exception {
		File file = new File(filename);
		return importFromStream(new FileInputStream(file), filename,
		file.length());
	}

}
