package com.dair.cais.organization.util;

import com.dair.cais.organization.OrganizationUnit;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CsvHelper {

    public static String TYPE = "text/csv";
    static String[] HEADERS = { "type", "orgKey", "orgName", "orgDescription", "parentOrgKey", "isActive" };

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
                if (values.length < 6) {
                    log.warn("Skipping invalid line: " + line);
                    continue;
                }

                OrganizationUnit organizationUnit = new OrganizationUnit();
                organizationUnit.setType(values[0]);
                organizationUnit.setOrgKey(values[1]);
                organizationUnit.setOrgName(values[2]);
                organizationUnit.setOrgDescription(values[3]);
                organizationUnit.setParentOrgKey(values[4].isEmpty() ? null : values[4]);
                organizationUnit.setIsActive(values[5].isEmpty() ? null : Boolean.parseBoolean(values[5]));

                log.debug("Parsed organization unit: {}", organizationUnit);
                organizationUnits.add(organizationUnit);
            }

            return organizationUnits;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}