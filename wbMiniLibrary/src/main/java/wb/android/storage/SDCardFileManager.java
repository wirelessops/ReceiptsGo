package wb.android.storage;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wb.android.BuildConfig;

public final class SDCardFileManager extends StorageManager {
	
	//logging variables
    private static final boolean D = false;
    private static final String TAG = "SDCardFileManager";
	
	protected SDCardFileManager(Context context) {
		super(new ExternalDirectoryRoot(context));
	}
	
}