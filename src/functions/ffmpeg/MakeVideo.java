package functions.ffmpeg;

import java.io.*;
import java.util.*;

public class MakeVideo {

    public static void generateVideo(String[][] metadata, String path) {
        String concatFile = path + "/concat.txt";
        String outputFile = path + "/output.mp4";

        deleteFile(concatFile);
        deleteFile(outputFile);

        List<String> finalFiles = new ArrayList<>();

        for (String[] meta : metadata) {
            String file = meta[0];
            File inputFile = new File(path, file);

            if (isImage(file)) {
                String videoFile = convertImageToVideo(file, path);
                if (videoFile != null) {
                    finalFiles.add(new File(path, videoFile).getAbsolutePath());
                }
            } else if (inputFile.exists()) {
                String normalizedFile = normalizeVideo(inputFile);
                finalFiles.add(normalizedFile);
            } else {
                System.err.println("Archivo no encontrado: " + inputFile.getAbsolutePath());
            }
        }

        if (!createConcatFile(finalFiles, concatFile)) return;

        System.out.println("Ejecutando FFmpeg para concatenar:");
        executeFFmpegCommand(new String[]{
                "ffmpeg", "-f", "concat", "-safe", "0", "-i", concatFile,
                "-c:v", "libx264", "-pix_fmt", "yuv420p", "-r", "30", "-y", outputFile
        });
    }

    private static String convertImageToVideo(String imagePath, String outputPath) {
        File imageFile = new File(outputPath, imagePath);
        String videoFileName = imageFile.getName().replaceFirst("\\.(jpg|jpeg|png|bmp)$", ".mp4");
        File videoFile = new File(outputPath, videoFileName);

        if (videoFile.exists()) {
            System.out.println("Archivo ya existe, no se convierte: " + videoFile.getAbsolutePath());
            return videoFileName;
        }

        System.out.println("Ejecutando FFmpeg para convertir imagen a video...");
        boolean success = executeFFmpegCommand(new String[]{
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

    private static String normalizeVideo(File inputFile) {
        String normalizedFile = inputFile.getParent() + "/normalized_" + inputFile.getName();
        File outputFile = new File(normalizedFile);

        if (outputFile.exists()) {
            return outputFile.getAbsolutePath();
        }

        System.out.println("Normalizando " + inputFile.getAbsolutePath());

        boolean success = executeFFmpegCommand(new String[]{
                "ffmpeg", "-i", inputFile.getAbsolutePath(), "-vf", "scale=1280:720,fps=30",
                "-c:v", "libx264", "-pix_fmt", "yuv420p", "-y", outputFile.getAbsolutePath()
        });

        return success ? outputFile.getAbsolutePath() : inputFile.getAbsolutePath();
    }

    private static boolean createConcatFile(List<String> files, String concatFile) {
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

    private static boolean executeFFmpegCommand(String[] command) {
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

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            System.out.println("Archivo eliminado: " + path);
        }
    }

    private static boolean isImage(String file) {
        return file.matches(".*\\.(jpg|jpeg|png|bmp)$");
    }

    public static void generateCollage(String inputFilePath, String outputPath) {
        List<String> files = new ArrayList<>();

        // Leer archivos desde input.txt
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("file ")) {
                    String filename = line.split("'")[1];
                    files.add(filename);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo " + inputFilePath + ": " + e.getMessage());
            return;
        }

        if (files.size() < 4) {
            System.err.println("Debe haber al menos 4 archivos en input.txt.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");

        for (String file : files) {
            command.add("-i");
            command.add(file);
        }

        // Construcción del filtro de collage
        StringBuilder filterComplex = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            filterComplex.append(String.format("[%d:v]scale=320:240[s%d];", i, i));
        }

        filterComplex.append("[s0][s1]hstack[tmp1];");
        filterComplex.append("[tmp1][s2]hstack[tmp2];");
        filterComplex.append("[tmp2][s3]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2:shortest=1[outv]");

        command.add("-filter_complex");
        command.add(filterComplex.toString());
        command.add("-map");
        command.add("[outv]");
        command.add(outputPath + "/collage.mp4");

        // Ejecutar ffmpeg
        if (executeFFmpegCommand(command.toArray(new String[0]))) {
            System.out.println("Collage generado exitosamente en " + outputPath + "/collage.mp4");
        }
    }
}
