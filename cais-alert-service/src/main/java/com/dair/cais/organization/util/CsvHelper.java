package com.dair.cais.organization.util;

import com.dair.cais.organization.OrganizationUnit;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvHelper {

    public static String TYPE = "text/csv";
    static String[] HEADERS = { "type", "orgKey", "orgName", "orgDescription", "isActive" };

    public boolean hasCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public List<OrganizationUnit> csvToOrganizationUnits(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            List<OrganizationUnit> organizationUnits = new ArrayList<>();
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip the header line
                }

                String[] values = line.split(",");
                OrganizationUnit organizationUnit = new OrganizationUnit();
                organizationUnit.setType(values[0]);
                organizationUnit.setOrgKey(values[1]);
                organizationUnit.setOrgName(values[2]);
                organizationUnit.setOrgDescription(values[3]);
                organizationUnit.setIsActive(Boolean.parseBoolean(values[4]));

                organizationUnits.add(organizationUnit);
            }

            return organizationUnits;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}