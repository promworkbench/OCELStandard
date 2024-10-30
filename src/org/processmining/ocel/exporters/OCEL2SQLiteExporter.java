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

@Plugin(name = "Export OCEL 2.0 to SQLite file (optimized)", parameterLabels = { "OcelEventLog", "File" }, returnLabels = {}, returnTypes = {})
@UIExportPlugin(description = "Export OCEL 2.0 to SQLite file (optimized)", extension = "sqlite")
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
        conn.setAutoCommit(false); // Start transaction

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

        // Prepare statements for event_map_type
        String insertEventMapTypeSQL = "INSERT INTO event_map_type (ocel_type, ocel_type_map) VALUES (?, ?)";
        PreparedStatement insertEventMapTypeStmt = conn.prepareStatement(insertEventMapTypeSQL);

        // Prepare statements for object_map_type
        String insertObjectMapTypeSQL = "INSERT INTO object_map_type (ocel_type, ocel_type_map) VALUES (?, ?)";
        PreparedStatement insertObjectMapTypeStmt = conn.prepareStatement(insertObjectMapTypeSQL);

        // Maps to store prepared statements and attribute names for event and object types
        Map<String, PreparedStatement> eventTypeInsertStmts = new HashMap<>();
        Map<String, List<String>> eventTypeAttrNames = new HashMap<>();

        Map<String, PreparedStatement> objectTypeInsertStmts = new HashMap<>();
        Map<String, List<String>> objectTypeAttrNames = new HashMap<>();

        // Create event_map_type and event type tables
        for (String eventType : eventTypes) {
            String eventTypeMapName = sanitizeName(eventType);
            eventTypeMap.put(eventType, eventTypeMapName);

            // Insert into event_map_type
            insertEventMapTypeStmt.setString(1, eventType);
            insertEventMapTypeStmt.setString(2, eventTypeMapName);
            insertEventMapTypeStmt.addBatch();

            // Collect attribute names for this event type
            Set<String> attributeNames = new HashSet<>();
            for (OcelEvent event : eventLog.events.values()) {
                if (event.activity.equals(eventType)) {
                    attributeNames.addAll(event.attributes.keySet());
                }
            }
            List<String> attrNames = new ArrayList<>(attributeNames);
            eventTypeAttrNames.put(eventType, attrNames);

            // Create table with columns: ocel_id, ocel_time, and attributes
            StringBuilder createEventTypeTable = new StringBuilder("CREATE TABLE event_" + eventTypeMapName + " (" +
                    "ocel_id TEXT PRIMARY KEY," +
                    "ocel_time TEXT");

            for (String attr : attrNames) {
                createEventTypeTable.append(", ").append(attr).append(" TEXT");
            }
            createEventTypeTable.append(", FOREIGN KEY(ocel_id) REFERENCES event(ocel_id))");

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createEventTypeTable.toString());
            }

            // Prepare insert statement for this event type
            StringBuilder insertEventTypeSQL = new StringBuilder("INSERT INTO event_" + eventTypeMapName + " (ocel_id, ocel_time");
            for (String attr : attrNames) {
                insertEventTypeSQL.append(", ").append(attr);
            }
            insertEventTypeSQL.append(") VALUES (?");
            for (int i = 0; i < attrNames.size() + 1; i++) { // +1 for ocel_time
                insertEventTypeSQL.append(", ?");
            }
            insertEventTypeSQL.append(")");

            PreparedStatement pstmt = conn.prepareStatement(insertEventTypeSQL.toString());
            eventTypeInsertStmts.put(eventType, pstmt);
        }

        insertEventMapTypeStmt.executeBatch();
        insertEventMapTypeStmt.close();

        // Create object_map_type and object type tables
        for (String objectType : objectTypes) {
            String objectTypeMapName = sanitizeName(objectType);
            objectTypeMap.put(objectType, objectTypeMapName);

            // Insert into object_map_type
            insertObjectMapTypeStmt.setString(1, objectType);
            insertObjectMapTypeStmt.setString(2, objectTypeMapName);
            insertObjectMapTypeStmt.addBatch();

            // Collect attribute names for this object type
            Set<String> attributeNames = new HashSet<>();
            for (OcelObject object : eventLog.objects.values()) {
                if (object.objectType.name.equals(objectType)) {
                    attributeNames.addAll(object.attributes.keySet());
                    attributeNames.addAll(object.timedAttributes.keySet());
                }
            }
            List<String> attrNames = new ArrayList<>(attributeNames);
            objectTypeAttrNames.put(objectType, attrNames);

            // Create table with columns: ocel_id, ocel_time, ocel_changed_field, and attributes
            StringBuilder createObjectTypeTable = new StringBuilder("CREATE TABLE object_" + objectTypeMapName + " (" +
                    "ocel_id TEXT," +
                    "ocel_time TEXT," +
                    "ocel_changed_field TEXT");

            for (String attr : attrNames) {
                createObjectTypeTable.append(", ").append(attr).append(" TEXT");
            }
            createObjectTypeTable.append(", FOREIGN KEY(ocel_id) REFERENCES object(ocel_id))");

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createObjectTypeTable.toString());
            }

            // Prepare insert statement for this object type
            StringBuilder insertObjectTypeSQL = new StringBuilder("INSERT INTO object_" + objectTypeMapName + " (ocel_id, ocel_time, ocel_changed_field");
            for (String attr : attrNames) {
                insertObjectTypeSQL.append(", ").append(attr);
            }
            insertObjectTypeSQL.append(") VALUES (?");
            for (int i = 0; i < attrNames.size() + 2; i++) { // +2 for ocel_time, ocel_changed_field
                insertObjectTypeSQL.append(", ?");
            }
            insertObjectTypeSQL.append(")");

            PreparedStatement pstmt = conn.prepareStatement(insertObjectTypeSQL.toString());
            objectTypeInsertStmts.put(objectType, pstmt);
        }

        insertObjectMapTypeStmt.executeBatch();
        insertObjectMapTypeStmt.close();

        // Insert into event and per-event-type tables
        String insertEventSQL = "INSERT INTO event (ocel_id, ocel_type) VALUES (?, ?)";
        PreparedStatement insertEventStmt = conn.prepareStatement(insertEventSQL);

        for (OcelEvent event : eventLog.events.values()) {
            // Insert into event table
            insertEventStmt.setString(1, event.id);
            insertEventStmt.setString(2, event.activity);
            insertEventStmt.addBatch();

            // Insert into per-event-type table
            String eventTypeMapName = eventTypeMap.get(event.activity);
            PreparedStatement pstmt = eventTypeInsertStmts.get(event.activity);
            int idx = 1;
            pstmt.setString(idx++, event.id);
            pstmt.setString(idx++, formatTimestamp(event.timestamp));
            List<String> attrNames = eventTypeAttrNames.get(event.activity);
            for (String attr : attrNames) {
                Object value = event.attributes.get(attr);
                pstmt.setString(idx++, value != null ? value.toString() : null);
            }
            pstmt.addBatch();
        }

        insertEventStmt.executeBatch();
        insertEventStmt.close();

        // Execute batch inserts for per-event-type tables
        for (PreparedStatement pstmt : eventTypeInsertStmts.values()) {
            pstmt.executeBatch();
            pstmt.close();
        }

        // Insert into object and per-object-type tables
        String insertObjectSQL = "INSERT INTO object (ocel_id, ocel_type) VALUES (?, ?)";
        PreparedStatement insertObjectStmt = conn.prepareStatement(insertObjectSQL);

        for (OcelObject object : eventLog.objects.values()) {
            // Insert into object table
            insertObjectStmt.setString(1, object.id);
            insertObjectStmt.setString(2, object.objectType.name);
            insertObjectStmt.addBatch();

            // Insert into per-object-type table
            String objectTypeMapName = objectTypeMap.get(object.objectType.name);
            PreparedStatement pstmt = objectTypeInsertStmts.get(object.objectType.name);
            int idx = 1;
            pstmt.setString(idx++, object.id);
            pstmt.setString(idx++, "1970-01-01 00:00:00");
            pstmt.setString(idx++, null); // ocel_changed_field is null
            List<String> attrNames = objectTypeAttrNames.get(object.objectType.name);
            for (String attr : attrNames) {
                Object value = object.attributes.get(attr);
                pstmt.setString(idx++, value != null ? value.toString() : null);
            }
            pstmt.addBatch();

            // Insert timed attributes
            for (String attr : object.timedAttributes.keySet()) {
                Map<Date, Object> changes = object.timedAttributes.get(attr);
                for (Map.Entry<Date, Object> changeEntry : changes.entrySet()) {
                    Date timestamp = changeEntry.getKey();
                    Object value = changeEntry.getValue();
                    idx = 1;
                    pstmt.setString(idx++, object.id);
                    pstmt.setString(idx++, formatTimestamp(timestamp));
                    pstmt.setString(idx++, attr);
                    for (String a : attrNames) {
                        if (a.equals(attr)) {
                            pstmt.setString(idx++, value != null ? value.toString() : null);
                        } else {
                            pstmt.setString(idx++, null);
                        }
                    }
                    pstmt.addBatch();
                }
            }
        }

        insertObjectStmt.executeBatch();
        insertObjectStmt.close();

        // Execute batch inserts for per-object-type tables
        for (PreparedStatement pstmt : objectTypeInsertStmts.values()) {
            pstmt.executeBatch();
            pstmt.close();
        }

        // Insert into event_object table
        String insertEventObjectSQL = "INSERT INTO event_object (ocel_event_id, ocel_object_id, ocel_qualifier) VALUES (?, ?, ?)";
        PreparedStatement insertEventObjectStmt = conn.prepareStatement(insertEventObjectSQL);

        for (OcelEvent event : eventLog.events.values()) {
            for (Map.Entry<OcelObject, String> entry : event.relatedObjects.entrySet()) {
                OcelObject object = entry.getKey();
                String qualifier = entry.getValue();
                insertEventObjectStmt.setString(1, event.id);
                insertEventObjectStmt.setString(2, object.id);
                insertEventObjectStmt.setString(3, qualifier != null ? qualifier : "");
                insertEventObjectStmt.addBatch();
            }
        }

        insertEventObjectStmt.executeBatch();
        insertEventObjectStmt.close();

        // Insert into object_object table
        String insertObjectObjectSQL = "INSERT INTO object_object (ocel_source_id, ocel_target_id, ocel_qualifier) VALUES (?, ?, ?)";
        PreparedStatement insertObjectObjectStmt = conn.prepareStatement(insertObjectObjectSQL);

        for (OcelObject object : eventLog.objects.values()) {
            for (Map.Entry<String, String> entry : object.relatedObjectIdentifiers.entrySet()) {
                OcelObject targetObject = eventLog.objects.get(entry.getKey());
                if (targetObject != null) {
                    String qualifier = entry.getValue();
                    insertObjectObjectStmt.setString(1, object.id);
                    insertObjectObjectStmt.setString(2, targetObject.id);
                    insertObjectObjectStmt.setString(3, qualifier);
                    insertObjectObjectStmt.addBatch();
                }
            }
        }

        insertObjectObjectStmt.executeBatch();
        insertObjectObjectStmt.close();

        conn.commit(); // Commit transaction
        conn.setAutoCommit(true); // Reset auto-commit to default
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