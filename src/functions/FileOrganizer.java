package functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileOrganizer {
    public static String[][] sortByDate(String[][] metadataArray) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss"); // ExifTool format

        Arrays.sort(metadataArray, (meta1, meta2) -> {
            try {
                // Get dates, if no date, set a distant default one
                Date date1 = dateFormat.parse(meta1[1].equals("Unknown") ? "9999:12:31 23:59:59" : meta1[1]);
                Date date2 = dateFormat.parse(meta2[1].equals("Unknown") ? "9999:12:31 23:59:59" : meta2[1]);

                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        return metadataArray; // Return the sorted array
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        
        if (!file.exists()) {
            System.out.println("File not found: " + path);
            return;
        }
    
        if (file.delete()) {
            System.out.println("File deleted: " + path);
        } else {
            System.err.println("Error: Could not delete file: " + path);
        }
    }
    
    public static boolean createConcatFile(List<String> files, String concatFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(concatFile))) {
            for (String file : files) {
                writer.println("file '" + file + "'");
                System.out.println("Agregado a concat.txt: file '" + file + "'");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error escribiendo concat.txt: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean executeCMDCommand(String[] command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Lee la salida del proceso
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(System.out::println);
            }

            // Espera que el proceso termine y obtiene el código de salida
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("El proceso terminó con error: " + exitCode);
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error ejecutando el comando: " + e.getMessage());
            return false;
        }
    }

    // Método sobrecargado para ejecutar un comando único
    public static boolean executeCMDCommand(String command) {
        return executeCMDCommand(command.split(" ")); // Divide el comando en partes
    }

    



}
