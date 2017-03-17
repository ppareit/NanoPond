package be.ppareit.android;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

public class Utils {

    public static <T extends Preference> T findPref(PreferenceActivity pa, CharSequence key) {
        return (T) pa.findPreference(key);
    }

    public static <T extends View> T findView(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    public static <T extends View> T findView(View view, int id) {
        return (T) view.findViewById(id);
    }

    public static void sleepIgnoreInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

}
