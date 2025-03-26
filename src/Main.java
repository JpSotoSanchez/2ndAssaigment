import functions.exif.ExifFunctions;
import functions.FileOrganizer;
import functions.chatGPT.IaFunctions;
import functions.ffmpeg.MakeVideo; // Import the new module

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        // Request the multimedia file path
        System.out.println("Enter the path of the images/videos: ");
        String path = input.nextLine();

        // Request the multimedia file path
        System.out.println("Enter the mood of the video: ");
        String mood = input.nextLine();

        // Get the metadata of the files
        String[][] metadataList = ExifFunctions.GatherVideos(path);

        // Check if there are any data
        if (metadataList.length == 0) {
            System.out.println("No files with valid metadata found.");
            input.close();
            return;
        }

        // Sort the files by creation date
        metadataList = FileOrganizer.sortByDate(metadataList);

        // Display the sorted list
        System.out.println("\nFiles sorted chronologically:");
        for (String[] meta : metadataList) {
            System.out.println("File: " + meta[0] + ", Date: " + meta[1] + ", Rotation: " + meta[2]);
        }

        
        // Call the function to generate the video with FFMpeg
        System.out.println("\nGenerating final video...");
        MakeVideo.generateVideo(metadataList, path);
        
        System.out.println(new File(path, "concat.txt").getAbsolutePath());
        MakeVideo.generateCollage(new File(path, "concat.txt").getAbsolutePath(), path);
        
        String postalCardVideoPath = IaFunctions.generateImage(mood, path);
        postalCardVideoPath = MakeVideo.convertImageToVideo("/output1.png", path);
        File postalCardVideo = new File(path, "output1.mp4");
        postalCardVideoPath = MakeVideo.normalizeVideo(postalCardVideo);
        System.out.println(postalCardVideoPath);
        
        /* 
        String [][]finalVideos = new String[3][1];
        finalVideos[0][0]="src/multimedia/output1";
        finalVideos[1][0]="src/multimedia/output2";
        finalVideos[2][0]="src/multimedia/output3";
        MakeVideo.generateVideo(finalVideos, path);
        FileOrganizer.deleteFile(path+"/output1.mp4");
        FileOrganizer.deleteFile(postalCardVideoPath);
        */
        System.out.println("Process completed.");
        input.close();
    }
}
