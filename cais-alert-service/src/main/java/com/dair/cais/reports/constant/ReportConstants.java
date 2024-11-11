package com.dair.cais.reports.constant;

public class ReportConstants {
    public static class Status {
        public static final String DRAFT = "DRAFT";
        public static final String PUBLISHED = "PUBLISHED";
        public static final String ARCHIVED = "ARCHIVED";
    }

    public static class ReportType {
        public static final String TABLE = "TABLE";
        public static final String CHART = "CHART";
    }

    public static class Alignment {
        public static final String LEFT = "left";
        public static final String CENTER = "center";
        public static final String RIGHT = "right";
        public static final String JUSTIFY = "justify";
    }

    public static class SortDirection {
        public static final String ASC = "ASC";
        public static final String DESC = "DESC";
    }
}