package functions.chatGPT;

import java.io.*;
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

    public static String generateImageFromImage(String imageLink, String path, String fileName) {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("https://api.openai.com/v1/images/generations");
        command.add("-H");
        command.add("Content-Type: application/json");
        command.add("-H");
        command.add("Authorization: Bearer " + ChatGPTKey.getKey()); // Replace with your API key
        command.add("-d");
        command.add("\"{\\\"model\\\": \\\"dall-e-3\\\", \\\"prompt\\\": \\\""+generateDescription(imageLink)+"\\\", \\\"n\\\": 1, \\\"size\\\": \\\"1024x1024\\\"}\"");
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        String url = "";

        try {
            // Execute the curl command
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
                // Assuming `downloadImage(url, path, fileName)` is your method to download the image
                downloadImage(url, path, fileName);
                return path + "/" + fileName;
            } else {
                System.out.println("Error during image generation.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
    


    public static String generateDescription(String url){
        String[] command = {
            "curl",
            "https://api.openai.com/v1/chat/completions",
            "-H", "Content-Type: application/json",
            "-H", "Authorization: Bearer " + ChatGPTKey.getKey(),
            "-d", "\"{\\\"model\\\": \\\"gpt-4o-mini\\\", \\\"messages\\\": [{\\\"role\\\": \\\"user\\\", \\\"content\\\": [{\\\"type\\\": \\\"text\\\", \\\"text\\\": \\\"Describe this image in English:\\\"},{\\\"type\\\": \\\"image_url\\\", \\\"image_url\\\": {\\\"url\\\": \\\"" + url + "\\\"}}]}], \\\"max_tokens\\\": 200}\""
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
                        description=line.substring(line.indexOf(":")+2, line.length()-1);
                    }        
                }                
                return description;
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return description;
        }
    }

    public static void generateAudio(String description, String path, String fileName){
        String[] command = {
            "curl",
            "https://api.openai.com/v1/audio/speech",
            "-H", "Content-Type: application/json",
            "-H", "Authorization: Bearer " + ChatGPTKey.getKey(),
            "-d", "\"{\\\"model\\\": \\\"gpt-4o-mini-tts\\\", \\\"input\\\": \\\""+description+"\\\", \\\"voice\\\": \\\"coral\\\", \\\"instructions\\\": \\\"Speak in a cheerful and positive tone.\\\"}\"",
            "--output", path+"/"+fileName};
            FileOrganizer.executeCMDCommand(command);
    }
}
