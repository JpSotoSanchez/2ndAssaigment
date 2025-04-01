package functions.ffmpeg;

import java.io.*;
import java.util.*;

import functions.FileOrganizer;
import functions.chatGPT.IaFunctions;

public class MakeVideo {

    public static List<String> generateVideo(String[][] metadata, String path, String txtNameString, String outFileString, int width, int height) {
        String concatFile = path + "/"+txtNameString;
        String outputFile = path + "/"+outFileString;

        FileOrganizer.deleteFile(concatFile);
        FileOrganizer.deleteFile(outputFile);

        List<String> finalFiles = new ArrayList<>();
        List<String> deleteFiles = new ArrayList<>();

        for (String[] meta : metadata) {
            String file = meta[0];

            if (isImage(file)) {
                String videoFile = convertImageToVideo(file, path, width, height);
                if (videoFile != null) {
                    deleteFiles.add(path+"/"+videoFile);
                    
                    String normalizedFile = normalizeVideo(path, videoFile, width, height);
                    deleteFiles.add(path+"/"+normalizedFile);
                    
                    String audioName = IaFunctions.generateAudioFromBase64ForImages(path, file,"audio_" + file.replaceFirst("\\.(jpg|jpeg|png|bmp)$", ".mp3"));
                    deleteFiles.add(path+"/"+audioName);

                    String normalizedWithAudio = addAudio(path, normalizedFile, audioName);
                    finalFiles.add(normalizedWithAudio);
                    deleteFiles.add(path+"/"+normalizedWithAudio);
                    
                }
            } else if (isVideo(file)) {
                String normalizedFile = normalizeVideo(path, file, width, height);
                deleteFiles.add(path+"/"+normalizedFile);
                
                String frame = MakeVideo.saveFrame(file, path, "frame.png");
                deleteFiles.add(frame);
                
                String audioName = IaFunctions.generateAudioFromBase64ForVideos(path, frame,"audio_" + file.replaceFirst("\\.(mp4|mov|avi)$", ".mp3"));
                deleteFiles.add(path+"/"+audioName);

                String normalizedWithAudio = addAudio(path, normalizedFile, audioName);
                finalFiles.add(normalizedWithAudio);
                deleteFiles.add(path+"/"+normalizedWithAudio);
            } else {
                System.err.println("File not Found: " + path+"/"+file);
            }
        }

        if (!FileOrganizer.createConcatFile(path, finalFiles, concatFile)) return deleteFiles;

        System.out.println("Executing FFmpeg to concatenate:");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-f", "concat", "-safe", "0", "-i", concatFile, "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-y", outputFile
        });

        System.out.println("Finished the video");
        return deleteFiles;
    }

    public static String convertImageToVideo(String imageName, String path, int width, int height) {
        File imageFile = new File(path, imageName);
        String videoFileName = "video_" + imageFile.getName().replaceFirst("\\.(jpg|jpeg|png|bmp)$", ".mp4");
        File videoFile = new File(path, videoFileName);

        if (videoFile.exists()) {
            return videoFileName;
        }

        System.out.println("Executing FFMPEG to turn an image to a video...");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-loop", "1", "-i", imageFile.getAbsolutePath(), "-t", "5",
            "-vf", "scale="+width+":"+height+":force_original_aspect_ratio=decrease,pad="+width+":"+height+":(ow-iw)/2:(oh-ih)/2",
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-y", videoFile.getAbsolutePath()
        });

        return success ? videoFileName : null;
    }

    public static String convertImageToVideoWithSilencedAudio(String imageName, String path, int width, int height) {
        File imageFile = new File(path, imageName);
        String videoFileName = "video_" + imageFile.getName().replaceFirst("\\.(jpg|jpeg|png|bmp)$", ".mp4");
        File videoFile = new File(path, videoFileName);
    
        if (videoFile.exists()) {
            return videoFileName; // Video already exists, return success
        }
    
        System.out.println("Executing FFMPEG to turn an image to a video with silent audio...");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-loop", "1", "-i", imageFile.getAbsolutePath(), "-t", "5", // Video duration 5 seconds
            "-f", "lavfi", "-i", "anullsrc=r=44100:cl=stereo", // Silent audio for 5 seconds
            "-vf", "scale=" + width + ":" + height + ":force_original_aspect_ratio=decrease," +
                  "pad=" + width + ":" + height + ":(ow-iw)/2:(oh-ih)/2", // Scale and pad video
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", // Video encoding options
            "-c:a", "aac", "-b:a", "192k", // Audio codec settings
            "-y", videoFile.getAbsolutePath() // Output file path
        });
    
        return success ? videoFileName : null;
    }
    

    public static String normalizeVideo(String path, String inputFile, int width, int height) {
        String fileName = path+"/"+inputFile;
        if (fileName.startsWith("normalized_")) {
            return fileName;
        }

        String normalizedFile ="normalized_" + inputFile;
        File outputFile = new File(normalizedFile);

        if (outputFile.exists()) {
            return outputFile.getAbsolutePath();
        }

        System.out.println("Giving format to " + path+"/"+inputFile);

        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-i", path+"/"+inputFile , "-vf", "scale="+width+":"+height+":force_original_aspect_ratio=decrease,pad="+width+":"+height+":(ow-iw)/2:(oh-ih)/2",
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-an","-y", path+"/"+normalizedFile
        });

        return success ? normalizedFile : fileName;
    }

    public static String normalizeVideoWithAudio(String path, String inputFile, int width, int height) {
        String fileName = path+"/"+inputFile;
        if (fileName.startsWith("normalized_")) {
            return fileName;
        }

        String normalizedFile ="normalized_" + inputFile;
        File outputFile = new File(normalizedFile);

        if (outputFile.exists()) {
            return outputFile.getAbsolutePath();
        }

        System.out.println("Giving format to " + path+"/"+inputFile);

        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-i", path+"/"+inputFile , "-vf", "scale="+width+":"+height+":force_original_aspect_ratio=decrease,pad="+width+":"+height+":(ow-iw)/2:(oh-ih)/2",
            "-c:v", "libx264", "-crf", "23", "-preset", "fast", "-r", "30", "-pix_fmt", "yuv420p", "-y", path+"/"+normalizedFile
        });

        return success ? normalizedFile : fileName;
    }

    private static boolean isImage(String file) {
        return file.matches(".*\\.(jpg|jpeg|png|bmp)$");
    }

    private static boolean isVideo(String file) {
        return file.matches(".*\\.(mov|mp4|avi)$");
    }
    
    
    public static String generateCollage(String txtFile, String outputFile, String path, int width, int height) {
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
                    command.append("-i \"").append(path+"/").append(line.substring(6, line.length()-1)).append("\" ");
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

        // Crear el filtro complejo para escalado y apilamiento
        command.append("-filter_complex \"");

        // Calcular dimensiones para cada video
        int nHeight = height / rows;
        int nWidth = width / cols;

        // Escalar cada video y asignarle una etiqueta [vX]
        for (int i = 0; i < numVideos; i++) {
            command.append("[").append(i).append(":v]scale=")
                   .append(nWidth).append("x").append(nHeight)
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
                           .append("pad=").append(cols * nWidth).append(":").append(nHeight)
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

        String postalCard = IaFunctions.generatePostalCardFromBase64(path, frame, "PostalCard2.png");
        

        MakeVideo.overlayImage(postalCard, outputFile2, outputFile, path);
        System.out.println("Image overlayed");
        FileOrganizer.deleteFile(path+"/"+outputFile1);
        FileOrganizer.deleteFile(path+"/"+outputFile2);
        FileOrganizer.deleteFile(path+"/"+frame);
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

    public static void overlayImage(String imageName, String videoName, String outputFileName, String path) {
        String[] command = {
            "ffmpeg", 
            "-i", path + "/" + videoName,
            "-i", path + "/" + imageName,
            "-filter_complex",
            "[1:v]scale=iw/1.5:ih/1.5[scaled];[0:v][scaled]overlay=(W-w)/2:(H-h)/2",
            "-c:a", "copy", "-y",
            path + "/" + outputFileName
        };        
        FileOrganizer.executeCMDCommand(command);
    }

    public static String saveFrame(String video, String path, String savedFrameName) {
        String[] command = {
            "ffmpeg",
            "-ss", "00:00:00",   
            "-i", path+"/"+video,
            "-frames:v", "1",
            "-update", "1", "-y",      
            path+"/"+savedFrameName
        };
        FileOrganizer.executeCMDCommand(command);
        return savedFrameName;
    }
    
    public static void generateAudioFrom64(String path, String audioName, String base64){
        String command [] = {
            "ffmpeg", 
            "-y", 
            "-f", 
            "s16le", 
            "-ar", 
            "44100", 
            "-ac", 
            "2", 
            "-i", 
            "<(echo '"+base64+"' | base64 --decode)", 
            path+"/"+audioName
        };
        FileOrganizer.executeCMDCommand(command);
        System.out.println("Audio generated in" +path+"/"+audioName);
    }

    public static String addAudio(String path, String videoFile, String audioFile){
        String outputFile = "audio_"+videoFile;
        String[] command = {
            "ffmpeg",
            "-i", path+"/"+videoFile,        // Video de entrada
            "-i", path+"/"+audioFile,        // Audio de entrada
            "-c:v", "copy",                 // Copiar códec de video
            "-c:a", "aac",                  // Códec de audio AAC
            "-strict", "experimental",      // Opción para códec AAC
            "-map", "0:v:0",                // Mapear video de la primera entrada
            "-map", "1:a:0",                // Mapear audio de la segunda entrada
            path+"/"+outputFile   // Video de salida con audio añadido
        };
        FileOrganizer.executeCMDCommand(command);
        return outputFile;
    }

    public static List<String> concatenateVideos(String[][] metadata, String path, String txtNameString, String outFileString, int width, int height) {
        String concatFile = path + "/"+txtNameString;
        String outputFile = path + "/"+outFileString;

        FileOrganizer.deleteFile(concatFile);
        FileOrganizer.deleteFile(outputFile);

        List<String> finalFiles = new ArrayList<>();
        List<String> deleteFiles = new ArrayList<>();

        for (String[] meta : metadata) {
            String file = meta[0];

            if (isImage(file)) {
                String videoFile = convertImageToVideo(file, path, width, height);
                if (videoFile != null) {
                    deleteFiles.add(path+"/"+videoFile);
                    
                    String normalizedFile = normalizeVideoWithAudio(path, videoFile, width, height);
                    deleteFiles.add(path+"/"+normalizedFile);
                    
                    finalFiles.add(normalizedFile);                    
                }
            } else if (isVideo(file)) {
                String normalizedFile = normalizeVideoWithAudio(path, file, width, height);
                deleteFiles.add(path+"/"+normalizedFile);

                finalFiles.add(normalizedFile);
            } else {
                System.err.println("File not Found: " + path+"/"+file);
            }
        }

        if (!FileOrganizer.createConcatFile(path, finalFiles, concatFile)) return deleteFiles;

        System.out.println("Executing FFmpeg to concatenate:");
        boolean success = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-f", "concat", "-safe", "0", "-i", concatFile, 
            "-c:v", "libx264", "-crf", "23", "-c:a", "aac", "-preset", "fast", 
            "-r", "30", "-pix_fmt", "yuv420p", "-y", 
            "-map", "0:v:0", "-map", "0:a:0?", // Use '?' to ignore missing audio streams
            outputFile
        });
        

        System.out.println("Finished the video");
        return deleteFiles;
    }
}

