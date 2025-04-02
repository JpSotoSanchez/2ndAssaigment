import functions.exif.ExifFunctions;
import functions.FileOrganizer;
import functions.chatGPT.IaFunctions;
import functions.ffmpeg.MakeVideo;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Set up a scanner to read user input from the console
        Scanner input = new Scanner(System.in);

        // Ask the user for the folder path where their images/videos are stored
        System.out.println("Enter the path of the images/videos: ");
        String path = input.nextLine();

        // Ask for the "mood" (this might be used later for AI-generated content)
        System.out.println("Enter the mood of the video: ");
        String mood = input.nextLine();

        // Fetch metadata (like creation date, rotation) for all media files in the folder
        String[][] metadataList = ExifFunctions.GatherVideos(path);

        // If no files were found, exit early to avoid errors
        if (metadataList.length == 0) {
            System.out.println("No files with valid metadata found.");
            input.close();
            return;
        }

        // Sort the files by their creation date (oldest first)
        metadataList = FileOrganizer.sortByDate(metadataList);

        // Show the user the sorted list of files (just for transparency)
        System.out.println("\nFiles sorted chronologically:");
        for (String[] meta : metadataList) {
            System.out.println("File: " + meta[0] + ", Date: " + meta[1] + ", Rotation: " + meta[2]);
        }

        // Define output filenames for the results we'll generate
        String imageFinal = "PostalCard1.png";       // AI-generated postal card
        String videoFinal2 = "output2.mp4";          // First video output
        String videoFinal3 = "output3.mp4";          // Second video output (collage)

        // Time to process the files!
        System.out.println("\nGenerating final videos...");

        // Standard video dimensions (1080p)
        int width = 1920;
        int height = 1080;

        // Step 1: Create a video from the sorted media files
        List<String> deleteFiles = MakeVideo.generateVideo(metadataList, path, "concat.txt", videoFinal2, width, height);

        // Debug: Show where the temporary concatenation file was saved
        System.out.println(new File(path, "concat.txt").getAbsolutePath());

        // Step 2: Create a collage video from the same files
        MakeVideo.generateCollage("concat.txt", videoFinal3, path, width, height);

        // Step 3: Use AI to generate a postal card based on the user's "mood"
        String postalCard = IaFunctions.makePostalCard(mood, path, imageFinal);
        System.out.println(path + "/" + postalCard);  // Show where the postal card was saved

        // Step 4: Combine everything (postal card + both videos) into one final video
        String[][] finalVideos = new String[3][1];
        finalVideos[0][0] = postalCard;  // Add postal card
        finalVideos[1][0] = videoFinal2; // Add first video
        finalVideos[2][0] = videoFinal3; // Add second video
        List<String> deleteFiles2 = MakeVideo.concatenateVideos(finalVideos, path, "finalConcat.txt", "output4.mp4", width, height);

        // Cleanup time! Delete temporary files to avoid clutter
        System.out.println("Process completed.");
        for (String string : deleteFiles) {
            FileOrganizer.deleteFile(string);
        }
        FileOrganizer.deleteFile(path + "/" + "concat.txt");
        for (String string : deleteFiles2) {
            FileOrganizer.deleteFile(string);
        }
        FileOrganizer.deleteFile(path + "/" + "finalConcat.txt");

        // Close the scanner to prevent resource leaks
        input.close();
    }
}