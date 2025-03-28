import functions.FileOrganizer;
import functions.ffmpeg.MakeVideo;

public class ProbarCOncat {
    public static void main(String[] args) {
        boolean bandera = FileOrganizer.executeCMDCommand(new String[]{
            "ffmpeg", "-f", "concat", "-safe", "0", "-i", "src/multimedia/concat.txt",
            "-c:v", "libx264", "-crf", "23", "-preset", "fast",
            "-c:a", "aac", "-b:a", "192k", "-y", "src/multimedia/output5.mp4"
        });
        
    }
}
