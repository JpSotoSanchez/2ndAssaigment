package functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileOrganizer {
    public static String[][] sortByDate(String[][] metadataArray) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss"); // ExifTool format

        Arrays.sort(metadataArray, (meta1, meta2) -> {
            try {
                // Get dates, if no date, set a distant default one
                Date date1 = dateFormat.parse(meta1[1].equals("Unknown") ? "9999:12:31 23:59:59" : meta1[1]);
                Date date2 = dateFormat.parse(meta2[1].equals("Unknown") ? "9999:12:31 23:59:59" : meta2[1]);

                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        return metadataArray; // Return the sorted array
    }
}
