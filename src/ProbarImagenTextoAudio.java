import functions.chatGPT.IaFunctions;

public class ProbarImagenTextoAudio {
    public static void main(String[] args) {
        String description = IaFunctions.generateDescription("https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
        System.out.println("Pollo loco");
        System.out.println(description);
        IaFunctions.generateAudio("Pollo Loco", "src/multimedia", "speech.mp3");
    }
}
