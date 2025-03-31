import functions.chatGPT.IaFunctions;

public class ProbarImagenAImagen {
    public static void main(String[] args) {
        String imagenGenerada = IaFunctions.generatePostalCardFromBase64("src/multimedia", "frame.png", "PostalCard2.png");
        System.out.println(imagenGenerada);
    }
}
