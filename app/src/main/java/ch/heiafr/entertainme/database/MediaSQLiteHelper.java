package ch.heiafr.entertainme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Lucas on 06.06.2017.
 */
public class MediaSQLiteHelper extends SQLiteOpenHelper {

    public static final String KEY_ID = "_id";
    public static final String KEY_ID_COLUMN = "ID_COLUMN";
    public static final String KEY_TYPE_COLUMN = "TYPE_COLUMN";


    public static final String DATABASE_NAME = "ToWatchDataBase.db";
    public static final String TABLE_NAME = "ToWatchTable";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_CREATE =
            "create table " + TABLE_NAME + " (" + KEY_ID +
                    " integer primary key autoincrement, " +
                    KEY_ID_COLUMN + " integer not null, " +
                    KEY_TYPE_COLUMN + " integer not null);";

    private SQLiteDatabase db;
    private Cursor cursor;

    public MediaSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_NAME);
        onCreate(db);
    }


    public ArrayList<Integer> getMediasFromDB(int media_type) {
        ArrayList<Integer> mediaList = new ArrayList<Integer>();
        String[] result_column = new String[]{KEY_ID, KEY_ID_COLUMN, KEY_TYPE_COLUMN};
        String where = KEY_TYPE_COLUMN + "=" + media_type;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;
        db = getWritableDatabase();
        cursor = db.query(TABLE_NAME, result_column, where, whereArgs, groupBy, having, order);
        while (cursor.moveToNext()) {
            int mediaId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_COLUMN));
            mediaList.add(mediaId);
        }
        cursor.close();
        return mediaList;
    }

    public boolean isMediaInDB(int mediaId) {
        boolean isPresent = false;
        String[] result_column = new String[]{KEY_ID, KEY_ID_COLUMN, KEY_TYPE_COLUMN};
        String where = KEY_ID_COLUMN + "=" + mediaId;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;
        db = getWritableDatabase();
        cursor = db.query(TABLE_NAME, result_column, where, whereArgs, groupBy, having, order);
        while (cursor.moveToNext()) {
            int mediaIdFound = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_COLUMN));
            if (mediaId == mediaIdFound)
                isPresent = true;
        }
        cursor.close();
        return isPresent;
    }

    public void addNewMedia(int mediaID, int media_type) {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID_COLUMN, mediaID);
        values.put(KEY_TYPE_COLUMN, media_type);
        db.insert(TABLE_NAME, null, values);
    }

    public void deleteMedia(int mediaID, int media_type) {
        String where = KEY_ID_COLUMN + "=" + mediaID + " AND " + KEY_TYPE_COLUMN + "=" + media_type;
        String whereArgs[] = null;
        db = getWritableDatabase();
        db.delete(TABLE_NAME, where, whereArgs);
    }
}