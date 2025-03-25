package functions.chatGPT;

import functions.FileOrganizer;
import java.io.*;
import java.net.*;
import env.ChatGPTKey;

public class IaFunctions {

    public static String generateImage(String prompt, String path) {
        System.out.println("Solicitando imagen a DALL·E 2...");

        // Escapar comillas dobles dentro del prompt
        String escapedPrompt = prompt.replace("\"", "\\\"");

        // Crear el JSON para la solicitud
        String jsonRequest = String.format("{\"model\":\"dall-e-2\",\"prompt\":\"%s\",\"n\":1,\"size\":\"1024x1024\"}", escapedPrompt);

        System.out.println("JSON a enviar: " + jsonRequest);  // Verificar el JSON generado

        // Comando curl para solicitar la imagen
        String[] command = {
            "curl", "-s", "-X", "POST", "https://api.openai.com/v1/images/generations",
            "-H", "Content-Type: application/json",
            "-H", "Authorization: Bearer " + ChatGPTKey.getKey(),
            "-d", jsonRequest
        };

        // Obtener la respuesta JSON con la URL de la imagen
        String response = getResponseFromCommand(command);
        if (response != null) {
            String imageUrl = extractImageUrl(response);
            if (imageUrl != null) {
                System.out.println("Imagen generada en: " + imageUrl);
                downloadImage(imageUrl, path);
                return path + "/output1.png";
            } else {
                System.out.println("No se pudo obtener la URL de la imagen.");
            }
        } else {
            System.out.println("No se obtuvo respuesta del comando.");
        }
        return null;
    }

    private static String getResponseFromCommand(String[] command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error ejecutando comando: " + e.getMessage());
        }
        return output.length() > 0 ? output.toString() : null;
    }

    private static String extractImageUrl(String jsonResponse) {
        int index = jsonResponse.indexOf("url\":\"");
        if (index != -1) {
            int start = index + 6; // Longitud de "url\":\""
            int end = jsonResponse.indexOf("\"", start);
            return jsonResponse.substring(start, end);
        }
        return null;
    }

    private static void downloadImage(String imageUrl, String path) {
        String outputFile = path + "/output1.png";
        String[] downloadCommand = {
            "curl", "-s", "-o", outputFile, imageUrl
        };

        if (FileOrganizer.executeCMDCommand(downloadCommand)) {
            System.out.println("Imagen guardada en: " + outputFile);
        } else {
            System.out.println("Error al descargar la imagen.");
        }
    }
}
