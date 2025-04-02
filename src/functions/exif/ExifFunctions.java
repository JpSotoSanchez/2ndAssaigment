package functions.exif;

import java.io.*;

public class ExifFunctions {

    //The function gathers and saves the files in a directory.
    public static String[][] GatherVideos(String path) {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Error: The specified path is not a valid directory.");
            return new String[0][0]; // Return an empty array if there's an error
        }

        //This array will gather the files from inside the directory
        File[] files = folder.listFiles((_, name) ->
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".mp4") ||
                        name.toLowerCase().endsWith(".mov") || name.toLowerCase().endsWith(".avi")
        );

        if (files == null || files.length == 0) {
            System.out.println("No images or videos were found in the folder.");
            return new String[0][0]; // Return an empty array
        }

        String[][] metadataArray = new String[files.length][3];

        //The following will extract the metadata of the files
        for (int i = 0; i < files.length; i++) {
            metadataArray[i] = extractMetadata(files[i]);
        }

        return metadataArray; // Return the array with the obtained metadata
    }

    //This function will extract the metadata of the files. It will store the name, the rotation and the Create Date
    public static String[] extractMetadata(File file) {
        String fileName = file.getName();
        String creationDate = "Unknown"; // Default value
        String rotation = "0"; // Default value

        String[] command = new String[]{"exiftool", file.getAbsolutePath()};

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[EXIF] " + line); // Debug: Show the exiftool output

                if (line.contains("Rotation")) {
                    rotation = line.substring(line.indexOf(":") + 1).trim();
                } else if (line.startsWith("Create Date")) {
                    creationDate = line.substring(line.indexOf(":") + 1).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String[]{fileName, creationDate, rotation};
    }

    public static int extractDuration(String path, String fileName) {
        String[] command = new String[]{"exiftool", path + "/" + fileName};
        String duration = "";
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
    
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[EXIF] " + line); // Debug: Show the exiftool output
    
                if (line.contains("Duration")) {
                    duration = line.substring(line.indexOf(":") + 1).trim();
                    break; // Found duration, exit loop
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        if (duration.isEmpty()) {
            return 5; // Return 5 or handle the case where duration was not found
        }
    
        String[] partes = duration.split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        int segundos = Integer.parseInt(partes[2]);
    
        // Convertimos todo a segundos
        int totalSeconds = (horas * 3600) + (minutos * 60) + segundos;
    
        return totalSeconds;
    }
}
