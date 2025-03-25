import java.io.*;
import java.util.Scanner;

import env.ChatGPTKey;
import functions.chatGPT.IaFunctions;
import functions.ffmpeg.MakeVideo;

public class CoomandIaTesting {
    public static void main(String[] args) {
         Scanner input = new Scanner(System.in);

        // Request the multimedia file path
        System.out.println("Enter the path of the images/videos: ");
        String path = input.nextLine();

        // Request the multimedia file path
        System.out.println("Enter the mood of the video: ");
        
        
        String mood = input.nextLine();
        IaFunctions.generateImage(mood, path);
        
    }
}
