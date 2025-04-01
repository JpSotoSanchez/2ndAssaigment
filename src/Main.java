import functions.exif.ExifFunctions;
import functions.FileOrganizer;
import functions.chatGPT.IaFunctions;
import functions.ffmpeg.MakeVideo; // Import the new module

import java.io.File;
import java.util.List;
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

        String imageFinal = "PostalCard1.png";
        String videoFinal2 = "output2.mp4";
        String videoFinal3 = "output3.mp4";
        // Call the function to generate the video with FFMpeg
        System.out.println("\nGenerating final videos...");

        int width = 1920;
        int height = 1080;
        List<String> deleteFiles = MakeVideo.generateVideo(metadataList, path, "concat.txt", videoFinal2, width, height);
        
        System.out.println(new File(path, "concat.txt").getAbsolutePath());
        MakeVideo.generateCollage("concat.txt", videoFinal3, path, width, height);
    
        String postalCard = IaFunctions.makePostalCard(mood, path, imageFinal);
        
        System.out.println(path+"/"+postalCard);
        
        
        String [][]finalVideos = new String[3][1];
        finalVideos[0][0]=postalCard;
        finalVideos[1][0]=videoFinal2;
        finalVideos[2][0]=videoFinal3;
        List<String> deleteFiles2 = MakeVideo.concatenateVideos(finalVideos, path, "finalConcat.txt", "output4.mp4", width, height);
        
        
        System.out.println("Process completed.");
        
        for (String string : deleteFiles) {
            FileOrganizer.deleteFile(string);
        }
        FileOrganizer.deleteFile(path+"/"+"concat.txt");
        for (String string : deleteFiles2) {
             FileOrganizer.deleteFile(string);
        }
        FileOrganizer.deleteFile(path+"/"+"finalConcat.txt");
        input.close();
    }
}
