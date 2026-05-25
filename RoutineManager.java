package src;

import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RoutineManager {
    private static final String INPUT_FOLDER = "routine_inputs/";
    private static final String DATABASE_FILE = "routines_database.json";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java RoutineManager <action> [arguments]");
            System.out.println("Actions: filter <section_name> | check-notifications");
            return;
        }

        String action = args[0];
        if (action.equalsIgnoreCase("filter")) {
            if (args.length < 2) {
                System.out.println("Please provide a section name.");
                return;
            }
            String sectionName = args[1];
            processAndFilterRoutines(sectionName);
        } else if (action.equalsIgnoreCase("check-notifications")) {
            checkAndTriggerNotifications();
        }
    }

    // Process multiple routine files and filter by section name
    public static void processAndFilterRoutines(String sectionName) {
        System.out.println("Processing routines for Section: " + sectionName);
        File folder = new File(INPUT_FOLDER);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("No routine files found in " + INPUT_FOLDER);
            return;
        }

        List<String> matchedRoutines = new ArrayList<>();

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        // Simulating parsing: format expected "Section,Subject,Code,Time,Day,Teacher"
                        if (line.contains(sectionName)) {
                            matchedRoutines.add(line);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error reading file: " + file.getName());
                }
            }
        }

        saveFilteredRoutines(matchedRoutines);
    }

    private static void saveFilteredRoutines(List<String> routines) {
        try (FileWriter writer = new FileWriter(DATABASE_FILE, false)) {
            writer.write("[\n");
            for (int i = 0; i < routines.size(); i++) {
                writer.write("  \"" + routines.get(i) + "\"");
                if (i < routines.size() - 1) writer.write(",\n");
            }
            writer.write("\n]");
            System.out.println("Successfully saved filtered routines to " + DATABASE_FILE);
        } catch (IOException e) {
            System.out.println("Error saving routines to database.");
        }
    }

    // Check if any class starts in exactly 45 minutes
    public static void checkAndTriggerNotifications() {
        System.out.println("Checking routine schedule...");
        File database = new File(DATABASE_FILE);
        if (!database.exists()) {
            System.out.println("No routine database found. Run 'filter' first.");
            return;
        }

        LocalTime currentTime = LocalTime.now();
        // Format example for class time: "14:30" (24-hour format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try (BufferedReader br = new BufferedReader(new FileReader(database))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[") || line.contains("]")) continue;
                
                // Parsing mock data: "Section,Subject,Code,14:30,Monday,TeacherName"
                String cleanLine = line.replace("\"", "").replace(",", "").trim();
                String[] parts = cleanLine.split(" ");
                
                if (parts.length >= 4) {
                    String classTimeStr = parts[3]; // Assuming index 3 is time
                    try {
                        LocalTime classTime = LocalTime.parse(classTimeStr, formatter);
                        // Check if current time is exactly 45 minutes before class time
                        if (currentTime.plusMinutes(45).getHour() == classTime.getHour() &&
                            currentTime.plusMinutes(45).getMinute() == classTime.getMinute()) {
                            
                            String message = "Class Alert: " + parts[1] + " (" + parts[2] + ") starts in 45 minutes!";
                            NotificationService.sendPushNotification(message);
                        }
                    } catch (Exception e) {
                        // Skip lines that don't match time format perfectly
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading routine database.");
        }
    }
}
