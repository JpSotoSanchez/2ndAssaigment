package functions.ffmpeg;

import functions.chatGPT.IaFunctions;

public class ProbarAudio {
    public static void main(String[] args) {
        String audioName = IaFunctions.generateAudioFromText("The image shows an interior space that appears to be a library or study area. There are several bookshelves filled with books in the background. In the foreground, there are a few chairs and a long table. The flooring is lightcolored tiles. The overall atmosphere seems quiet and conducive to reading or studying", "src/multimedia", "pruebaSpeech.mp3");
        System.out.println(audioName);
    }
}
