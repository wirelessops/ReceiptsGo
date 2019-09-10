package co.smartreceipts.android.utils.shadows;

import com.tom_roush.fontbox.util.autodetect.FontDirFinder;
import com.tom_roush.fontbox.util.autodetect.FontFileFinder;
import com.tom_roush.fontbox.util.autodetect.MacFontDirFinder;
import com.tom_roush.fontbox.util.autodetect.UnixFontDirFinder;
import com.tom_roush.fontbox.util.autodetect.WindowsFontDirFinder;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Note: this is basically just a raw copy of {@link FontFileFinder}, but it removes the Android specific
 * stuff, which was commented out. We do this, so Robolectric can find the local file system fonts
 */
@Implements(FontFileFinder.class)
public class ShadowFontFileFinder {

    @RealObject
    private FontFileFinder realFontFileFinder;

    private FontDirFinder fontDirFinder = null;

    private FontDirFinder determineDirFinder() {
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            return new WindowsFontDirFinder();
        } else if (osName.startsWith("Mac")) {
            return new MacFontDirFinder();
        } else {
            return new UnixFontDirFinder();
        }
    }

    @Implementation
    public List<URI> find() {
        if (fontDirFinder == null) {
            fontDirFinder = determineDirFinder();
        }
        List<File> fontDirs = fontDirFinder.find();
        List<URI> results = new java.util.ArrayList<>();
        for (File dir : fontDirs) {
            walk(dir, results);
        }
        return results;
    }

    /**
     * walk down the directory tree and search for font files.
     *
     * @param directory the directory to start at
     * @param results names of all found font files
     */
    private void walk(File directory, List<URI> results) {
        // search for font files recursively in the given directory
        if (directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                int numOfFiles = fileList.length;
                for (int i=0;i<numOfFiles;i++) {
                    File file = fileList[i];
                    if (file.isDirectory()) {
                        // skip hidden directories
                        if (file.getName().startsWith(".")) {
                            continue;
                        }
                        walk(file, results);
                    }
                    else {
                        if (checkFontFile(file)) {
                            results.add(file.toURI());
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if the given name belongs to a font file.
     *
     * @param file the given file
     * @return true if the given filename has a typical font file ending
     */
    private boolean checkFontFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".pfb") || name.endsWith(".ttc");
    }
}
