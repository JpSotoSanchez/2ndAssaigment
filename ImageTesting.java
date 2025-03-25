import java.io.File;
import java.util.Scanner;
import functions.chatGPT.IaFunctions;
import functions.ffmpeg.MakeVideo;

public class ImageTesting {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Solicitar la ruta de archivos multimedia
        System.out.println("Enter the path of the images/videos: ");
        String path = input.nextLine();

        // Solicitar el mood del video
        System.out.println("Enter the mood of the video: ");
        String mood = input.nextLine();

        // Generar imagen con DALL·E 2
        String imagePath = IaFunctions.generateImage(mood, path);
        
        // Verificar si la imagen se generó correctamente
        if (imagePath == null || !(new File(imagePath)).exists()) {
            System.err.println("Error: La imagen no se generó correctamente.");
            input.close();
            return;
        }

        // Convertir imagen a video
        String videoPath = MakeVideo.convertImageToVideo(imagePath, path + "/output1.mp4");

        // Verificar si el video se generó correctamente
        if (videoPath == null || !(new File(videoPath)).exists()) {
            System.err.println("Error: El video no se generó correctamente.");
            input.close();
            return;
        }

        // Normalizar video
        File postalCardVideo = new File(videoPath);
        String normalizedVideoPath = MakeVideo.normalizeVideo(postalCardVideo);

        // Mostrar el resultado final
        System.out.println("Video final guardado en: " + normalizedVideoPath);

        input.close();
    }
}
