import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import functions.FileOrganizer;
import functions.ffmpeg.MakeVideo;

public class ProbarCOmandoCollage {
    
    public static String generateFFmpegCommand(String filePath, String outputFilePath) throws IOException {
        
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int numVideos = 0;  // Contador de videos
        StringBuilder command = new StringBuilder("ffmpeg ");

        // Leer cada línea y agregar los archivos de video al comando
        while ((line = reader.readLine()) != null) {
            line = line.trim();  // Eliminar espacios en blanco adicionales
            if (!line.isEmpty()) {  // Si la línea no está vacía
                // Asumimos que cada línea empieza con "file:" y termina con un carácter extra (por ejemplo, comillas)
                command.append("-i \"").append(line.substring(6, line.length()-1)).append("\" ");
                numVideos++;
            }
        }
        reader.close();

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

        // Especificar la salida
        command.append("-map \"[out]\" -c:v libx264 -crf 23 -preset veryfast -shortest "+outputFilePath);
        FileOrganizer.executeCMDCommand(command.toString());
        System.out.println("Collage made");
        String outputFile2 = path+"/"+"output3_2.mp4";
        MakeVideo.cutVideoFiveSeconds(outputFilePath, outputFile2);
        System.out.println("Cut it to 5 seconds");
        String outputFileFinal = "output3.mp4";
        MakeVideo.overlayImage("src/multimedia/images.jpg", outputFile2, outputFileFinal);
        System.out.println("Image overlayed");
        FileOrganizer.deleteFile(outputFilePath);
        FileOrganizer.deleteFile(outputFile2);
        MakeVideo.saveFrame(outputFileFinal, "frame.png");
        return command.toString();
    }

    public static void main(String[] args) throws IOException {
        FileOrganizer.deleteFile("output_3_1.mp4");
        FileOrganizer.deleteFile("output3_2.mp4");
        String ffmpegCommand = generateFFmpegCommand("src/multimedia/concat.txt", "output_3_1.mp4");
        System.out.println("Comando generado:\n" + ffmpegCommand);
        
    }
}
