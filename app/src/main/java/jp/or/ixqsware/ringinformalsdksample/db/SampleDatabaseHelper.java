package jp.or.ixqsware.ringinformalsdksample.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import static jp.or.ixqsware.ringinformalsdksample.db.Constants.*;

/**
 * Created by hnakadate on 15/04/19.
 */
public class SampleDatabaseHelper extends SQLiteOpenHelper {
    private static SampleDatabaseHelper instance;

    public SampleDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SampleDatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new SampleDatabaseHelper(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createEmptyDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createEmptyDatabase(db);
    }

    public String getGestureTitle(SQLiteDatabase db, long gestureId) {
        Cursor mCursor = db.rawQuery(SQL_SELECT, new String[]{Long.toString(gestureId)});
        String title = "";
        if (mCursor.moveToFirst()) {
            int colTitle = mCursor.getColumnIndex(COLUMN_TITLE);
            title = mCursor.getString(colTitle);
        }
        mCursor.close();
        return title;
    }

    public boolean registerGestureTitle(SQLiteDatabase db, Long gestureId, String title) {
        boolean result = true;
        db.beginTransaction();
        try {
            SQLiteStatement statement = db.compileStatement(SQL_REGISTER);
            statement.bindString(1, Long.toString(gestureId));
            statement.bindString(2, title);
            statement.executeInsert();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            result = false;
        } finally {
            db.endTransaction();
        }
        return  result;
    }

    private void createEmptyDatabase(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE);
        db.execSQL(SQL_CREATE);
    }
}
