package org.processmining.ocel.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

@Plugin(name = "Import OCEL from XML", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OcelEventLog.class })
@UIImportPlugin(description = "Import OCEL from XML", extensions = { "xmlocel", "gz" })
public class XMLImporter extends AbstractImportPlugin {
	String logPath;
	
	public XMLImporter() {
		
	}
	
	public XMLImporter(String logPath) {
		this.logPath = logPath;
	}
	
	protected OcelEventLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		this.logPath = filename;
		return doImportFromStream(input);
	}
	
	public OcelEventLog doImport() {
		File file = new File(this.logPath);
		InputStream is0 = null;
		try {
			is0 = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doImportFromStream(is0);
	}
	
	public OcelEventLog doImportFromStream(InputStream is0) {
		InputStream is = null;
		if (this.logPath.endsWith(".gz")) {
			try {
				is = new GZIPInputStream(is0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			is = is0;
		}
		
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try {
			document = (Document) builder.build(is);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OcelEventLog eventLog = new OcelEventLog();
		
		for (Element inner0 : (List<Element>)document.getContent()) {
			if (inner0.getName().equals("log")) {
				List<Element> children = inner0.getChildren();
				for (Element inner : children) {
					if (inner.getName().equals("events")) {
						this.importEvents(eventLog, inner);
					}
					else if (inner.getName().equals("objects")) {
						this.importObjects(eventLog, inner);
					}
					else if (inner.getName().equals("global")) {
						String scope = inner.getAttribute("scope").getValue();
						if (scope.equals("log")) {
							this.importGlobalLog(eventLog, inner);
						}
						else if (scope.equals("event")) {
							this.importGlobalEvent(eventLog, inner);
						}
						else if (scope.equals("object")) {
							this.importGlobalObject(eventLog, inner);
						}
					}
				}
			}
		}
		
		eventLog.register();
				
		return eventLog;
	}
	
	public void importGlobalEvent(OcelEventLog eventLog, Element globalEvent) {
		System.out.println("called importGlobalEvent");
		List<Element> children = globalEvent.getChildren();
		for (Element inner : children) {
			String type = inner.getName();
			String name = inner.getAttributeValue("key");
			String value = inner.getAttributeValue("value");
			
			if (type == "string") {
				eventLog.globalEvent.put(name, value);
			}
			else if (type.equals("float")) {
				eventLog.globalEvent.put(name, Float.parseFloat(value));
			}
			else if (type.equals("date")) {
				Date dateValue = null;
				try {
					dateValue = Date.from( Instant.parse( value ));
				}
				catch (Exception ex) {
					dateValue = Date.from( Instant.parse( value + "Z" ));
				}
				eventLog.globalEvent.put(name, dateValue);
			}
			else {
				eventLog.globalEvent.put(name, value);
			}
		}
	}
	
	public void importGlobalObject(OcelEventLog eventLog, Element globalObject) {
		System.out.println("called importGlobalObject");
		List<Element> children = globalObject.getChildren();
		for (Element inner : children) {
			String type = inner.getName();
			String name = inner.getAttributeValue("key");
			String value = inner.getAttributeValue("value");
			
			if (type.equals("string")) {
				eventLog.globalObject.put(name, value);
			}
			else if (type.equals("float")) {
				eventLog.globalObject.put(name, Float.parseFloat(value));
			}
			else if (type.equals("date")) {
				Date dateValue = null;
				try {
					dateValue = Date.from( Instant.parse( value ));
				}
				catch (Exception ex) {
					dateValue = Date.from( Instant.parse( value + "Z" ));
				}
				eventLog.globalObject.put(name, dateValue);
			}
			else {
				eventLog.globalObject.put(name, value);
			}
		}
	}
	
	public void importGlobalLog(OcelEventLog eventLog, Element globalLog) {
		System.out.println("called importGlobalLog");
		List<Element> children = globalLog.getChildren();
		for (Element inner : children) {
			String key = inner.getAttribute("key").getValue();
			if (key.equals("version")) {
				String value = inner.getAttribute("value").getValue();
				eventLog.globalLog.put("ocel:version", value);
			}
			else if (key.equals("ordering")) {
				String value = inner.getAttribute("value").getValue();
				eventLog.globalLog.put("ocel:ordering", value);
			}
			else if (key.equals("attribute-names")) {
				List<Element> attributeNames = inner.getChildren();
				Set<String> ocelAttributeNames = (Set<String>)eventLog.globalLog.get("ocel:attribute-names");
				for (Element attributeName : attributeNames) {
					String value = attributeName.getAttribute("value").getValue();
					ocelAttributeNames.add(value);
				}
			}
			else if (key.equals("object-types")) {
				List<Element> objectTypes = inner.getChildren();
				Set<String> ocelObjectTypes = (Set<String>)eventLog.globalLog.get("ocel:object-types");
				for (Element objectType : objectTypes) {
					String value = objectType.getAttribute("value").getValue();
					ocelObjectTypes.add(value);
				}
			}
		}
	}
	
	public void importEvents(OcelEventLog eventLog, Element xmlevents) {
		List<Element> events = xmlevents.getChildren();
		for (Element xmlEvent : events) {
			OcelEvent event = new OcelEvent(eventLog);
			String id = "";
			String activity = "";
			Date timestamp = new Date();
			
			List<Element> eventProperties = xmlEvent.getChildren();
			for (Element property : eventProperties) {
				String key = property.getAttributeValue("key");
				if (key.equals("id")) {
					id = property.getAttributeValue("value");
				}
				else if (key.equals("activity")) {
					activity = property.getAttributeValue("value");
				}
				else if (key.equals("timestamp")) {
					String value = property.getAttributeValue("value");
					try {
						timestamp = Date.from( Instant.parse( value ));
					}
					catch (Exception ex) {
						timestamp = Date.from( Instant.parse( value + "Z" ));
					}
					//System.out.println(event.timestamp);
				}
				else if (key.equals("omap")) {
					List<Element> relatedObjects = property.getChildren();
					for (Element relatedObject : relatedObjects) {
						String value = relatedObject.getAttributeValue("value");
						event.relatedObjectsIdentifiers.put(value, "");
					}
				}
				else if (key.equals("vmap")) {
					List<Element> subattributes = property.getChildren();
					for (Element subattribute : subattributes) {
						String type = subattribute.getName();
						String name = subattribute.getAttributeValue("key");
						String value = subattribute.getAttributeValue("value");
												
						if (type.equals("string")) {
							event.attributes.put(name, value);
						}
						else if (type.equals("float")) {							
							event.attributes.put(name, Float.parseFloat(value));
						}
						else if (type.equals("date")) {
							Date dateValue = null;
							try {
								dateValue = Date.from( Instant.parse( value ));
							}
							catch (Exception ex) {
								dateValue = Date.from( Instant.parse( value + "Z" ));
							}
							event.attributes.put(name, dateValue);
						}
						else {
							event.attributes.put(name, value);
						}
					}
				}
			}
			
			event.id = id;
			event.activity = activity;
			event.timestamp = timestamp;
			
			eventLog.events.put(id, event);
		}
		System.out.println("called importEvents");
	}
	
	public void importObjects(OcelEventLog eventLog, Element xmlobjects) {
		List<Element> objects = xmlobjects.getChildren();
		for (Element xmlObject : objects) {
			OcelObject object = new OcelObject(eventLog);
			String id = "";
						
			List<Element> objectProperties = xmlObject.getChildren();
			for (Element property : objectProperties) {
				String key = property.getAttributeValue("key");
				if (key.equals("id")) {
					id = property.getAttributeValue("value");
				}
				else if (key.equals("type")) {
					String objectTypeName = property.getAttributeValue("value");
					if (!(eventLog.objectTypes.containsKey(objectTypeName))) {
						OcelObjectType objectType = new OcelObjectType(eventLog, objectTypeName);
						eventLog.objectTypes.put(objectTypeName, objectType);
					}
					OcelObjectType objectType = eventLog.objectTypes.get(objectTypeName);
					object.objectType = objectType;
				}
				else if (key.equals("ovmap")) {
					List<Element> subattributes = property.getChildren();
					for (Element subattribute : subattributes) {
						String type = subattribute.getName();
						String name = subattribute.getAttributeValue("key");
						String value = subattribute.getAttributeValue("value");
						
						if (type.equals("string")) {
							object.attributes.put(name, value);
						}
						else if (type.equals("float")) {
							object.attributes.put(name, Float.parseFloat(value));
						}
						else if (type.equals("date")) {
							Date dateValue = null;
							try {
								dateValue = Date.from( Instant.parse( value ));
							}
							catch (Exception ex) {
								dateValue = Date.from( Instant.parse( value + "Z" ));
							}
							object.attributes.put(name, dateValue);
						}
						else {
							object.attributes.put(name, value);
						}
					}
				}
			}
			
			object.id = id;
			
			eventLog.objects.put(id, object);
		}
		System.out.println("called importObjects");
	}
}
