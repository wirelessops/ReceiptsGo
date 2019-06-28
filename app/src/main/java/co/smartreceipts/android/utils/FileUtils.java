package co.smartreceipts.android.utils;

import androidx.annotation.Nullable;

public final class FileUtils {

    private static final CharSequence[] RESERVED_CHARS = {"|", "\\", "?", "*", "<", "\"", ":", ">", "+", "[", "]", "/", "'", "\n", "\r", "\t", "\0", "\f"};

    public static boolean filenameContainsIllegalCharacter(@Nullable String filename) {
        if (filename == null) {
            return false;
        }
        for (int i = 0; i < RESERVED_CHARS.length; i++) {
            if (filename.contains(RESERVED_CHARS[i])) {
                return true;
            }
        }
        return false;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String omitIllegalCharactersFromFileName(@Nullable String filename) {
        if (filename == null) {
            return "";
        }
        for (int i = 0; i < RESERVED_CHARS.length; i++) {
            filename = filename.replace(RESERVED_CHARS[i], "");
        }
        return filename;
    }
}
