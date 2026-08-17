package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IngestionServiceApp {

    private static final String CSV_FILE = "/wards-outdated.csv";

    public static void main(String[] args) throws IOException {
        List< WardRecord> wards =  loadWards();
        
        Javalin app = Javalin.create().start(7030);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/wards", ctx -> ctx.json(wards));

        // TODO: read and clean src/main/resources/wards-outdated.csv (wards, wings, specialist departments data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
    }

    private static List<WardRecord> loadWards() throws IOException{
        List<WardRecord> wards = new ArrayList<>();
        try(InputStream inputStream = IngestionServiceApp.class.getResourceAsStream(CSV_FILE)){
            if(inputStream == null){
                throw new IllegalStateException("Could not find" + CSV_FILE);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            reader.readLine();
            String line ;

            while((line = reader.readLine()) != null){
                if(line.trim().isEmpty()){
                    continue;
                }
                WardRecord ward = cleanRow(line);

                if(ward != null){
                    addOrHandleDuplicate(wards , ward);
                }
            }

        } catch (Exception e){
            throw new RuntimeException("Failed to read ward data", e);
        }
        return wards;
    }

    private static WardRecord cleanRow(String line){
        String[] fields = line.split("," ,-1);
        if(fields.length != 4){
            return null;
        }
        String WardId = cleanWardId(fields[0]);
        String wing = cleanWing(fields[1]);
        String department = cleanDepartment(fields[2]);

        BedResult bedResult = cleanBeds(fields[3]);

        return new WardRecord(WardId, wing, department, bedResult.bedsAvailable(), bedResult.notes());
            
    }

    private static String cleanWardId(String value) {

        return value.trim().toUpperCase();
    }

    private static String cleanWing(String value) {

        String cleaned = value.trim().replaceAll("\\s+", " ");

        if (cleaned.isEmpty()) {
            return null;
        }

        String[] words = cleaned.toLowerCase().split(" ");

        StringBuilder result = new StringBuilder();

        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(
                    Character.toUpperCase(word.charAt(0))
            );
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }
         return result.toString();
    }

    private static String cleanDepartment(String value) {

        String cleaned = value
                .trim()
                .replaceAll("\\s+", " ");

        if (cleaned.isEmpty()) {
            return null;
        }

        if (cleaned.equalsIgnoreCase("pediatrics")
                || cleaned.equalsIgnoreCase("paediatrics")) {
            return "Paediatrics";
        }

        if (cleaned.equalsIgnoreCase("icu")) {
            return "ICU";
        }

        return capitalizeFirstLetter(cleaned);
    }

    private static String capitalizeFirstLetter(String value) {

        String lower = value.toLowerCase();

        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }


    private static BedResult cleanBeds(String value) {

        String original = value.trim();

        if (original.isEmpty()) {
            return new BedResult(
                    null,
                    "bedsAvailable was missing — flagged for follow-up"
            );
        }

        if (original.equalsIgnoreCase("N/A")
                || original.equalsIgnoreCase("TBD")
                || original.equalsIgnoreCase("unknown")
                || original.equalsIgnoreCase("NaN")) {

            return new BedResult(
                    null,
                    "bedsAvailable was unavailable ('"
                            + original
                            + "') — flagged for follow-up"
            );
        }
        try {

            int beds = Integer.parseInt(original);

            if (beds < 0) {
                return new BedResult(
                        null,
                        "bedsAvailable was negative ('"
                                + original
                                + "') — flagged for follow-up"
                );
            }

            // The dataset contains 2023, which is clearly unrealistic.
            if (beds > 100) {
                return new BedResult(
                        null,
                        "bedsAvailable was unrealistic ('"
                                + original
                                + "') — flagged for follow-up"
                );
            }

            return new BedResult(beds, null);
        } catch (NumberFormatException e) {

            return new BedResult(
                    null,
                    "bedsAvailable was non-numeric ('"
                            + original
                            + "') — flagged for follow-up"
            );
        }
    }
    
    private static void addOrHandleDuplicate(
            List<WardRecord> wards,
            WardRecord newWard) {

        for (WardRecord existingWard : wards) {

            if (existingWard.getWardId()
                    .equalsIgnoreCase(newWard.getWardId())) {

                // Keep the existing valid record.
                // Add a note indicating that a duplicate was found.

                String existingNotes = existingWard.getNotes();

                String duplicateNote =
                        "Duplicate record detected for ward "
                                + newWard.getWardId();

                if (existingNotes == null
                        || existingNotes.isBlank()) {

                    existingWard.setNotes(duplicateNote);

                } else {

                    existingWard.setNotes(
                            existingNotes + "; " + duplicateNote
                    );
                }

                return;
            }
        }
        wards.add(newWard);
    }

     private record BedResult(
            Integer bedsAvailable,
            String notes) {
    }
}






  

