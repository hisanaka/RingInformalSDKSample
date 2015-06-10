package jp.or.ixqsware.ringinformalsdksample.db;

import static android.provider.BaseColumns._ID;

/**
 * Created by hnakadate on 15/04/19.
 */
public class Constants {
    public static final String DATABASE_NAME = "sample";
    public static final int DATABASE_VERSION = 1;

    public static final String COLUMN_GESTURE_ID = "gesture_id";
    public static final String COLUMN_TITLE = "title";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME
            + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_GESTURE_ID + " INTEGER, "
            + COLUMN_TITLE + " TEXT);";

    public static final String SQL_DELETE = "DROP TABLE IF EXISTS " + DATABASE_NAME + ";";

    public static final String SQL_REGISTER = "INSERT INTO " + DATABASE_NAME
            + " (" + COLUMN_GESTURE_ID + ", " + COLUMN_TITLE + ")"
            + " values (?, ?);";

    public static final String SQL_SELECT = "SELECT * FROM " + DATABASE_NAME
            + " WHERE " + COLUMN_GESTURE_ID + " = ?;";
}
