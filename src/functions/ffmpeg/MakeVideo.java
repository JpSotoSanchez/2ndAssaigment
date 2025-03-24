package functions.ffmpeg;

import java.io.*;
import java.util.*;

import functions.FileOrganizer;

public class MakeVideo {

    public static void generateVideo(String[][] metadata, String path) {
        String concatFile = path + "/concat.txt";
        String outputFile = path + "/output2.mp4";

        FileOrganizer.deleteFile(concatFile);
        FileOrganizer.deleteFile(outputFile);

        List<String> finalFiles = new ArrayList<>();
        List<String> deleteFiles = new ArrayList<>();

        for (String[] meta : metadata) {
            String file = meta[0];
            File inputFile = new File(path, file);

            if (isImage(file)) {
                String videoFile = convertImageToVideo(file, path);
                if (videoFile != null) {
                    File videoFilePath = new File(path, videoFile);
                    System.out.println("Ruta a eliminar:"+videoFile);
                    deleteFiles.add(path+"/"+videoFile);
                    String normalizedFile = normalizeVideo(videoFilePath);
                    finalFiles.add(normalizedFile);
                    deleteFiles.add(normalizedFile);
                }
            } else if (inputFile.exists()) {
                String normalizedFile = normalizeVideo(inputFile);
                finalFiles.add(normalizedFile);
                deleteFiles.add(normalizedFile);
            } else {
                System.err.println("Archivo no encontrado: " + inputFile.getAbsolutePath());
            }
            
        }

        if (!FileOrganizer.createConcatFile(finalFiles, concatFile)) return;

        System.out.println("Ejecutando FFmpeg para concatenar:");
        FileOrganizer.executeCMDCommand(new String[]{
                "ffmpeg", "-f", "concat", "-safe", "0", "-i", concatFile,
                "-c:v", "libx264", "-pix_fmt", "yuv420p", "-r", "30", "-y", outputFile
        });
        System.out.println("Finished the video");
        for (String string : deleteFiles) {
            FileOrganizer.deleteFile(string);
        }
        FileOrganizer.deleteFile(concatFile);
    }

    public static String convertImageToVideo(String imagePath, String outputPath) {
        File imageFile = new File(outputPath, imagePath);
        String videoFileName = imageFile.getName().replaceFirst("\\.(jpg|jpeg|png|bmp)$", ".mp4");
        File videoFile = new File(outputPath, videoFileName);

        if (videoFile.exists()) {
            System.out.println("Archivo ya existe, no se convierte: " + videoFile.getAbsolutePath());
            return videoFileName;
        }

        System.out.println("Ejecutando FFmpeg para convertir imagen a video...");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
                "ffmpeg", "-loop", "1", "-i", imageFile.getAbsolutePath(), "-t", "5",
                "-vf", "scale=1280:720", "-c:v", "libx264", "-pix_fmt", "yuv420p", "-r", "30", "-y", videoFile.getAbsolutePath()
        });

        if (success) {
            System.out.println("Video creado: " + videoFile.getAbsolutePath());
            return videoFileName;
        } else {
            System.err.println("Error creando video para " + imageFile.getAbsolutePath());
            return null;
        }
    }

    public static String normalizeVideo(File inputFile) {
        String fileName = inputFile.getName();
        
        // Evitar normalizar un archivo que ya está normalizado
        if (fileName.startsWith("normalized_")) {
            return inputFile.getAbsolutePath();
        }
    
        String normalizedFile = inputFile.getParent() + "/normalized_" + fileName;
        File outputFile = new File(normalizedFile);
    
        if (outputFile.exists()) {
            return outputFile.getAbsolutePath();
        }
    
        System.out.println("Normalizando " + inputFile.getAbsolutePath());
    
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
                "ffmpeg", "-i", inputFile.getAbsolutePath(), "-vf", "scale=1280:720,fps=30",
                "-c:v", "libx264", "-pix_fmt", "yuv420p", "-y", outputFile.getAbsolutePath()
        });
    
        return success ? outputFile.getAbsolutePath() : inputFile.getAbsolutePath();
    }
    

    

    public static boolean executeCMDCommand(String[] command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(System.out::println);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("FFmpeg terminó con error: " + exitCode);
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error ejecutando FFmpeg: " + e.getMessage());
            return false;
        }
    }

    

    private static boolean isImage(String file) {
        return file.matches(".*\\.(jpg|jpeg|png|bmp)$");
    }

    public static void generateCollage(String inputFilePath, String outputPath) {
        List<String> files = new ArrayList<>();
        
        /*
        try (PrintWriter writer = new PrintWriter(new FileWriter(inputFilePath))) {
            writer.println("file '" + "output3.png" + "'");
        } catch (IOException e) {
            System.err.println("Error escribiendo concat.txt: " + e.getMessage());
        }
        */

        // Read files from input.txt
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("file ")) {
                    String filename = line.split(" ")[1].trim();
                    files.add(filename);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + inputFilePath + ": " + e.getMessage());
            return;
        }
    
        if (files.size() < 2) {
            System.err.println("At least 2 files are required in input.txt to generate a collage.");
            return;
        }
    
        // The last file is the center file
        String centerFile = files.remove(files.size() - 1);
    
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
    
        for (String file : files) {
            command.add("-i");
            command.add(file);
        }
        command.add("-i");
        command.add(centerFile); // Center file added separately
    
        // Build the filter_complex dynamically
        StringBuilder filterComplex = new StringBuilder();
    
        // Scale all input videos
        for (int i = 0; i < files.size(); i++) {
            filterComplex.append(String.format("[%d:v]scale=320:240[s%d];", i, i));
        }
        
        // Pairwise hstack
        int stackIndex = 0;
        List<String> rowLabels = new ArrayList<>();
        for (int i = 0; i < files.size(); i += 2) {
            if (i + 1 < files.size()) {
                filterComplex.append(String.format("[s%d][s%d]hstack=2[s_h%d];", i, i + 1, stackIndex));
                rowLabels.add(String.format("[s_h%d]", stackIndex));
            } else {
                rowLabels.add(String.format("[s%d]", i)); // Odd one out goes alone
            }
            stackIndex++;
        }
    
        // VStack to combine rows if needed
        if (rowLabels.size() > 1) {
            filterComplex.append(String.join("", rowLabels));
            filterComplex.append(String.format("vstack=%d[tmp1];", rowLabels.size()));
        } else {
            filterComplex.append(String.format("%s[tmp1];", rowLabels.get(0))); // Single row case
        }
    
        // Center overlay
        filterComplex.append(String.format("[%d:v]scale=320:240[center];", files.size())); // Center file is last input
        filterComplex.append("[tmp1][center]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2:shortest=1[outv]");
    
        command.add("-filter_complex");
        command.add(filterComplex.toString());
        command.add("-map");
        command.add("[outv]");
        command.add(outputPath + "/output4.mp4");
    
        // Execute FFmpeg
        if (FileOrganizer.executeCMDCommand(command.toArray(new String[0]))) {
            System.out.println("Collage successfully generated at " + outputPath + "/output3.mp4");
        }
    }
        
}
