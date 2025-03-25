import java.io.*;

import env.ChatGPTKey;

public class ImageTesting {
    public static void main(String[] args) {
        try {
            String command = "curl https://api.openai.com/v1/images/generations "
                             + "-H \"Content-Type: application/json\" "
                             + "-H \"Authorization: Bearer "+ChatGPTKey.getKey()+"\" "
                             + "-d \"{\\\"model\\\": \\\"dall-e-3\\\", \\\"prompt\\\": \\\"a mage shark\\\", \\\"n\\\": 1, \\\"size\\\": \\\"1024x1024\\\"}\"";

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            System.out.println("Exit Code: " + exitCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
