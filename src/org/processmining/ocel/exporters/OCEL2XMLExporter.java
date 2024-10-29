package org.processmining.ocel.exporters;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Plugin(name = "Export OCEL 2.0 to XML file", parameterLabels = { "OcelEventLog", "File" }, returnLabels = { }, returnTypes = {})
@UIExportPlugin(description = "Export OCEL 2.0 to XML file", extension = "xml")
public class OCEL2XMLExporter {

    public OcelEventLog eventLog;
    
    public OCEL2XMLExporter() {
    	
    }

    public OCEL2XMLExporter(OcelEventLog eventLog) {
        this.eventLog = eventLog;
    }
    
	@PluginVariant(variantLabel = "Export OCEL 2.0 to XML file", requiredParameterLabels = { 0, 1 })
	public void exportFromProm(PluginContext context, OcelEventLog eventLog, File file) {
		this.eventLog = eventLog;
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportLogToStream(os);
	}

    public void exportLogToStream(OutputStream output0) {
        try {
            // Initialize XML Document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Create root element <log>
            Element rootElement = doc.createElement("log");
            doc.appendChild(rootElement);

            // Create <object-types>
            createObjectTypesElement(doc, rootElement);

            // Create <event-types> (if event types are needed)
            // You can implement this similarly to object types if event type definitions are stored

            // Create <objects>
            createObjectsElement(doc, rootElement);

            // Create <events>
            createEventsElement(doc, rootElement);

            // Write the content into the OutputStream
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Optional: Format the XML output
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(output0);

            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private void createObjectTypesElement(Document doc, Element rootElement) {
        Element objectTypesElement = doc.createElement("object-types");
        rootElement.appendChild(objectTypesElement);

        // For each object type
        for (String objectTypeName : eventLog.objectTypes.keySet()) {
            OcelObjectType objectType = eventLog.objectTypes.get(objectTypeName);
            Element objectTypeElement = doc.createElement("object-type");
            objectTypeElement.setAttribute("name", objectTypeName);
            objectTypesElement.appendChild(objectTypeElement);

            // Create <attributes> element
            Element attributesElement = doc.createElement("attributes");
            objectTypeElement.appendChild(attributesElement);

            // Since OcelObjectType does not store attribute definitions,
            // this section remains empty unless you extend the class to include them
        }
    }

    private void createObjectsElement(Document doc, Element rootElement) {
        Element objectsElement = doc.createElement("objects");
        rootElement.appendChild(objectsElement);

        for (OcelObject ocelObject : eventLog.objects.values()) {
            Element objectElement = doc.createElement("object");
            objectElement.setAttribute("id", ocelObject.id);
            objectElement.setAttribute("type", ocelObject.objectType.name);
            objectsElement.appendChild(objectElement);

            // Add <attributes>
            Element attributesElement = doc.createElement("attributes");
            objectElement.appendChild(attributesElement);

            // Combine initial attributes and timed attributes
            Map<String, TreeMap<Date, Object>> allAttributes = new HashMap<>();

            // Initial attributes (assumed to have time "1970-01-01T00:00:00Z")
            for (String attrName : ocelObject.attributes.keySet()) {
                TreeMap<Date, Object> timedValues = new TreeMap<>();
                timedValues.put(new Date(0), ocelObject.attributes.get(attrName)); // Epoch time
                allAttributes.put(attrName, timedValues);
            }

            // Timed attributes
            for (String attrName : ocelObject.timedAttributes.keySet()) {
                Map<Date, Object> timedValues = ocelObject.timedAttributes.get(attrName);
                TreeMap<Date, Object> sortedTimedValues = new TreeMap<>(timedValues);
                if (allAttributes.containsKey(attrName)) {
                    allAttributes.get(attrName).putAll(sortedTimedValues);
                } else {
                    allAttributes.put(attrName, sortedTimedValues);
                }
            }

            // Write attributes to XML
            for (String attrName : allAttributes.keySet()) {
                TreeMap<Date, Object> timedValues = allAttributes.get(attrName);
                for (Date time : timedValues.keySet()) {
                    Element attributeElement = doc.createElement("attribute");
                    attributeElement.setAttribute("name", attrName);
                    attributeElement.setAttribute("time", formatISODate(time));
                    attributeElement.setTextContent(timedValues.get(time).toString());
                    attributesElement.appendChild(attributeElement);
                }
            }

            // Add related objects
            if (!ocelObject.relatedObjectIdentifiers.isEmpty()) {
                Element relatedObjectsElement = doc.createElement("objects");
                objectElement.appendChild(relatedObjectsElement);

                for (String relatedObjectId : ocelObject.relatedObjectIdentifiers.keySet()) {
                    String qualifier = ocelObject.relatedObjectIdentifiers.get(relatedObjectId);
                    Element relationshipElement = doc.createElement("relationship");
                    relationshipElement.setAttribute("object-id", relatedObjectId);
                    relationshipElement.setAttribute("qualifier", qualifier);
                    relatedObjectsElement.appendChild(relationshipElement);
                }
            }
        }
    }

    private void createEventsElement(Document doc, Element rootElement) {
        Element eventsElement = doc.createElement("events");
        rootElement.appendChild(eventsElement);

        for (OcelEvent ocelEvent : eventLog.events.values()) {
            Element eventElement = doc.createElement("event");
            eventElement.setAttribute("id", ocelEvent.id);
            eventElement.setAttribute("type", ocelEvent.activity);
            eventElement.setAttribute("time", formatISODate(ocelEvent.timestamp));
            eventsElement.appendChild(eventElement);

            // Add <attributes>
            if (!ocelEvent.attributes.isEmpty()) {
                Element attributesElement = doc.createElement("attributes");
                eventElement.appendChild(attributesElement);

                for (String attrName : ocelEvent.attributes.keySet()) {
                    Element attributeElement = doc.createElement("attribute");
                    attributeElement.setAttribute("name", attrName);
                    attributeElement.setTextContent(ocelEvent.attributes.get(attrName).toString());
                    attributesElement.appendChild(attributeElement);
                }
            }

            // Add related objects
            if (!ocelEvent.relatedObjectsIdentifiers.isEmpty()) {
                Element relatedObjectsElement = doc.createElement("objects");
                eventElement.appendChild(relatedObjectsElement);

                for (String relatedObjectId : ocelEvent.relatedObjectsIdentifiers.keySet()) {
                    String qualifier = ocelEvent.relatedObjectsIdentifiers.get(relatedObjectId);
                    Element relationshipElement = doc.createElement("relationship");
                    relationshipElement.setAttribute("object-id", relatedObjectId);
                    relationshipElement.setAttribute("qualifier", qualifier);
                    relatedObjectsElement.appendChild(relationshipElement);
                }
            }
        }
    }

    private String formatISODate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        return sdf.format(date);
    }
}
