package functions.chatGPT;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import env.ChatGPTKey;
import functions.FileOrganizer;
import functions.exif.ExifFunctions;
import functions.ffmpeg.MakeVideo;

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
                System.out.println("Image saved in " + path);
            } else {
                System.out.println("Error during the image saving");
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
            "-d", "\"{\\\"model\\\": \\\"gpt-4o-mini-tts\\\", \\\"input\\\": \\\""+description+"\\\", \\\"voice\\\": \\\"coral\\\", \\\"instructions\\\": \\\"Speech in a familiar way.\\\"}\"",
            "--output", path+"/"+fileName};
            FileOrganizer.executeCMDCommand(command);
            return fileName;
    }

    public static String generateAudioFromBase64ForImages(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        description = normalizeDescriptionForImages(description);
        String audioName = generateAudioFromText(description, path, outputFile);
        System.out.println("Audio made in: "+path+"/"+outputFile);
        return audioName;
    }
    public static String generateAudioFromBase64ForVideos(String path, String videoName, String outputFile){
        String frame = MakeVideo.saveFrame(videoName, path, "videoFrame.png");
        String description = base64ToDescription(path, frame);
        int seconds = ExifFunctions.extractDuration(path, videoName);
        description = normalizeDescriptionForVideos(description,seconds);
        String audioName = generateAudioFromText(description, path, outputFile);
        System.out.println("Audio made in: "+path+"/"+outputFile);
        FileOrganizer.deleteFile(path+"/"+frame);
        return audioName;
    }
    public static String generateAudioFromImageURL(String url, String path, String fileName){
        String description = generateDescriptionFromURL(url);
        String audioName = generateAudioFromText(description, path, fileName);
        return audioName;
    }

    public static String generateImageFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        String image = generateImageFromText(description, path, outputFile);
        return image;
    }

    public static String generatePostalCardFromBase64(String path, String imageName, String outputFile){
        String description = base64ToDescription(path, imageName);
        String image = makePostalCard(description, path, outputFile);
        return image;
    }

    public static String base64ToDescription(String path, String imageName) {
        String image64 = FileOrganizer.convertImageToBase64(path + "/" + imageName);
        String descripcion = "";
        try{
            String urlString = "https://api.openai.com/v1/responses";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + ChatGPTKey.getKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            JSONObject contentText = new JSONObject();
            contentText.put("type", "input_text");
            contentText.put("text", "Analyze the uploaded image and give a description. Just give back simple text.");


            JSONObject contentImage = new JSONObject();
            contentImage.put("type", "input_image");
            contentImage.put("image_url", "data:image/jpeg;base64," + image64);
            contentImage.put("detail", "low");

            JSONArray contentArray = new JSONArray();
            contentArray.put(contentText);
            contentArray.put(contentImage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", contentArray);

            JSONArray inputArray = new JSONArray();
            inputArray.put(userMessage);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("input", inputArray);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            catch(IOException e){
                System.err.println(e.getMessage());
            }

            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                while (scanner.hasNext()) {
                    String mssg = scanner.nextLine();
                    System.out.println(mssg);
                    if (mssg.contains("\"text\":")){
                        descripcion = mssg.substring(mssg.indexOf("\"text\":") + 9, mssg.indexOf("\",")-1);
                        descripcion = descripcion.replaceAll("[^a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ.,]", "");
                        return descripcion;
                    }
                }
            }catch(IOException e){
                System.err.println(e.getMessage());
            }
        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
        return "";
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

    public static String normalizeDescriptionForImages(String description){
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("https://api.openai.com/v1/responses");
        command.add("-H");
        command.add("Content-Type: application/json");
        command.add("-H");
        command.add("Authorization: Bearer " + ChatGPTKey.getKey());
        command.add("-d");
        command.add("\"{\\\"model\\\": \\\"gpt-4o\\\", \\\"input\\\": \\\"Give me the resume in 12-14 words of the following description: "+description+"\\\"}\"");
        
        String nDescription = "";

        ProcessBuilder processBuilder = new ProcessBuilder(command);
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
                    if (line.contains("\"text\":")) {
                        nDescription = line.substring(line.indexOf(":") + 2).trim();
                        nDescription = nDescription.substring(1, nDescription.length() - 1);  // Remove the extra quotes
                        nDescription = nDescription.replaceAll("[^a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ.,]", "");
                        return nDescription;
                    }
                }
                return nDescription;
            } else {
                System.out.println("Error during the normalization of the description");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }
    
    public static String normalizeDescriptionForVideos(String description, int duration){
        int words = (15/5)*duration;
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("https://api.openai.com/v1/responses");
        command.add("-H");
        command.add("Content-Type: application/json");
        command.add("-H");
        command.add("Authorization: Bearer " + ChatGPTKey.getKey());
        command.add("-d");
        command.add("\"{\\\"model\\\": \\\"gpt-4o\\\", \\\"input\\\": \\\"Give me the resume in "+words+" words of the following description: "+description+"\\\"}\"");
        
        String nDescription = "";

        ProcessBuilder processBuilder = new ProcessBuilder(command);
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
                    if (line.contains("\"text\":")) {
                        nDescription = line.substring(line.indexOf(":") + 2).trim();
                        nDescription = nDescription.substring(1, nDescription.length() - 1);  // Remove the extra quotes
                        nDescription = nDescription.replaceAll("[^a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ.,]", "");
                        return nDescription;
                    }
                }
                return nDescription;
            } else {
                System.out.println("Error during the normalization of the description");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }
}
