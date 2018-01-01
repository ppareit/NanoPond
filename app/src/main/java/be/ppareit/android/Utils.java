package be.ppareit.android;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static void sleepIgnoreInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static CharSequence openRawTextFile(Resources resources, int id) throws IOException {
        StringBuilder result = new StringBuilder();
        try (InputStream ins = resources.openRawResource(id);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result;
    }

}
