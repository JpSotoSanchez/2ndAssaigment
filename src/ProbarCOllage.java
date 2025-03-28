import functions.FileOrganizer;
import functions.ffmpeg.MakeVideo;

public class ProbarCOllage {
    public static void main(String[] args) {
        String txt = "src/multimedia/concat.txt";
        String imagen = FileOrganizer.convertImageToBase64("src/multimedia/shark1.jpg");
        System.out.println(imagen);
    }
}
