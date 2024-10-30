package org.processmining.ocel.importers;


import org.processmining.ocel.ocelobjects.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class OCEL2SQLImporter {
    public OcelEventLog doImportFromStream(InputStream is0) throws Exception {
        // Write InputStream to a temporary file
        Path tempFile = Files.createTempFile("ocel_temp_db", ".sqlite");
        try (OutputStream out = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is0.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // Load the SQLite JDBC driver
        Class.forName("org.sqlite.JDBC");

        // Create a connection to the temporary SQLite database file
        String url = "jdbc:sqlite:" + tempFile.toAbsolutePath().toString();
        Connection conn = DriverManager.getConnection(url);

        // Create the event log object
        OcelEventLog eventLog = new OcelEventLog();

        // Load event and object types
        Map<String, String> eventTypeMap = loadEventTypes(conn, eventLog);
        Map<String, String> objectTypeMap = loadObjectTypes(conn, eventLog);

        // Load events
        Map<String, OcelEvent> events = loadEvents(conn, eventLog, eventTypeMap);

        // Load objects
        Map<String, OcelObject> objects = loadObjects(conn, eventLog, objectTypeMap);

        // Load event-type-specific attributes
        loadEventAttributes(conn, events, eventTypeMap);

        // Load object-type-specific attributes
        loadObjectAttributes(conn, objects, objectTypeMap);

        // Load event-object relationships
        loadEventObjectRelationships(conn, events, objects);

        // Load object-object relationships (if needed)
        loadObjectObjectRelationships(conn, objects);

        // Close the database connection
        conn.close();

        // Delete the temporary file
        Files.deleteIfExists(tempFile);

        // Register the events and objects in the event log
        eventLog.register();

        return eventLog;
    }

    private Map<String, String> loadEventTypes(Connection conn, OcelEventLog eventLog) throws SQLException {
        Map<String, String> eventTypeMap = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ocel_type, ocel_type_map FROM event_map_type");
        while (rs.next()) {
            String ocelType = rs.getString("ocel_type");
            String ocelTypeMap = rs.getString("ocel_type_map");
            eventTypeMap.put(ocelType, ocelTypeMap);
        }
        rs.close();
        stmt.close();
        return eventTypeMap;
    }

    private Map<String, String> loadObjectTypes(Connection conn, OcelEventLog eventLog) throws SQLException {
        Map<String, String> objectTypeMap = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ocel_type, ocel_type_map FROM object_map_type");
        while (rs.next()) {
            String ocelType = rs.getString("ocel_type");
            String ocelTypeMap = rs.getString("ocel_type_map");
            objectTypeMap.put(ocelType, ocelTypeMap);
            eventLog.objectTypes.put(ocelTypeMap, new OcelObjectType(eventLog, ocelTypeMap));
            eventLog.getObjectTypes().add(ocelTypeMap);
        }
        rs.close();
        stmt.close();
        return objectTypeMap;
    }

    private Map<String, OcelEvent> loadEvents(Connection conn, OcelEventLog eventLog, Map<String, String> eventTypeMap) throws SQLException {
        Map<String, OcelEvent> events = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ocel_id, ocel_type FROM event");
        while (rs.next()) {
            String eventId = rs.getString("ocel_id");
            String eventType = rs.getString("ocel_type");
            OcelEvent event = new OcelEvent(eventLog);
            event.id = eventId;
            event.activity = eventType;
            events.put(eventId, event);
            eventLog.events.put(eventId, event);
            eventLog.getAttributeNames().add(eventType);
        }
        rs.close();
        stmt.close();
        return events;
    }

    private Map<String, OcelObject> loadObjects(Connection conn, OcelEventLog eventLog, Map<String, String> objectTypeMap) throws SQLException {
        Map<String, OcelObject> objects = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ocel_id, ocel_type FROM object");
        while (rs.next()) {
            String objectId = rs.getString("ocel_id");
            String objectType = rs.getString("ocel_type");
            OcelObject object = new OcelObject(eventLog);
            object.id = objectId;
            object.objectType = eventLog.objectTypes.get(objectTypeMap.get(objectType));
            objects.put(objectId, object);
            eventLog.objects.put(objectId, object);
        }
        rs.close();
        stmt.close();
        return objects;
    }

    private void loadEventAttributes(Connection conn, Map<String, OcelEvent> events, Map<String, String> eventTypeMap) throws SQLException {
        for (String eventType : eventTypeMap.keySet()) {
            String tableName = "event_" + eventTypeMap.get(eventType);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                String eventId = rs.getString("ocel_id");
                OcelEvent event = events.get(eventId);
                // Load timestamp
                String dateStr = rs.getString("ocel_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
					event.timestamp = sdf.parse(dateStr.replace('T', ' ').split("\\.")[0]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                // Load other attributes
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    if (!columnName.equals("ocel_id") && !columnName.equals("ocel_time")) {
                        Object value = rs.getObject(columnName);
                        event.attributes.put(columnName, value);
                        event.eventLog.getAttributeNames().add(columnName);
                    }
                }
            }
            rs.close();
            stmt.close();
        }
    }

    private void loadObjectAttributes(Connection conn, Map<String, OcelObject> objects, Map<String, String> objectTypeMap) throws SQLException {
        for (String objectType : objectTypeMap.keySet()) {
            String tableName = "object_" + objectTypeMap.get(objectType);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY ocel_id, ocel_time");
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                String objectId = rs.getString("ocel_id");
                OcelObject object = objects.get(objectId);
                String timeStr = rs.getString("ocel_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                
                Date timestamp = new Date();
				try {
					timestamp = sdf.parse(timeStr.replace('T', ' ').split("\\.")[0]);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                // Load attributes
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    if (!columnName.equals("ocel_id") && !columnName.equals("ocel_time") && !columnName.equals("ocel_changed_field")) {
                        Object value = rs.getObject(columnName);
                        if (timestamp.getTime() == 0) {
                            // Initial attribute value
                            object.attributes.put(columnName, value);
                        } else {
                            // Attribute changes over time
                            object.timedAttributes
                                    .computeIfAbsent(columnName, k -> new HashMap<>())
                                    .put(timestamp, value);
                        }
                    }
                }
            }
            rs.close();
            stmt.close();
        }
    }

    private void loadEventObjectRelationships(Connection conn, Map<String, OcelEvent> events, Map<String, OcelObject> objects) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ocel_event_id, ocel_object_id, ocel_qualifier FROM event_object");
        while (rs.next()) {
            String eventId = rs.getString("ocel_event_id");
            String objectId = rs.getString("ocel_object_id");
            String qualifier = rs.getString("ocel_qualifier");
            OcelEvent event = events.get(eventId);
            OcelObject object = objects.get(objectId);
            if (event != null && object != null) {
                event.relatedObjects.put(object, qualifier);
                event.relatedObjectsIdentifiers.put(objectId, qualifier);
                object.relatedEvents.add(event);
            }
        }
        rs.close();
        stmt.close();
    }

    private void loadObjectObjectRelationships(Connection conn, Map<String, OcelObject> objects) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ocel_source_id, ocel_target_id, ocel_qualifier FROM object_object");
        while (rs.next()) {
            String sourceId = rs.getString("ocel_source_id");
            String targetId = rs.getString("ocel_target_id");
            String qualifier = rs.getString("ocel_qualifier");
            OcelObject sourceObject = objects.get(sourceId);
            OcelObject targetObject = objects.get(targetId);
            if (sourceObject != null && targetObject != null && qualifier != null) {
            	sourceObject.relatedObjectIdentifiers.put(targetId, qualifier);
            }
        }
        rs.close();
        stmt.close();
    }
}