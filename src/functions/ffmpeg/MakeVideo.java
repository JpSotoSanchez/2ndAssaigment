package functions.ffmpeg;

import java.io.*;
import java.util.*;

import functions.FileOrganizer;

public class MakeVideo {

    public static List<String> generateVideo(String[][] metadata, String path) {
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
                    deleteFiles.add(videoFilePath.getAbsolutePath());
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

        if (!FileOrganizer.createConcatFile(finalFiles, concatFile)) return deleteFiles;

        System.out.println("Ejecutando FFmpeg para concatenar:");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-f", "concat", "-safe", "0", "-i", concatFile, "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-y", outputFile
        });

        System.out.println("Finished the video");
        return deleteFiles;
    }

    public static String convertImageToVideo(String imagePath, String outputPath) {
        File imageFile = new File(outputPath, imagePath);
        String videoFileName = "video_" + imageFile.getName().replaceFirst("\\.(jpg|jpeg|png|bmp)$", ".mp4");
        File videoFile = new File(outputPath, videoFileName);

        if (videoFile.exists()) {
            return videoFileName;
        }

        System.out.println("Ejecutando FFmpeg para convertir imagen a video...");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-loop", "1", "-i", imageFile.getAbsolutePath(), "-t", "5",
            "-vf", "scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2",
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-an", "-y", videoFile.getAbsolutePath()
        });

        return success ? videoFileName : null;
    }

    public static String normalizeVideo(File inputFile) {
        String fileName = inputFile.getName();
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
            "ffmpeg", "-i", inputFile.getAbsolutePath(), "-vf", "scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2",
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-y", outputFile.getAbsolutePath()
        });

        return success ? outputFile.getAbsolutePath() : inputFile.getAbsolutePath();
    }

    private static boolean isImage(String file) {
        return file.matches(".*\\.(jpg|jpeg|png|bmp)$");
    }
}