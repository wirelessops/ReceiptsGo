package co.smartreceipts.android.persistence.database.tables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

public class TestSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String NAME = "temporary.db";

    public TestSQLiteOpenHelper(@NonNull Context context) {
        super(context, NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

}
