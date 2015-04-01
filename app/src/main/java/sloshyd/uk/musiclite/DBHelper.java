package sloshyd.uk.musiclite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Darren Brooks on 23/03/2015.
 *
 * Helper Creates two tables
 * allSongs - holds all songs held on the device it is dropped and recreated on application load
 * playlist - holds reference to the songs that have been selected as to include in playlist and
 * is not dropped.  If no playlist table exists then all the songs in the allSong table are played.
 *
 * Live Database numbers start at 100
 */
public class DBHelper extends SQLiteOpenHelper{

    private static final String LOGTAG = "sloshy" ;//debug tracking
    private static final String DATABASE_NAME = "playlist.db";
    private static final int DATABASE_VERSION = 100;

    public static final String TABLE_ALL_SONGS = "allSongs";
    public static final String COLUMN_ID = "columnId";
    public static final String COLUMN_TITLE ="songTitle";
    public static final String COLUMN_ARTIST="songArtist";
    public static final String COLUMN_ALBUM_ID ="ablumId";

    //create TABLE_ALL_SONGS
    private static final String TABLE_CREATE_ALL_SONGS =
            "CREATE TABLE " + TABLE_ALL_SONGS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_ARTIST + " TEXT, " +
                    COLUMN_ALBUM_ID + " NUMERIC " + ")";
    //create TABLE_PLAYLIST
    public static final String TABLE_PLAYLIST="playlist";
    private static final String TABLE_CREATE_PLAYLIST =
            "CREATE TABLE " + TABLE_PLAYLIST + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY " +
                    ")";



    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_ALL_SONGS);
        Log.i(LOGTAG, "TABLE_CREATE_ALL_SONGS has been created");
        db.execSQL(TABLE_CREATE_PLAYLIST);
        Log.i(LOGTAG, "TABLE_CREATE_PLAYLIST has been created");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
        onCreate(db);

        Log.i(LOGTAG, "Database has been upgraded from " +
                oldVersion + " to " + newVersion);
    }
}
