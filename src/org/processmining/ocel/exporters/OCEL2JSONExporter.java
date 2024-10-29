package org.processmining.ocel.exporters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.processmining.ocel.ocelobjects.*;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class OCEL2JSONExporter {
    private final OcelEventLog eventLog;

    public OCEL2JSONExporter(OcelEventLog eventLog) {
        this.eventLog = eventLog;
    }

    public void exportLogToStream(OutputStream output0) {
        ObjectMapper mapper = new ObjectMapper();
        // Configure the mapper for pretty printing and date formatting
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));

        try {
            // Create a map to hold the top-level structure
            Map<String, Object> root = new HashMap<>();

            // Serialize objectTypes
            List<Map<String, Object>> objectTypesList = new ArrayList<>();
            for (OcelObjectType objectType : eventLog.objectTypes.values()) {
                Map<String, Object> objectTypeMap = new HashMap<>();
                objectTypeMap.put("name", objectType.name);
                // Assuming attributes are stored in globalObject or elsewhere
                // For this example, we'll leave 'attributes' empty
                objectTypeMap.put("attributes", Collections.emptyList());
                objectTypesList.add(objectTypeMap);
            }
            root.put("objectTypes", objectTypesList);

            // Serialize eventTypes
            List<Map<String, Object>> eventTypesList = new ArrayList<>();
            // We need to collect event types and their attributes
            // Since OcelEventLog doesn't store event types separately, we'll infer them from events
            Map<String, Set<String>> eventTypeAttributesMap = new HashMap<>();
            for (OcelEvent event : eventLog.events.values()) {
                eventTypeAttributesMap.computeIfAbsent(event.activity, k -> new HashSet<>())
                        .addAll(event.attributes.keySet());
            }
            for (String eventTypeName : eventTypeAttributesMap.keySet()) {
                Map<String, Object> eventTypeMap = new HashMap<>();
                eventTypeMap.put("name", eventTypeName);
                List<Map<String, Object>> attributesList = new ArrayList<>();
                for (String attrName : eventTypeAttributesMap.get(eventTypeName)) {
                    Map<String, Object> attrMap = new HashMap<>();
                    attrMap.put("name", attrName);
                    attrMap.put("type", "string"); // Assuming type is string for simplicity
                    attributesList.add(attrMap);
                }
                eventTypeMap.put("attributes", attributesList);
                eventTypesList.add(eventTypeMap);
            }
            root.put("eventTypes", eventTypesList);

            // Serialize objects
            List<Map<String, Object>> objectsList = new ArrayList<>();
            for (OcelObject obj : eventLog.objects.values()) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("id", obj.id);
                objectMap.put("type", obj.objectType.name);

                // Serialize attributes
                List<Map<String, Object>> attributesList = new ArrayList<>();
                for (Map.Entry<String, Object> attrEntry : obj.attributes.entrySet()) {
                    Map<String, Object> attrMap = new HashMap<>();
                    attrMap.put("name", attrEntry.getKey());
                    attrMap.put("time", "1970-01-01T00:00:00Z"); // Default time for the first value
                    attrMap.put("value", attrEntry.getValue());
                    attributesList.add(attrMap);
                }
                // Serialize timed attributes
                for (Map.Entry<String, Map<Date, Object>> timedAttrEntry : obj.timedAttributes.entrySet()) {
                    String attrName = timedAttrEntry.getKey();
                    Map<Date, Object> timeValues = timedAttrEntry.getValue();
                    for (Map.Entry<Date, Object> timeValueEntry : timeValues.entrySet()) {
                        Map<String, Object> attrMap = new HashMap<>();
                        attrMap.put("name", attrName);
                        attrMap.put("time", formatTime(timeValueEntry.getKey()));
                        attrMap.put("value", timeValueEntry.getValue());
                        attributesList.add(attrMap);
                    }
                }
                objectMap.put("attributes", attributesList);

                // Serialize relationships
                List<Map<String, Object>> relationshipsList = new ArrayList<>();
                for (Map.Entry<String, String> relEntry : obj.relatedObjectIdentifiers.entrySet()) {
                    Map<String, Object> relMap = new HashMap<>();
                    relMap.put("objectId", relEntry.getKey());
                    relMap.put("qualifier", relEntry.getValue());
                    relationshipsList.add(relMap);
                }
                if (!relationshipsList.isEmpty()) {
                    objectMap.put("relationships", relationshipsList);
                }

                objectsList.add(objectMap);
            }
            root.put("objects", objectsList);

            // Serialize events
            List<Map<String, Object>> eventsList = new ArrayList<>();
            for (OcelEvent event : eventLog.events.values()) {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", event.id);
                eventMap.put("type", event.activity);
                eventMap.put("time", formatTime(event.timestamp));

                // Serialize attributes
                List<Map<String, Object>> attributesList = new ArrayList<>();
                for (Map.Entry<String, Object> attrEntry : event.attributes.entrySet()) {
                    Map<String, Object> attrMap = new HashMap<>();
                    attrMap.put("name", attrEntry.getKey());
                    attrMap.put("value", attrEntry.getValue());
                    attributesList.add(attrMap);
                }
                eventMap.put("attributes", attributesList);

                // Serialize relationships
                List<Map<String, Object>> relationshipsList = new ArrayList<>();
                for (Map.Entry<OcelObject, String> relEntry : event.relatedObjects.entrySet()) {
                    Map<String, Object> relMap = new HashMap<>();
                    relMap.put("objectId", relEntry.getKey().id);
                    relMap.put("qualifier", relEntry.getValue());
                    relationshipsList.add(relMap);
                }
                eventMap.put("relationships", relationshipsList);

                eventsList.add(eventMap);
            }
            root.put("events", eventsList);

            // Write the JSON to the output stream
            JsonGenerator generator = mapper.getFactory().createGenerator(output0);
            mapper.writeValue(generator, root);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions as needed
        }
    }

    private String formatTime(Date date) {
        // Format the date in ISO 8601 format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        return sdf.format(date);
    }
}