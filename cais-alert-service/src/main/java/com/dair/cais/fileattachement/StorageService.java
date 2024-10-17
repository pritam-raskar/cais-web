package com.dair.cais.fileattachement;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface StorageService {
    String store(String alertId, String filename, InputStream inputStream) throws IOException;
    List<String> listByAlertId(String alertId) throws IOException;
    byte[] retrieve(String alertId, String filename) throws IOException;
}