package functions.chatGPT;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.net.URL;

public class IaFunctions {

    // Función para describir la imagen usando la API de OpenAI
    public static String describeImage(String apiKey, String imagePath) {
        // Comando cURL para enviar la imagen a OpenAI
        String command = String.format(
                "curl https://api.openai.com/v1/images/generations " +
                        "-H \"Content-Type: application/json\" " +
                        "-H \"Authorization: Bearer %s\" " +
                        "-F \"file=@%s\" " +
                        "-F \"model=gpt-4-vision\"",
                apiKey, imagePath
        );

        // Ejecutamos el comando cURL usando ProcessBuilder
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        try {
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor(); // Esperamos que el proceso termine
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error al procesar la imagen.";
        }

        // Parseamos la respuesta JSON de OpenAI para extraer la descripción
        return parseDescription(output.toString());
    }

    // Método para parsear la respuesta JSON y obtener la descripción de la imagen
    private static String parseDescription(String jsonResponse) {
        String description = "Descripción no encontrada.";
        try {
            int start = jsonResponse.indexOf("\"text\":") + 8;
            int end = jsonResponse.indexOf("\"", start);
            if (start != -1 && end != -1) {
                description = jsonResponse.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return description;
    }

    // Función para generar audio (speech) a partir del texto usando cURL y la API de OpenAI
    public static void generateSpeech(String apiKey, String text, String outputAudioPath) {
        // Comando cURL para enviar el texto a la API de OpenAI para la conversión a voz
        String command = String.format(
                "curl https://api.openai.com/v1/audio/transcriptions " +
                        "-H \"Authorization: Bearer %s\" " +
                        "-H \"Content-Type: application/json\" " +
                        "-d \"{\\\"input\\\": \\\"%s\\\"}\"",
                apiKey, text
        );

        // Ejecutamos el comando cURL usando ProcessBuilder
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        try {
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor(); // Esperamos que el proceso termine
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error al convertir el texto a audio.");
            return;
        }

        // Guardar el resultado como un archivo de audio (esto dependerá del formato de la respuesta)
        saveAudio(output.toString(), outputAudioPath);
    }

    // Método para guardar el audio obtenido (suponiendo que la respuesta es un enlace para descargar el audio)
    private static void saveAudio(String response, String outputAudioPath) {
        try {
            // Extraemos la URL del audio de la respuesta
            String audioUrl = parseAudioUrl(response);

            if (audioUrl == null || audioUrl.isEmpty()) {
                System.out.println("No se encontró la URL del archivo de audio.");
                return;
            }

            // Descargar el archivo de audio desde la URL
            downloadAudio(audioUrl, outputAudioPath);

            System.out.println("Audio guardado correctamente en: " + outputAudioPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al guardar el audio.");
        }
    }

    // Método para parsear la URL del audio desde la respuesta
    private static String parseAudioUrl(String jsonResponse) {
        // Suponemos que la respuesta contiene una URL de archivo de audio en el campo "url".
        String url = null;

        try {
            int start = jsonResponse.indexOf("\"url\":") + 7;  // Encontrar la posición donde comienza la URL
            int end = jsonResponse.indexOf("\"", start);      // Encontrar la posición donde termina la URL

            if (start != -1 && end != -1) {
                url = jsonResponse.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    // Método para descargar el archivo de audio desde la URL y guardarlo en el disco
    private static void downloadAudio(String audioUrl, String outputAudioPath) throws IOException {
        URL url = new URL(audioUrl);
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(outputAudioPath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    // Función para verificar si el archivo es una imagen
    public static boolean esImagen(String archivo) {
        return archivo.toLowerCase().endsWith(".jpg") || archivo.toLowerCase().endsWith(".jpeg") ||
                archivo.toLowerCase().endsWith(".png") || archivo.toLowerCase().endsWith(".bmp");
    }
}
