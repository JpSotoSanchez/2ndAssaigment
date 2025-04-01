

import java.util.List;

import functions.ffmpeg.MakeVideo;

public class ProbarSalidaFinal {
    public static void main(String[] args) {
        int width = 1920;
        int height = 1080;
        String path = "src/multimedia";
        String postalCard = "PostalCard1.png";
        String [][]finalVideos = new String[3][1];
        finalVideos[0][0]=postalCard;
        finalVideos[1][0]="output2.mp4";
        finalVideos[2][0]="output3.mp4";
      
        List<String> finalVideo = MakeVideo.concatenateVideos(finalVideos, path, "pruebaConcat.txt", "PruebaFinal.mp4", finalVideos[1][0], 5, width, height);
    }
}
