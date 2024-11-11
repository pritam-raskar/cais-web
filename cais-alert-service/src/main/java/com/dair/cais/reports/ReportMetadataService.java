package com.dair.cais.reports;

import com.dair.cais.connection.ConnectionService;
import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.exception.MetadataException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportMetadataService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ConnectionService connectionService;


    @Transactional(readOnly = true)
    public List<TableMetadataDto> getTableMetadata(Long connectionId) {
        log.debug("Fetching table metadata for connection: {}", connectionId);

        try {
            String sql = """
            SELECT 
                t.table_schema,
                t.table_name,
                t.table_type,
                pg_class.reltuples::bigint as approximate_row_count,
                obj_description(pg_class.oid) as description,
                (SELECT COUNT(*) FROM information_schema.columns c
                 WHERE c.table_schema = t.table_schema
                 AND c.table_name = t.table_name) as column_count
            FROM information_schema.tables t
            JOIN pg_class ON pg_class.relname = t.table_name
            JOIN pg_namespace ON pg_namespace.nspname = t.table_schema
                AND pg_namespace.oid = pg_class.relnamespace
            WHERE t.table_schema NOT IN ('pg_catalog', 'information_schema')
            AND t.table_type IN ('BASE TABLE', 'VIEW')
            ORDER BY t.table_schema, t.table_name
        """;

            List<Map<String, Object>> results = connectionService.getConnectionAndTestQuery(connectionId, sql);

            List<TableMetadataDto> dtoList = new ArrayList<>();
            for (Map<String, Object> row : results) {
                TableMetadataDto dto = new TableMetadataDto();
                dto.setTableSchema(convertToString(row.get("table_schema")));
                dto.setTableName(convertToString(row.get("table_name")));
                dto.setTableType(convertToString(row.get("table_type")));
                dto.setDescription(convertToString(row.get("description")));
                dto.setApproximateRowCount(convertToLong(row.get("approximate_row_count")));
                dto.setColumnCount(convertToInteger(row.get("column_count")));
                dtoList.add(dto);
            }

            return dtoList;

        } catch (Exception e) {
            log.error("Error fetching table metadata for connection {}: {}", connectionId, e.getMessage());
            throw new MetadataException("Failed to fetch table metadata", e);
        }
    }

    // Helper methods for safe type conversion
    private String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Long convertToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to convert value to Long: {}", value);
            return null;
        }
    }

    private Integer convertToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to convert value to Integer: {}", value);
            return null;
        }
    }




//    @Transactional(readOnly = true)
//    public List<ColumnMetadataDto> getColumnMetadata(Long connectionId, String tableSchema,  String tableName  ) {
//        log.debug("Fetching column metadata for table: {} in connection: {}", tableName, connectionId);
//
//        try {
//            String sql = """
//                SELECT
//                    c.column_name,
//                    c.data_type,
//                    c.column_default,
//                    c.is_nullable,
//                    c.character_maximum_length,
//                    c.numeric_precision,
//                    c.numeric_scale,
//                    c.ordinal_position,
//                    c.udt_name,
//                    pg_catalog.col_description(pgc.oid, c.ordinal_position) as column_description,
//                    CASE
//                        WHEN pk.column_name IS NOT NULL THEN 'true'
//                        ELSE 'false'
//                    END as is_primary_key,
//                    CASE
//                        WHEN fk.column_name IS NOT NULL THEN 'true'
//                        ELSE 'false'
//                    END as is_foreign_key,
//                    fk.foreign_table_name,
//                    fk.foreign_column_name
//                FROM information_schema.columns c
//                JOIN pg_class pgc ON pgc.relname = c.table_name
//                LEFT JOIN (
//                    SELECT ku.column_name
//                    FROM information_schema.table_constraints tc
//                    JOIN information_schema.key_column_usage ku
//                        ON tc.constraint_name = ku.constraint_name
//                    WHERE tc.constraint_type = 'PRIMARY KEY'
//                        AND ku.table_name = ?
//                ) pk ON pk.column_name = c.column_name
//                LEFT JOIN (
//                    SELECT
//                        kcu.column_name,
//                        ccu.table_name AS foreign_table_name,
//                        ccu.column_name AS foreign_column_name
//                    FROM information_schema.table_constraints tc
//                    JOIN information_schema.key_column_usage kcu
//                        ON tc.constraint_name = kcu.constraint_name
//                    JOIN information_schema.constraint_column_usage ccu
//                        ON ccu.constraint_name = tc.constraint_name
//                    WHERE tc.constraint_type = 'FOREIGN KEY'
//                        AND kcu.table_name = ?
//                ) fk ON fk.column_name = c.column_name
//                WHERE c.table_name = ?
//                ORDER BY c.ordinal_position
//            """;
//
//            return jdbcTemplate.query(sql,
//                    (rs, rowNum) -> {
//                        ColumnMetadataDto dto = new ColumnMetadataDto();
//                        dto.setColumnName(rs.getString("column_name"));
//                        dto.setDataType(rs.getString("data_type"));
//                        dto.setDefaultValue(rs.getString("column_default"));
//                        dto.setNullable("YES".equals(rs.getString("is_nullable")));
//                        dto.setMaxLength(rs.getObject("character_maximum_length", Integer.class));
//                        dto.setPrecision(rs.getObject("numeric_precision", Integer.class));
//                        dto.setScale(rs.getObject("numeric_scale", Integer.class));
//                        dto.setOrdinalPosition(rs.getInt("ordinal_position"));
//                        dto.setDescription(rs.getString("column_description"));
//                        dto.setIsPrimaryKey("true".equals(rs.getString("is_primary_key")));
//                        dto.setIsForeignKey("true".equals(rs.getString("is_foreign_key")));
//                        dto.setForeignTableName(rs.getString("foreign_table_name"));
//                        dto.setForeignColumnName(rs.getString("foreign_column_name"));
//
//                        // Set suggested formatting
//                        dto.setSuggestedFormatting(getFormattingSuggestions(rs.getString("data_type")));
//
//                        return dto;
//                    },
//                    tableName, tableName, tableName);
//        } catch (Exception e) {
//            log.error("Error fetching column metadata: {}", e.getMessage());
//            throw new MetadataException("Failed to fetch column metadata", e);
//        }
//    }

    @Transactional(readOnly = true)
    public List<ColumnMetadataDto> getColumnMetadata(Long connectionId, String schemaName, String tableName) {
        log.debug("Fetching column metadata for table: {}.{} in connection: {}", schemaName, tableName, connectionId);
        try {
            String sql = """
            SELECT 
                c.column_name,
                c.data_type,
                c.column_default,
                c.is_nullable,
                c.character_maximum_length,
                c.numeric_precision,
                c.numeric_scale,
                c.ordinal_position,
                c.udt_name,
                pg_catalog.col_description(pgc.oid, c.ordinal_position) as column_description,
                CASE 
                    WHEN pk.column_name IS NOT NULL THEN 'true'
                    ELSE 'false'
                END as is_primary_key,
                CASE 
                    WHEN fk.column_name IS NOT NULL THEN 'true'
                    ELSE 'false'
                END as is_foreign_key,
                fk.foreign_table_name,
                fk.foreign_column_name,
                fk.foreign_table_schema
            FROM information_schema.columns c
            JOIN pg_class pgc 
                ON pgc.relname = c.table_name 
                AND c.table_schema = '%s'
            JOIN pg_namespace pgn 
                ON pgn.oid = pgc.relnamespace 
                AND pgn.nspname = c.table_schema
            LEFT JOIN (
                SELECT 
                    ku.column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage ku
                    ON tc.constraint_name = ku.constraint_name
                WHERE tc.constraint_type = 'PRIMARY KEY'
                    AND ku.table_schema = '%s'
                    AND ku.table_name = '%s'
            ) pk ON pk.column_name = c.column_name
            LEFT JOIN (
                SELECT 
                    kcu.column_name,
                    ccu.table_schema AS foreign_table_schema,
                    ccu.table_name AS foreign_table_name,
                    ccu.column_name AS foreign_column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                    ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage ccu
                    ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'FOREIGN KEY'
                    AND kcu.table_schema = '%s'
                    AND kcu.table_name = '%s'
            ) fk ON fk.column_name = c.column_name
            WHERE c.table_schema = '%s'
                AND c.table_name = '%s'
            ORDER BY c.ordinal_position
        """.formatted(
                    schemaName,
                    schemaName, tableName,
                    schemaName, tableName,
                    schemaName, tableName
            );

            List<Map<String, Object>> results = connectionService.getConnectionAndTestQuery(connectionId, sql);

            return results.stream()
                    .map(this::mapToColumnMetadataDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching column metadata for table {}.{} in connection {}: {}",
                    schemaName, tableName, connectionId, e.getMessage());
            throw new MetadataException("Failed to fetch column metadata", e);
        }
    }

    private ColumnMetadataDto mapToColumnMetadataDto(Map<String, Object> row) {
        ColumnMetadataDto dto = new ColumnMetadataDto();
        try {
            dto.setColumnName(convertToString(row.get("column_name")));
            dto.setDataType(convertToString(row.get("data_type")));
            dto.setDefaultValue(convertToString(row.get("column_default")));
            dto.setNullable("YES".equalsIgnoreCase(convertToString(row.get("is_nullable"))));
            dto.setMaxLength(convertToInteger(row.get("character_maximum_length")));
            dto.setPrecision(convertToInteger(row.get("numeric_precision")));
            dto.setScale(convertToInteger(row.get("numeric_scale")));
            dto.setOrdinalPosition(convertToInteger(row.get("ordinal_position")));
            dto.setDescription(convertToString(row.get("column_description")));
            dto.setIsPrimaryKey("true".equalsIgnoreCase(convertToString(row.get("is_primary_key"))));
            dto.setIsForeignKey("true".equalsIgnoreCase(convertToString(row.get("is_foreign_key"))));
            dto.setForeignTableName(convertToString(row.get("foreign_table_name")));
            dto.setForeignColumnName(convertToString(row.get("foreign_column_name")));


            // Set suggested formatting based on data type
            dto.setSuggestedFormatting(getFormattingSuggestions(convertToString(row.get("data_type"))));

            return dto;
        } catch (Exception e) {
            log.warn("Error mapping column metadata for column {}: {}",
                    row.get("column_name"), e.getMessage());
            throw new MetadataException("Error mapping column metadata", e);
        }
    }

    @Transactional
    public void updateColumnMetadata(Long connectionId, String tableName,
                                     List<ColumnMetadataUpdateDto> updates) {
        log.debug("Updating column metadata for table: {} in connection: {}", tableName, connectionId);

        try {
            for (ColumnMetadataUpdateDto update : updates) {
                String sql = """
                    COMMENT ON COLUMN %s.%s IS ?
                """.formatted(tableName, update.getColumnName());

                jdbcTemplate.update(sql, update.getDescription());
                log.debug("Updated metadata for column: {}", update.getColumnName());
            }
        } catch (Exception e) {
            log.error("Error updating column metadata: {}", e.getMessage());
            throw new MetadataException("Failed to update column metadata", e);
        }
    }

    /**
     * Returns formatting suggestions based on column data type
     */
    public ColumnFormattingDto getFormattingSuggestions(String dataType) {
        log.debug("Getting formatting suggestions for data type: {}", dataType);

        ColumnFormattingDto formatting = new ColumnFormattingDto();

        if (dataType == null) {
            return formatting;
        }

        switch (dataType.toLowerCase()) {
            case "integer", "bigint", "smallint" -> {
                formatting.setAlignment("right");
                formatting.setFormat("#,##0");
                formatting.setUseThousandsSeparator(true);
                formatting.setDecimalPlaces(0);
            }
            case "decimal", "numeric", "double precision", "real" -> {
                formatting.setAlignment("right");
                formatting.setFormat("#,##0.00");
                formatting.setUseThousandsSeparator(true);
                formatting.setDecimalPlaces(2);
            }
            case "money" -> {
                formatting.setAlignment("right");
                formatting.setFormat("#,##0.00");
                formatting.setUseThousandsSeparator(true);
                formatting.setDecimalPlaces(2);
                formatting.setPrefix("$");
            }
            case "timestamp", "timestamptz" -> {
                formatting.setAlignment("left");
                formatting.setFormat("yyyy-MM-dd HH:mm:ss");
                formatting.setDisplayFormat("Timestamp");
            }
            case "date" -> {
                formatting.setAlignment("left");
                formatting.setFormat("yyyy-MM-dd");
                formatting.setDisplayFormat("Date");
            }
            case "time", "timetz" -> {
                formatting.setAlignment("left");
                formatting.setFormat("HH:mm:ss");
                formatting.setDisplayFormat("Time");
            }
            case "boolean" -> {
                formatting.setAlignment("center");
                formatting.setDisplayFormat("Boolean");
            }
            case "json", "jsonb" -> {
                formatting.setAlignment("left");
                formatting.setDisplayFormat("JSON");
            }
            default -> {
                formatting.setAlignment("left");
                formatting.setDisplayFormat("Text");
            }
        }

        return formatting;
    }

    /**
     * Gets all schemas for a connection
     */
    @Cacheable(value = "schemaMetadata", key = "#connectionId")
    @Transactional(readOnly = true)
    public List<DatabaseSchemaDto> getDatabaseSchemas(Long connectionId) {
        log.debug("Fetching database schemas for connection: {}", connectionId);

        try {
            String sql = """
                SELECT 
                    n.nspname as schema_name,
                    d.description,
                    COUNT(CASE WHEN t.table_type = 'BASE TABLE' THEN 1 END) as table_count,
                    COUNT(CASE WHEN t.table_type = 'VIEW' THEN 1 END) as view_count
                FROM pg_namespace n
                LEFT JOIN pg_description d ON d.objoid = n.oid
                LEFT JOIN information_schema.tables t 
                    ON t.table_schema = n.nspname
                WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
                GROUP BY n.nspname, d.description
                ORDER BY n.nspname
            """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                DatabaseSchemaDto dto = new DatabaseSchemaDto();
                dto.setSchemaName(rs.getString("schema_name"));
                dto.setDescription(rs.getString("description"));
                dto.setTableCount(rs.getInt("table_count"));
                dto.setViewCount(rs.getInt("view_count"));
                return dto;
            });
        } catch (Exception e) {
            log.error("Error fetching database schemas for connection {}: {}", connectionId, e.getMessage());
            throw new MetadataException("Failed to fetch database schemas", e);
        }
    }


    /**
     * Gets detailed information about a specific schema
     */
    @Transactional(readOnly = true)
    public DatabaseSchemaDto getSchemaDetails(Long connectionId, String schemaName) {
        log.debug("Fetching schema details for: {} in connection: {}", schemaName, connectionId);

        try {
            String sql = """
            SELECT 
                n.nspname as schema_name,
                d.description,
                pg_size_pretty(sum(pg_total_relation_size(c.oid))) as total_size,
                COUNT(CASE WHEN c.relkind = 'r' THEN 1 END) as table_count,
                COUNT(CASE WHEN c.relkind = 'v' THEN 1 END) as view_count
            FROM pg_namespace n
            LEFT JOIN pg_description d ON d.objoid = n.oid
            LEFT JOIN pg_class c ON c.relnamespace = n.oid
            WHERE n.nspname = ?
            GROUP BY n.nspname, d.description
        """;

            DatabaseSchemaDto schema = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                DatabaseSchemaDto dto = new DatabaseSchemaDto();
                dto.setSchemaName(rs.getString("schema_name"));
                dto.setDescription(rs.getString("description"));
                dto.setTableCount(rs.getInt("table_count"));
                dto.setViewCount(rs.getInt("view_count"));
                return dto;
            }, schemaName);

            if (schema != null) {
                // Get tables and views for this schema
                schema.setTables(getTablesForSchema(connectionId, schemaName, "BASE TABLE"));
                schema.setViews(getTablesForSchema(connectionId, schemaName, "VIEW"));
            }

            return schema;
        } catch (Exception e) {
            log.error("Error fetching schema details: {}", e.getMessage());
            throw new MetadataException("Failed to fetch schema details", e);
        }
    }

    private List<TableMetadataDto> getTablesForSchema(Long connectionId, String schemaName, String tableType) {
        String sql = """
        SELECT 
            table_name,
            obj_description(pgc.oid, 'pg_class') as description,
            pgc.reltuples::bigint as approximate_row_count
        FROM information_schema.tables t
        JOIN pg_class pgc ON pgc.relname = t.table_name
        WHERE table_schema = ?
        AND table_type = ?
        ORDER BY table_name
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TableMetadataDto dto = new TableMetadataDto();
            dto.setTableSchema(schemaName);
            dto.setTableName(rs.getString("table_name"));
            dto.setTableType(tableType);
            dto.setDescription(rs.getString("description"));
            dto.setApproximateRowCount(rs.getLong("approximate_row_count"));
            return dto;
        }, schemaName, tableType);
    }
}