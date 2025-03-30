package functions.ffmpeg;

import java.io.*;
import java.util.*;

import functions.FileOrganizer;
import functions.chatGPT.IaFunctions;

public class MakeVideo {

    public static List<String> generateVideo(String[][] metadata, String path, String txtNameString, String outFileString) {
        String concatFile = path + "/"+txtNameString;
        String outputFile = path + "/"+outFileString;

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
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-y", videoFile.getAbsolutePath()
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
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-an","-y", outputFile.getAbsolutePath()
        });

        return success ? outputFile.getAbsolutePath() : inputFile.getAbsolutePath();
    }

    private static boolean isImage(String file) {
        return file.matches(".*\\.(jpg|jpeg|png|bmp)$");
    }
    
    
    public static String generateCollage(String txtFile, String outputFile, String path) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path+"/"+txtFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        int numVideos = 0;  // Contador de videos
        StringBuilder command = new StringBuilder("ffmpeg ");

        // Leer cada línea y agregar los archivos de video al comando
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // Eliminar espacios en blanco adicionales
                if (!line.isEmpty()) {  // Si la línea no está vacía
                    // Asumimos que cada línea empieza con "file:" y termina con un carácter extra (por ejemplo, comillas)
                    command.append("-i \"").append(line.substring(6, line.length()-1)).append("\" ");
                    numVideos++;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Determinar cómo distribuir los videos en filas y columnas
        int rows = (int) Math.ceil(Math.sqrt(numVideos)); // Número de filas
        int cols = (int) Math.ceil((double) numVideos / rows); // Número de columnas
        System.out.println("Rows: " + rows);
        System.out.println("Cols: " + cols);

        // Crear el filtro complejo para escalado y apilamiento
        command.append("-filter_complex \"");

        // Calcular dimensiones para cada video
        int height = 1080 / rows;
        int width = 1920 / cols;
        System.out.println("Height: " + height);
        System.out.println("Width: " + width);

        // Escalar cada video y asignarle una etiqueta [vX]
        for (int i = 0; i < numVideos; i++) {
            command.append("[").append(i).append(":v]scale=")
                   .append(width).append("x").append(height)
                   .append("[v").append(i).append("]; ");
        }

        // Crear cada fila: si hay más de un video, usar hstack; si solo hay uno, aplicar un filtro format para asignar etiqueta.
        // Además, si la fila tiene menos de 'cols' videos, se aplica pad para que todas las filas tengan el mismo ancho.
        for (int i = 0; i < rows; i++) {
            int startIdx = i * cols;
            int endIdx = Math.min(startIdx + cols, numVideos);
            
            if (startIdx < numVideos) {
                String rowLabel = "row" + i;
                if (endIdx - startIdx > 1) {
                    // Más de un video en la fila: concatenar cada entrada con sus corchetes
                    for (int j = startIdx; j < endIdx; j++) {
                        command.append("[v").append(j).append("]");
                    }
                    command.append("hstack=inputs=").append(endIdx - startIdx)
                           .append("[").append(rowLabel).append("]; ");
                } else {
                    // Un solo video en la fila: aplicar filtro inofensivo para asignar la etiqueta
                    command.append("[v").append(startIdx)
                           .append("]format=yuv420p[").append(rowLabel).append("]; ");
                }
                // Si la fila no tiene la cantidad completa de columnas, aplicamos un pad para igualar el ancho.
                if ((endIdx - startIdx) < cols) {
                    // El ancho deseado para la fila es cols*width.
                    String paddedRow = rowLabel + "p";
                    command.append("[").append(rowLabel).append("]")
                           .append("pad=").append(cols * width).append(":").append(height)
                           .append(":0:0:black[").append(paddedRow).append("]; ");
                    rowLabel = paddedRow; // Actualizamos la etiqueta para usar la fila ya paddeada.
                }
            }
        }

        // Unir las filas usando vstack (ahora todas tendrán el mismo ancho)
        command.append("[");
        for (int i = 0; i < rows; i++) {
            // Usar la etiqueta paddeada si existe, de lo contrario la original
            int startIdx = i * cols;
            int endIdx = Math.min(startIdx + cols, numVideos);
            if ((endIdx - startIdx) < cols) {
                command.append("row").append(i).append("p");
            } else {
                command.append("row").append(i);
            }
            if (i < rows - 1) {
                command.append("][");
            }
        }
        command.append("]vstack=inputs=").append(rows).append("[out]\" ");

        String outputFile1 = "temp0.mp4";
        // Especificar la salida
        command.append("-map \"[out]\" -c:v libx264 -crf 23 -preset veryfast -shortest "+path+"/"+outputFile1);
        FileOrganizer.executeCMDCommand(command.toString());
        System.out.println("Collage made");
        
        String outputFile2 = "temp1.mp4";
        MakeVideo.cutVideoFiveSeconds(outputFile1, outputFile2, path);
        System.out.println("Cut it to 5 seconds");
        
        
        String frame = MakeVideo.saveFrame(outputFile2, path, "frame.png");
        System.out.println("Made the frame");
        
        MakeVideo.overlayImage("shark1.jpg", outputFile2, outputFile, path);
        //MakeVideo.overlayImage(path+IaFunctions.generateImageFromImage(url, path, outputFileFinal), outputFile2, outputFileFinal);
        System.out.println("Image overlayed");
        FileOrganizer.deleteFile(path+"/"+outputFile1);
        FileOrganizer.deleteFile(path+"/"+outputFile2);
        return command.toString();
    }

    public static void cutVideoFiveSeconds(String inputFile, String outputFile, String path){
        String[] command = {
            "ffmpeg",
            "-i", path+"/"+inputFile,  
            "-t", "00:00:05",  
            "-c", "copy", "-y",       
            path+"/"+outputFile        
        };
        FileOrganizer.executeCMDCommand(command);
    }
    public static void overlayImage(String imageName, String videoName, String outputFileName, String path){
        String[] command = {
            "ffmpeg", 
            "-i", path+"/"+videoName,
            "-i", path+"/"+imageName,
            "-filter_complex",
            "[0:v][1:v]overlay=(W-w)/2:(H-h)/2",
            "-c:a", "copy", "-y",
            path+"/"+outputFileName
        };        
        FileOrganizer.executeCMDCommand(command);
    }
    public static String saveFrame(String video, String path, String savedFrameName) {
        String[] command = {
            "ffmpeg",
            "-ss", "00:00:01",   // Posición en el tiempo (10 segundos)
            "-i", path+"/"+video,
            "-frames:v", "1",
            "-update", "1", "-y",      // Sobrescribe la imagen de salida
            path+"/"+savedFrameName
        };
        FileOrganizer.executeCMDCommand(command);
        return savedFrameName;
    }       
}

