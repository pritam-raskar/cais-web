package com.dair.cais.reports.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper class containing SQL queries for metadata retrieval
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetadataQueries {

    public static final String TABLE_METADATA_QUERY = """
        SELECT 
            t.table_schema,
            t.table_name,
            t.table_type,
            pg_class.reltuples::bigint as approximate_row_count,
            obj_description(pg_class.oid) as description,
            (SELECT COUNT(*) FROM information_schema.columns c 
             WHERE c.table_schema = t.table_schema 
             AND c.table_name = t.table_name) as column_count,
            CASE WHEN t.table_type = 'VIEW' THEN true ELSE false END as is_view,
            CASE WHEN pg_class.reltuples > 0 THEN true ELSE false END as has_data
        FROM information_schema.tables t
        JOIN pg_class ON pg_class.relname = t.table_name
        JOIN pg_namespace ON pg_namespace.nspname = t.table_schema
            AND pg_namespace.oid = pg_class.relnamespace
        WHERE t.table_schema NOT IN ('pg_catalog', 'information_schema')
        AND t.table_type IN ('BASE TABLE', 'VIEW')
        ORDER BY t.table_schema, t.table_name
    """;

    public static final String COLUMN_METADATA_QUERY = """
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
            CASE WHEN pk.column_name IS NOT NULL THEN true ELSE false END as is_primary_key,
            CASE WHEN fk.column_name IS NOT NULL THEN true ELSE false END as is_foreign_key,
            fk.foreign_table_name,
            fk.foreign_column_name
        FROM information_schema.columns c
        JOIN pg_class pgc ON pgc.relname = c.table_name
        LEFT JOIN (
            SELECT ku.column_name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage ku
                ON tc.constraint_name = ku.constraint_name
            WHERE tc.constraint_type = 'PRIMARY KEY'
                AND ku.table_name = ?
        ) pk ON pk.column_name = c.column_name
        LEFT JOIN (
            SELECT
                kcu.column_name,
                ccu.table_name AS foreign_table_name,
                ccu.column_name AS foreign_column_name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
                ON tc.constraint_name = kcu.constraint_name
            JOIN information_schema.constraint_column_usage ccu
                ON ccu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
                AND kcu.table_name = ?
        ) fk ON fk.column_name = c.column_name
        WHERE c.table_name = ?
        ORDER BY c.ordinal_position
    """;
}