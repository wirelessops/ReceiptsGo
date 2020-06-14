package wb.android.storage;

import android.content.Context;

public final class SDCardFileManager extends StorageManager {
	
	//logging variables
    private static final boolean D = false;
    private static final String TAG = "SDCardFileManager";
	
	SDCardFileManager(Context context) {
		super(new ExternalDirectoryRoot(context));
	}
	
}