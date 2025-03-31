package functions.exif;

public class ProbarDuracion {
    public static void main(String[] args) {
        int duracion = ExifFunctions.extractDuration("src/multimedia", "output4.mp4");
        System.out.println(duracion);
    }
}
