package functions.ffmpeg;

public class ProbarSalidaFinal {
    public static void main(String[] args) {
        String [][]finalVideos = new String[3][1];
        finalVideos[0][0]="PostalCard1.png";
        finalVideos[1][0]="output2.mp4";
        finalVideos[2][0]="output3.mp4";
        String finalVideo = MakeVideo.concatenateVideos(finalVideos, "src/multimedia", "PruebaOutput4.mp4", 1920, 1080);
        System.out.println("Process completed.");
    }
}
