import java.util.List;
import java.util.Scanner;

import functions.FileOrganizer;
import functions.exif.ExifFunctions;
import functions.ffmpeg.MakeVideo;

public class ProbarNuevoGenerateVideo {
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

        String imageFinal = "PostalCard1.png";
        String videoFinal2 = "output2.mp4";
        String videoFinal3 = "output3.mp4";
        // Call the function to generate the video with FFMpeg
        System.out.println("\nGenerating final videos...");

        int width = 1920;
        int height = 1080;
        List<String> deleteFiles = MakeVideo.generateVideo(metadataList, path, "concat.txt", videoFinal2, width, height);
    }    
}
