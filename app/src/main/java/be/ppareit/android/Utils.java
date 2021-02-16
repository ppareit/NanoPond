package be.ppareit.android;

public class Utils {

    public static void sleepIgnoreInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
