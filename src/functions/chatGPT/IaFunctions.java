package functions.chatGPT;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import env.ChatGPTKey;
import functions.FileOrganizer;

public class IaFunctions {

    // Método para generar la imagen y guardarla
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
        command.add("\"{\\\"model\\\": \\\"dall-e-3\\\", \\\"prompt\\\": \\\"Generate a postal card of the following: "+prompt+"\\\", \\\"n\\\": 1, \\\"size\\\": \\\"1024x1024\\\"}\"");
        
        
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
                // Assuming `descargarImagen(url, path)` is your method to download the image
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

    // Método para descargar la imagen usando curl
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
    


    public static String generateDescriptionFromUrl(String url){
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
        
            int _ = process.waitFor();
            
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

    public static String generateAudioFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        generateAudioFromText(description, path, outputFile);
        return new String("Audio made in: "+path+"/"+outputFile);
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
            return new String("Audio made in: "+path+"/"+fileName);
    }

    public static String generateImageFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        System.out.println(description);
        String image = generateImageFromText(description, path, outputFile);
        return image;
    }

    public static String base64ToDescription(String path, String imageName) {
        String image64 = FileOrganizer.convertImageToBase64(path + "/" + imageName);
    
        String requestBody = """
                {
                    "model": "gpt-4-vision-preview",
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "Describe this image in English and in 12 words:"
                                },
                                {
                                    "type": "image_url",
                                    "image_url": {
                                        "url": "data:image/jpeg;base64,%s"
                                    }
                                }
                            ]
                        }
                    ],
                    "max_tokens": 300
                }
                """.formatted(image64);
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ChatGPTKey.getKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 200) {
                // Extract description from JSON response using JSON parsing library
                // Example with Jackson ObjectMapper:
                // ObjectMapper mapper = new ObjectMapper();
                // JsonNode rootNode = mapper.readTree(response.body());
                // String description = rootNode.get("content").get(0).get("text").asText();
                
                // For simplicity, assuming a straightforward JSON structure:
                String responseBody = response.body();
                return responseBody.split("\"content\":\"")[1].split("\"")[0];
            } else {
                System.err.println("Error: Unexpected HTTP status code: " + response.statusCode());
                return null; // or handle error as appropriate
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null; // or handle error as appropriate
        }
    }
}
