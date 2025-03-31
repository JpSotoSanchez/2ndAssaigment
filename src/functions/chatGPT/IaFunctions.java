package functions.chatGPT;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.Scanner;


import env.ChatGPTKey;
import functions.FileOrganizer;

public class IaFunctions {

    // Method to make the image using openai api
    public static String generateImageFromText(String prompt, String path, String fileName) {
        // Prepare the curl command as a list of arguments
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("https://api.openai.com/v1/images/generations");
        command.add("-H");
        command.add("Content-Type: application/json");
        command.add("-H");
        command.add("Authorization: Bearer " + ChatGPTKey.getKey());
        command.add("-d");
        command.add("\"{\\\"model\\\": \\\"dall-e-3\\\", \\\"prompt\\\": \\\""+prompt+"\\\", \\\"n\\\": 1, \\\"size\\\": \\\"1024x1024\\\"}\"");
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        String url = "";
        try {
            // Execute the command
            Process process = processBuilder.start();
            
            // Wait for the process to finish
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("\"url\"")) {
                        url = line.substring(line.indexOf(":") + 2).trim();
                        url = url.substring(1, url.length() - 1);  // Remove the extra quotes
                    }
                }
                downloadImage(url, path, fileName);
                return fileName;
            } else {
                System.out.println("Error during image generation.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }

    // Method to download the image using cURL
    public static void downloadImage(String url, String path, String fileName) {
        // Prepare the curl command as a list of arguments
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("-o");
        command.add(path + "/" + fileName);
        command.add(url);
    
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        
        try {
            // Execute the process
            Process process = processBuilder.start();
            
            // Wait for the process to finish
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("Imagen descargada y guardada en " + path);
            } else {
                System.out.println("Hubo un error al descargar la imagen.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    


    public static String generateDescriptionFromURL(String url){
        String[] command = {
            "curl",
            "https://api.openai.com/v1/chat/completions",
            "-H", "Content-Type: application/json",
            "-H", "Authorization: Bearer " + ChatGPTKey.getKey(),
            "-d", "\"{\\\"model\\\": \\\"gpt-4o-mini\\\", \\\"messages\\\": [{\\\"role\\\": \\\"user\\\", \\\"content\\\": [{\\\"type\\\": \\\"text\\\", \\\"text\\\": \\\"Describe this image in English and in 12 words:\\\"},{\\\"type\\\": \\\"image_url\\\", \\\"image_url\\\": {\\\"url\\\": \\\"" + url + "\\\"}}]}], \\\"max_tokens\\\": 200}\""
        };
            
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        String description = "";
    
        try {
            Process process = processBuilder.start();
        
            int waitFor = process.waitFor();
            
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("\"content\":")){
                        description=line.substring(line.indexOf(":")+3, line.length()-2);
                    }        
                }                
                return description;
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return description;
        }
    }

    public static String generateAudioFromText(String description, String path, String fileName){
        String[] command = {
            "curl",
            "https://api.openai.com/v1/audio/speech",
            "-H", "Content-Type: application/json",
            "-H", "Authorization: Bearer " + ChatGPTKey.getKey(),
            "-d", "\"{\\\"model\\\": \\\"gpt-4o-mini-tts\\\", \\\"input\\\": \\\""+description+"\\\", \\\"voice\\\": \\\"coral\\\", \\\"instructions\\\": \\\"It is imporant that the audio has a duration of 5 seconds.\\\"}\"",
            "--output", path+"/"+fileName};
            FileOrganizer.executeCMDCommand(command);
            return fileName;
    }

    public static String generateAudioFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        String audioName = generateAudioFromText(description, path, outputFile);
        System.out.println("Audio made in: "+path+"/"+outputFile);
        return audioName;
    }
    public static String generateAudioFromImageURL(String url, String path, String fileName){
        String description = generateDescriptionFromURL(url);
        String audioName = generateAudioFromText(description, path, fileName);
        return audioName;
    }

    public static String generateImageFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        System.out.println(description);
        String image = generateImageFromText(description, path, outputFile);
        return image;
    }

    public static String generatePostalCardFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        System.out.println(description);
        String image = makePostalCard(description, path, outputFile);
        return image;
    }

    public static String base64ToDescription(String path, String imageName) {
        String image64 = FileOrganizer.convertImageToBase64(path + "/" + imageName);
        try {

            // Construcción del cuerpo de la petición
            JSONObject imageObject = new JSONObject();
            imageObject.put("type", "image_url");
            imageObject.put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + image64));

            JSONObject textObject = new JSONObject();
            textObject.put("type", "text");
            textObject.put("text", "Describe this image in English and in 12 words:");

            JSONArray contentArray = new JSONArray();
            contentArray.put(textObject);
            contentArray.put(imageObject);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", contentArray);

            JSONArray messagesArray = new JSONArray();
            messagesArray.put(userMessage);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o");
            requestBody.put("messages", messagesArray);
            requestBody.put("max_tokens", 50);

            // Enviar la petición HTTP POST
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + ChatGPTKey.getKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Leer la respuesta
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Parsear la respuesta JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                return choices.getJSONObject(0).getJSONObject("message").getString("content");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return "Description not found.";
    }

    public static String makePostalCard(String prompt, String path, String fileName) {
        // Prepare the curl command as a list of arguments
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("https://api.openai.com/v1/images/generations");
        command.add("-H");
        command.add("Content-Type: application/json");
        command.add("-H");
        command.add("Authorization: Bearer " + ChatGPTKey.getKey());
        command.add("-d");
        command.add("\"{\\\"model\\\": \\\"dall-e-3\\\", \\\"prompt\\\": \\\"Make a postal card of the following description: "+prompt+"\\\", \\\"n\\\": 1, \\\"size\\\": \\\"1024x1024\\\"}\"");
    
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        String url = "";
        try {
            // Execute the command
            Process process = processBuilder.start();
            
            // Wait for the process to finish
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("\"url\"")) {
                        url = line.substring(line.indexOf(":") + 2).trim();
                        url = url.substring(1, url.length() - 1);  // Remove the extra quotes
                    }
                }
                downloadImage(url, path, fileName);
                return fileName;
            } else {
                System.out.println("Error during image generation.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }
}
