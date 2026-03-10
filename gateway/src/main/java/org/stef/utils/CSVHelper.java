package org.stef.utils;

import org.stef.dto.OpportunityDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


public class CSVHelper {

    private CSVHelper() {
        // Private constructor to prevent instantiation
    }
    public static ByteArrayInputStream opportunitiesToCSV(List<OpportunityDTO> opportunities) {
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("proposalId", "customer", "priceTonne", "tonnes", "country", "proposalValidityDays")
                .build();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
            for (OpportunityDTO opp : opportunities) {
                List<String> data = Arrays.asList(
                        String.valueOf(opp.proposalId()),
                        opp.customer(),
                        String.valueOf(opp.priceTonne()),
                        String.valueOf(opp.lastCurrencyQuotation())
                );
                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to import data to CSV file: " + e.getMessage());
        }
    }
}
