package com.dair.cais.reports.tabs.exception;

/**
 * Exception thrown when a tab is not found
 */
public class TabNotFoundException extends RuntimeException {
    public TabNotFoundException(Integer tabId) {
        super("Tab not found with ID: " + tabId);
    }

    public TabNotFoundException(String tabIdentifier) {
        super("Tab not found with identifier: " + tabIdentifier);
    }
}
