package org.processmining.ocel.exporters;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Plugin(name = "Export OCEL 2.0 to SQLite file (beta)", parameterLabels = { "OcelEventLog", "File" }, returnLabels = { }, returnTypes = {})
@UIExportPlugin(description = "Export OCEL 2.0 to SQLite file (beta)", extension = "sqlite")
public class OCEL2SQLiteExporter {
    public OcelEventLog eventLog;
    
    public OCEL2SQLiteExporter() {
    	
    }

    public OCEL2SQLiteExporter(OcelEventLog eventLog) {
        this.eventLog = eventLog;
    }

	@PluginVariant(variantLabel = "Export OCEL 2.0 to SQLite file", requiredParameterLabels = { 0, 1 })
	public void exportFromProm(PluginContext context, OcelEventLog eventLog, File file) throws Exception {
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
	
    public void exportLogToStream(OutputStream output0) throws Exception {
        // Create a temporary file for the SQLite database
        Path tempFile = Files.createTempFile("ocel_export_temp_db", ".sqlite");

        try {
            // Create SQLite database at tempFile
            String url = "jdbc:sqlite:" + tempFile.toAbsolutePath().toString();
            Connection conn = DriverManager.getConnection(url);

            // Create tables
            createTables(conn);

            // Insert data
            insertData(conn);

            // Close connection
            conn.close();

            // Read tempFile and write to output0
            try (InputStream in = Files.newInputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    output0.write(buffer, 0, bytesRead);
                }
            }

        } finally {
            // Delete temp file
            Files.deleteIfExists(tempFile);
        }
    }

    private void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        // Create event_map_type table
        String createEventMapType = "CREATE TABLE event_map_type (" +
                "ocel_type TEXT PRIMARY KEY," +
                "ocel_type_map TEXT" +
                ")";
        stmt.execute(createEventMapType);

        // Create object_map_type table
        String createObjectMapType = "CREATE TABLE object_map_type (" +
                "ocel_type TEXT PRIMARY KEY," +
                "ocel_type_map TEXT" +
                ")";
        stmt.execute(createObjectMapType);

        // Create event table
        String createEventTable = "CREATE TABLE event (" +
                "ocel_id TEXT PRIMARY KEY," +
                "ocel_type TEXT," +
                "FOREIGN KEY(ocel_type) REFERENCES event_map_type(ocel_type)" +
                ")";
        stmt.execute(createEventTable);

        // Create object table
        String createObjectTable = "CREATE TABLE object (" +
                "ocel_id TEXT PRIMARY KEY," +
                "ocel_type TEXT," +
                "FOREIGN KEY(ocel_type) REFERENCES object_map_type(ocel_type)" +
                ")";
        stmt.execute(createObjectTable);

        // Create event_object table
        String createEventObjectTable = "CREATE TABLE event_object (" +
                "ocel_event_id TEXT," +
                "ocel_object_id TEXT," +
                "ocel_qualifier TEXT," +
                "PRIMARY KEY(ocel_event_id, ocel_object_id, ocel_qualifier)," +
                "FOREIGN KEY(ocel_event_id) REFERENCES event(ocel_id)," +
                "FOREIGN KEY(ocel_object_id) REFERENCES object(ocel_id)" +
                ")";
        stmt.execute(createEventObjectTable);

        // Create object_object table
        String createObjectObjectTable = "CREATE TABLE object_object (" +
                "ocel_source_id TEXT," +
                "ocel_target_id TEXT," +
                "ocel_qualifier TEXT," +
                "PRIMARY KEY(ocel_source_id, ocel_target_id, ocel_qualifier)," +
                "FOREIGN KEY(ocel_source_id) REFERENCES object(ocel_id)," +
                "FOREIGN KEY(ocel_target_id) REFERENCES object(ocel_id)" +
                ")";
        stmt.execute(createObjectObjectTable);

        stmt.close();
    }

    private void insertData(Connection conn) throws SQLException {
        // Collect event types and object types
        Set<String> eventTypes = new HashSet<>();
        Set<String> objectTypes = new HashSet<>();

        for (OcelEvent event : eventLog.events.values()) {
            eventTypes.add(event.activity);
        }
        for (OcelObject object : eventLog.objects.values()) {
            objectTypes.add(object.objectType.name);
        }

        // Create mappings and tables
        Map<String, String> eventTypeMap = new HashMap<>();
        Map<String, String> objectTypeMap = new HashMap<>();

        // Create event_map_type and event type tables
        for (String eventType : eventTypes) {
            String eventTypeMapName = sanitizeName(eventType);
            eventTypeMap.put(eventType, eventTypeMapName);

            // Insert into event_map_type
            String insertEventMapType = "INSERT INTO event_map_type (ocel_type, ocel_type_map) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertEventMapType)) {
                pstmt.setString(1, eventType);
                pstmt.setString(2, eventTypeMapName);
                pstmt.executeUpdate();
            }

            // Create event_<eventTypeMapName> table
            // Need to collect all attribute names for this event type
            Set<String> attributeNames = new HashSet<>();
            for (OcelEvent event : eventLog.events.values()) {
                if (event.activity.equals(eventType)) {
                    attributeNames.addAll(event.attributes.keySet());
                }
            }
            // Create table with columns: ocel_id, ocel_time, and attributes
            StringBuilder createEventTypeTable = new StringBuilder("CREATE TABLE event_" + eventTypeMapName + " (" +
                    "ocel_id TEXT PRIMARY KEY," +
                    "ocel_time TEXT");

            for (String attr : attributeNames) {
                createEventTypeTable.append(", ").append(attr).append(" TEXT");
            }
            createEventTypeTable.append(", FOREIGN KEY(ocel_id) REFERENCES event(ocel_id))");

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createEventTypeTable.toString());
            }
        }

        // Create object_map_type and object type tables
        for (String objectType : objectTypes) {
            String objectTypeMapName = sanitizeName(objectType);
            objectTypeMap.put(objectType, objectTypeMapName);

            // Insert into object_map_type
            String insertObjectMapType = "INSERT INTO object_map_type (ocel_type, ocel_type_map) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertObjectMapType)) {
                pstmt.setString(1, objectType);
                pstmt.setString(2, objectTypeMapName);
                pstmt.executeUpdate();
            }

            // Create object_<objectTypeMapName> table
            // Need to collect all attribute names for this object type
            Set<String> attributeNames = new HashSet<>();
            for (OcelObject object : eventLog.objects.values()) {
                if (object.objectType.name.equals(objectType)) {
                    attributeNames.addAll(object.attributes.keySet());
                    for (String attr : object.timedAttributes.keySet()) {
                        attributeNames.add(attr);
                    }
                }
            }
            // Create table with columns: ocel_id, ocel_time, ocel_changed_field, and attributes
            StringBuilder createObjectTypeTable = new StringBuilder("CREATE TABLE object_" + objectTypeMapName + " (" +
                    "ocel_id TEXT," +
                    "ocel_time TEXT," +
                    "ocel_changed_field TEXT");

            for (String attr : attributeNames) {
                createObjectTypeTable.append(", ").append(attr).append(" TEXT");
            }
            createObjectTypeTable.append(", FOREIGN KEY(ocel_id) REFERENCES object(ocel_id))");

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createObjectTypeTable.toString());
            }
        }

        // Insert into event and event_<eventType> tables
        for (OcelEvent event : eventLog.events.values()) {
            // Insert into event table
            String insertEvent = "INSERT INTO event (ocel_id, ocel_type) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertEvent)) {
                pstmt.setString(1, event.id);
                pstmt.setString(2, event.activity);
                pstmt.executeUpdate();
            }

            // Insert into event_<eventType> table
            String eventTypeMapName = eventTypeMap.get(event.activity);
            StringBuilder insertEventType = new StringBuilder("INSERT INTO event_" + eventTypeMapName + " (ocel_id, ocel_time");
            List<String> attrNames = new ArrayList<>(event.attributes.keySet());
            for (String attr : attrNames) {
                insertEventType.append(", ").append(attr);
            }
            insertEventType.append(") VALUES (?");
            for (int i = 0; i < attrNames.size() + 1; i++) { // +1 for ocel_time
                insertEventType.append(", ?");
            }
            insertEventType.append(")");

            try (PreparedStatement pstmt = conn.prepareStatement(insertEventType.toString())) {
                int idx = 1;
                pstmt.setString(idx++, event.id);
                pstmt.setString(idx++, formatTimestamp(event.timestamp));
                for (String attr : attrNames) {
                    Object value = event.attributes.get(attr);
                    pstmt.setString(idx++, value != null ? value.toString() : null);
                }
                pstmt.executeUpdate();
            }
        }

        // Insert into object and object_<objectType> tables
        for (OcelObject object : eventLog.objects.values()) {
            // Insert into object table
            String insertObject = "INSERT INTO object (ocel_id, ocel_type) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertObject)) {
                pstmt.setString(1, object.id);
                pstmt.setString(2, object.objectType.name);
                pstmt.executeUpdate();
            }

            // Insert into object_<objectType> table
            String objectTypeMapName = objectTypeMap.get(object.objectType.name);
            // Prepare initial attribute values
            StringBuilder insertObjectType = new StringBuilder("INSERT INTO object_" + objectTypeMapName + " (ocel_id, ocel_time, ocel_changed_field");
            List<String> attrNames = new ArrayList<>(object.attributes.keySet());
            for (String attr : attrNames) {
                insertObjectType.append(", ").append(attr);
            }
            insertObjectType.append(") VALUES (?");
            for (int i = 0; i < attrNames.size() + 2; i++) { // +2 for ocel_time, ocel_changed_field
                insertObjectType.append(", ?");
            }
            insertObjectType.append(")");

            try (PreparedStatement pstmt = conn.prepareStatement(insertObjectType.toString())) {
                int idx = 1;
                pstmt.setString(idx++, object.id);
                pstmt.setString(idx++, "1970-01-01 00:00:00");
                pstmt.setString(idx++, null); // ocel_changed_field is null
                for (String attr : attrNames) {
                    Object value = object.attributes.get(attr);
                    pstmt.setString(idx++, value != null ? value.toString() : null);
                }
                pstmt.executeUpdate();
            }

            // Insert timed attributes
            for (String attr : object.timedAttributes.keySet()) {
                Map<Date, Object> changes = object.timedAttributes.get(attr);
                for (Map.Entry<Date, Object> changeEntry : changes.entrySet()) {
                    Date timestamp = changeEntry.getKey();
                    Object value = changeEntry.getValue();
                    StringBuilder insertTimedAttr = new StringBuilder("INSERT INTO object_" + objectTypeMapName +
                            " (ocel_id, ocel_time, ocel_changed_field, " + attr + ") VALUES (?, ?, ?, ?)");
                    try (PreparedStatement pstmt = conn.prepareStatement(insertTimedAttr.toString())) {
                        pstmt.setString(1, object.id);
                        pstmt.setString(2, formatTimestamp(timestamp));
                        pstmt.setString(3, attr);
                        pstmt.setString(4, value != null ? value.toString() : null);
                        pstmt.executeUpdate();
                    }
                }
            }
        }

        // Insert into event_object table
        for (OcelEvent event : eventLog.events.values()) {
            for (Map.Entry<OcelObject, String> entry : event.relatedObjects.entrySet()) {
                OcelObject object = entry.getKey();
                String qualifier = entry.getValue();
                String insertEventObject = "INSERT INTO event_object (ocel_event_id, ocel_object_id, ocel_qualifier) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertEventObject)) {
                    pstmt.setString(1, event.id);
                    pstmt.setString(2, object.id);
                    pstmt.setString(3, qualifier != null ? qualifier : "");
                    pstmt.executeUpdate();
                }
            }
        }

        for (OcelObject object : eventLog.objects.values()) {
            for (Map.Entry<String, String> entry : object.relatedObjectIdentifiers.entrySet()) {
                OcelObject targetObject = eventLog.objects.get(entry.getKey());
                if (targetObject != null) {
	                String qualifier = entry.getValue();
	                String insertObjectObject = "INSERT INTO object_object (ocel_source_id, ocel_target_id, ocel_qualifier) VALUES (?, ?, ?)";
	                try (PreparedStatement pstmt = conn.prepareStatement(insertObjectObject)) {
	                    pstmt.setString(1, object.id);
	                    pstmt.setString(2, targetObject.id);
	                    pstmt.setString(3, qualifier);
	                    pstmt.executeUpdate();
	                }
                }
            }
        }
    }

    private String sanitizeName(String name) {
        // Replace spaces and special characters to make a valid table name
        return name.replaceAll("[^a-zA-Z0-9_]", "");
    }

    private String formatTimestamp(Date timestamp) {
        // Format date as "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }
}
