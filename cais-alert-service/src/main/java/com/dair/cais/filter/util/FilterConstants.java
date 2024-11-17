package com.dair.cais.filter.util;

public final class FilterConstants {
    private FilterConstants() {}

    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final String DEFAULT_SORT_FIELD = "updatedAt";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final class ErrorMessages {
        private ErrorMessages() {}

        public static final String FILTER_NOT_FOUND = "Filter not found";
        public static final String INVALID_FILTER_CONFIG = "Invalid filter configuration";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access to filter";
        public static final String DUPLICATE_FILTER_NAME = "Filter name already exists";
    }

    public static final class CacheKeys {
        private CacheKeys() {}

        public static final String USER_FILTERS = "user_filters";
        public static final String PUBLIC_FILTERS = "public_filters";
        public static final String FILTER_TYPES = "filter_types";
    }
}