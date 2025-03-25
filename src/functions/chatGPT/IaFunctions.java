package functions.chatGPT;

import functions.FileOrganizer;
import java.io.*;
import env.ChatGPTKey;

public class IaFunctions {

    public static void generateImage(String prompt, String path) {
        System.out.println("Solicitando imagen a DALL·E 3...");

        // Construir el payload JSON con formato adecuado
        String jsonPayload = buildJsonPayload(prompt);
        
        // Comando curl con la estructura exacta solicitada
        String[] command = {
            "curl",
            "https://api.openai.com/v1/images/generations",
            "-H", "Content-Type: application/json",
            "-H", "Authorization: Bearer " + ChatGPTKey.getKey(),
            "-d", jsonPayload
        };
        
        // Mostrar el comando que se ejecutará (ocultando la API key)
        logCommand(command);
        
        // Ejecutar el comando
        FileOrganizer.executeCMDCommand(command);
    }

    private static String buildJsonPayload(String prompt) {
        // Escapar comillas en el prompt para JSON válido
        String escapedPrompt = prompt.replace("\"", "\\\"");
        
        return new StringBuilder()
            .append("{\n")
            .append("    \"model\": \"dall-e-3\",\n")
            .append("    \"prompt\": \"").append(escapedPrompt).append("\",\n")
            .append("    \"n\": 1,\n")
            .append("    \"size\": \"1024x1024\"\n")
            .append("}")
            .toString();
    }

    private static void logCommand(String[] command) {
        StringBuilder loggedCommand = new StringBuilder();
        for (String part : command) {
            if (part.contains(ChatGPTKey.getKey())) {
                loggedCommand.append("Authorization: Bearer *** ");
            } else {
                loggedCommand.append(part).append(" ");
            }
        }
        System.out.println("Ejecutando comando:");
        System.out.println(loggedCommand.toString().trim());
    }
}