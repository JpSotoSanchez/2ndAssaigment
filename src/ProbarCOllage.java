import java.io.FileNotFoundException;
import java.io.IOException;

import functions.ffmpeg.MakeVideo;

public class ProbarCOllage {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String txt = "src/multimedia/concat.txt";
        MakeVideo.generateCollage(txt, "src/multimedia/images.jpg", "src/multimedia/output3.mp4");
    }
}
