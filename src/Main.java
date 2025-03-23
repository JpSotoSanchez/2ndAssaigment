import functions.chatGPT.IaFunctions;
import functions.exif.ExifFunctions;
import functions.FileOrganizer;
import functions.ffmpeg.MakeVideo; // Import the new module

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        // Request the multimedia file path
        System.out.println("Enter the path of the images/videos: ");
        String path = input.nextLine();

        // Get the metadata of the files
        String[][] metadataList = ExifFunctions.GatherVideos(path);

        // Check if there are any data
        if (metadataList.length == 0) {
            System.out.println("No files with valid metadata found.");
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

        System.out.println("Process completed.");
    }
}
