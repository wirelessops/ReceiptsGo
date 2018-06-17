package wb.android.storage;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class InternalStorageManager extends StorageManager {
	
	private static final String TAG = "InternalStorageManager";
	private static final boolean D = false;

	protected InternalStorageManager(Context context) {
		super(new InternalDirectoryRoot(context));
	}

}