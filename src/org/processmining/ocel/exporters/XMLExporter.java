package org.processmining.ocel.exporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.utils.TypeFromValue;

@Plugin(name = "Export OCEL to XML file", parameterLabels = { "OcelEventLog", "File" }, returnLabels = { }, returnTypes = {})
@UIExportPlugin(description = "Export OCEL to XML file", extension = "xmlocel")
public class XMLExporter {
	OcelEventLog eventLog;
	String filePath;
	
	public XMLExporter() {

	}
	
	public XMLExporter(OcelEventLog eventLog, String filePath) {
		this.eventLog = eventLog;
		this.filePath = filePath;
	}
	
	@PluginVariant(variantLabel = "Export OCEL to XML file", requiredParameterLabels = { 0, 1 })
	public void exportFromProm(PluginContext context, OcelEventLog eventLog, File file) {
		this.eventLog = eventLog;
		this.filePath = file.getAbsolutePath();
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportLogToStream(os);
	}
	
	public void exportLog() {
		FileOutputStream output0 = null;
		try {
			output0 = new FileOutputStream(this.filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportLogToStream(output0);
	}
	
	public void exportLogToStream(OutputStream output0) {
		OutputStream output = null;
		if (this.filePath.endsWith("gz")) {
			try {
				output = new GZIPOutputStream(output0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			output = output0;
		}
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(output, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XMLOutputter outter=new XMLOutputter();
		outter.setFormat(Format.getPrettyFormat());
		Document document = this.getXmlEventLog();
		try {
			outter.output(document, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document getXmlEventLog() {
		Document document = new Document();
		Element root = new Element("log");
		this.setGlobalEvent(root);
		this.setGlobalObject(root);
		this.setGlobalLog(root);
		this.exportEvents(root);
		this.exportObjects(root);
		document.setRootElement(root);
		return document;
	}
	
	public void setGlobalEvent(Element root) {
		Element globalEvent = new Element("global");
		globalEvent.setAttribute("scope", "event");
		for (String attribute : this.eventLog.globalEvent.keySet()) {
			Object attributeValue = this.eventLog.globalEvent.get(attribute);
			String[] typeString = TypeFromValue.getTypeStringForValue(attributeValue);
			Element xmlAttribute = new Element(typeString[0]);
			xmlAttribute.setAttribute("key", attribute);
			xmlAttribute.setAttribute("value", typeString[1]);
			globalEvent.addContent(xmlAttribute);
		}
		root.addContent(globalEvent);
	}
	
	public void setGlobalObject(Element root) {
		Element globalObject = new Element("global");
		globalObject.setAttribute("scope", "object");
		for (String attribute : this.eventLog.globalObject.keySet()) {
			Object attributeValue = this.eventLog.globalObject.get(attribute);
			String[] typeString = TypeFromValue.getTypeStringForValue(attributeValue);
			Element xmlAttribute = new Element(typeString[0]);
			xmlAttribute.setAttribute("key", attribute);
			xmlAttribute.setAttribute("value", typeString[1]);
			globalObject.addContent(xmlAttribute);
		}
		root.addContent(globalObject);
	}
	
	public void setGlobalLog(Element root) {
		Element globalLog = new Element("global");
		globalLog.setAttribute("scope", "log");
		Element version = new Element("string");
		version.setAttribute("key", "version");
		version.setAttribute("value", (String)this.eventLog.globalLog.get("ocel:version"));
		globalLog.addContent(version);
		Element ordering = new Element("string");
		ordering.setAttribute("key", "ordering");
		ordering.setAttribute("value", (String)this.eventLog.globalLog.get("ocel:ordering"));
		globalLog.addContent(ordering);
		Element attributeNames = new Element("list");
		attributeNames.setAttribute("key", "attribute-names");
		globalLog.addContent(attributeNames);
		Set<String> ocelAttributeNames = ((Set<String>)this.eventLog.globalLog.get("ocel:attribute-names"));
		for (String attributeName : ocelAttributeNames) {
			Element attribute = new Element("string");
			attribute.setAttribute("key", "attribute-name");
			attribute.setAttribute("value", attributeName);
			attributeNames.addContent(attribute);
		}
		Element objectTypes = new Element("list");
		objectTypes.setAttribute("key", "object-types");
		globalLog.addContent(objectTypes);
		Set<String> ocelObjectTypes = ((Set<String>)this.eventLog.globalLog.get("ocel:object-types"));
		for (String objectType : ocelObjectTypes) {
			Element ot = new Element("string");
			ot.setAttribute("key", "object-type");
			ot.setAttribute("value", objectType);
			objectTypes.addContent(ot);
		}
		root.addContent(globalLog);
	}
	
	public void exportEvents(Element root) {
		Element events = new Element("events");
		for (String eve : this.eventLog.events.keySet()) {
			OcelEvent ocelEvent = this.eventLog.events.get(eve);
			
			Element event = new Element("event");
			
			Element eventId = new Element("string");
			eventId.setAttribute("key", "id");
			Element eventActivity = new Element("string");
			eventActivity.setAttribute("key", "activity");
			Element eventTimestamp = new Element("date");
			eventTimestamp.setAttribute("key", "timestamp");
			Element eventOmap = new Element("list");
			eventOmap.setAttribute("key", "omap");
			Element eventVmap = new Element("list");
			eventVmap.setAttribute("key", "vmap");
			
			eventId.setAttribute("value", ocelEvent.id);
			eventActivity.setAttribute("value", ocelEvent.activity);
			eventTimestamp.setAttribute("value", ocelEvent.timestamp.toInstant().toString());
			
			for (OcelObject relObj : ocelEvent.relatedObjects) {
				Element xmlObj = new Element("string");
				xmlObj.setAttribute("key", "object-id");
				xmlObj.setAttribute("value", relObj.id);
				eventOmap.addContent(xmlObj);
			}
			
			for (String attribute : ocelEvent.attributes.keySet()) {
				Object attributeValue = ocelEvent.attributes.get(attribute);
				String[] typeString = TypeFromValue.getTypeStringForValue(attributeValue);
				Element xmlObj = new Element(typeString[0]);
				xmlObj.setAttribute("key", attribute);
				xmlObj.setAttribute("value", typeString[1]);
				eventVmap.addContent(xmlObj);
			}
			
			event.addContent(eventId);
			event.addContent(eventActivity);
			event.addContent(eventTimestamp);
			event.addContent(eventOmap);
			event.addContent(eventVmap);
			
			events.addContent(event);
		}
		root.addContent(events);
	}
	
	public void exportObjects(Element root) {
		Element objects = new Element("objects");
		for (String obj : this.eventLog.objects.keySet()) {
			OcelObject ocelObject = this.eventLog.objects.get(obj);
			
			Element object = new Element("object");
			
			Element objectId = new Element("string");
			objectId.setAttribute("key", "id");
			Element objectType = new Element("string");
			objectType.setAttribute("key", "type");
			Element objectVmap = new Element("list");
			objectVmap.setAttribute("key", "ovmap");
			
			objectId.setAttribute("value", ocelObject.id);
			objectType.setAttribute("value", ocelObject.objectType.name);
			
			for (String attribute : ocelObject.attributes.keySet()) {
				Object attributeValue = ocelObject.attributes.get(attribute);
				String[] typeString = TypeFromValue.getTypeStringForValue(attributeValue);
				Element xmlObj = new Element(typeString[0]);
				xmlObj.setAttribute("key", attribute);
				xmlObj.setAttribute("value", typeString[1]);
				objectVmap.addContent(xmlObj);
			}
			
			object.addContent(objectId);
			object.addContent(objectType);
			object.addContent(objectVmap);
			
			objects.addContent(object);
		}
		root.addContent(objects);
	}
}
