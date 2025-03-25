package functions.exif;

import java.io.*;

public class ExifFunctions {

    public static String[][] GatherVideos(String path) {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Error: The specified path is not a valid directory.");
            return new String[0][0]; // Return an empty array if there's an error
        }

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

        for (int i = 0; i < files.length; i++) {
            metadataArray[i] = extractMetadata(files[i]);
        }

        return metadataArray; // Return the array with the obtained metadata
    }

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
}
