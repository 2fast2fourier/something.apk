package net.fastfourier.something.util;

import android.os.Build;

/**
 * Various utility functions for Something dealing with checking SDK verisons
 */
public class SomeUtils {
    /**
     * Checks to see if a particular code is less than or equal to the current
     * running OS's SDK.
     *
     * @param code API code to test again
     * @return boolean
     */
    private static boolean isAtLeast(int code) {
        return Build.VERSION.SDK_INT >= code;
    }

    /**
     * Check if the running OS is at least Jellybean (SDK 16, 4.1 to 4.3)
     *
     * @return boolean
     */
    public static boolean isJellybean() {
        return isAtLeast(Build.VERSION_CODES.JELLY_BEAN);
    }

    /**
     * Check if the running OS is at least Kit Kat (SDK 19, 4.4)
     *
     * @return boolean
     */
    public static boolean isKitKat() {
        return isAtLeast(Build.VERSION_CODES.KITKAT);
    }

    /**
     * Check if the running OS is at least Lollipop (SDK 21, 5.0)
     *
     * @return boolean
     */
    public static boolean isLollipop() {
        return isAtLeast(Build.VERSION_CODES.LOLLIPOP);
    }

    /**
     * Check if the running OS is at least Marshmallow (SDK 23, 6.0)
     *
     * @return boolean
     */
    public static boolean isMarshmallow() {
        return isAtLeast(Build.VERSION_CODES.M);
    }

}
