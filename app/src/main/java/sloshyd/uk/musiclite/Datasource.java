package sloshyd.uk.musiclite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Darren Brooks on 23/03/2015.
 */
public class Datasource {
    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;
    public static final String LOGTAG = "sloshy";

    //This array is created so it can be used in the Cursor query to pull all the columns in the
    //database
    private static final String[] allColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,
            DBHelper.COLUMN_ARTIST,
            DBHelper.COLUMN_ALBUM_ID};

    public Datasource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        Log.i(LOGTAG, "Database opened");
        database = dbHelper.getWritableDatabase();

    }

    public void close() {
        Log.i(LOGTAG, "Database closed");
        dbHelper.close();
    }

    public boolean createData(Song song) {
        // by creating long result allows managing of data that is already in the database
        ContentValues values = new ContentValues();//use ContentValues to store data

        values.put(DBHelper.COLUMN_ID, song.getId());
        values.put(DBHelper.COLUMN_TITLE, song.getTitle());
        values.put(DBHelper.COLUMN_ARTIST, song.getArtist());
        values.put(DBHelper.COLUMN_ALBUM_ID, song.getAlbumId());
        long result = database.insert(DBHelper.TABLE_ALL_SONGS, null, values);
        Log.i(LOGTAG, "Row inserted into Database");
        return (result != -1);

    }

    //pulls data from database and puts it into List
    private List<Song> cursorToList(Cursor cursor) {
        List<Song> songs = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                song.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE)));
                song.setArtist(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ARTIST)));
                song.setAlbum_Id(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ALBUM_ID)));

                songs.add(song);
            }
        }
        return songs;
    }

    //Get all the rows of data in the
    public List<Song> findAll() {
        //returns cursor ordered by column title
        Cursor cursor = database.query(DBHelper.TABLE_ALL_SONGS, allColumns,
                null, null, null, null, DBHelper.COLUMN_TITLE);

        Log.i(LOGTAG, "Returned " + cursor.getCount() + " rows");
        List<Song> songs = cursorToList(cursor);
        return songs;
    }

    //Remove id from Playlist table
    public boolean removeFromPlaylist(Song song) {
        String where = DBHelper.COLUMN_ID + "=" + song.getId();
        int result = database.delete(DBHelper.TABLE_PLAYLIST, where, null);
        return (result == 1); // if result is not equal to deleting 1 row then it will return false;
    }

    //joins tables and retrieves the data for all the items in the myTours table
    public List<Song> findMyPlaylist() {

        String query = "SELECT allSongs.* FROM " +
                "allSongs JOIN playlist ON " +
                "allSongs.columnId = playlist.columnId";
        Cursor cursor = database.rawQuery(query, null);//rawQuery(sql, param)

        Log.i(LOGTAG, "Returned " + cursor.getCount() + " rows");

        List<Song> songs = cursorToList(cursor);
        return songs;
    }

    //add a song COLUMN_ID to TABLE_PLAYLIST if result = -1 then the ID is already in the table
    public boolean addSongToPlaylist(Song song) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_ID, song.getId());
        long result = database.insert(DBHelper.TABLE_PLAYLIST, null, values);
        return (result != -1);// if -1 then the TourId is already in the db
    }

    //use for testing
    public void createDummyPlaylist() {
        database.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_PLAYLIST);
        database.execSQL("INSERT INTO playlist (columnId) VALUES (175)");
        database.execSQL("INSERT INTO playlist (columnId) VALUES (453)");
        database.execSQL("INSERT INTO playlist (columnId) VALUES (456)");
        database.execSQL("INSERT INTO playlist (columnId) VALUES (459)");

    }

    public boolean isPlaylist() {
        String query = "SELECT * FROM playlist";
        //use SELECT NOT COUNT as COUNT will always return 1 record
        Cursor cursor = database.rawQuery(query, null);
        int result = cursor.getCount();
        if (result == 1) {
            //table does not exists with one row = (no data)
            Log.i(LOGTAG, "No valid playlist exist count() = " + result);
            return false;
        } else if (result > 1) {

            Log.i(LOGTAG, "Valid PlayList exist count() =" + result);
            return true;

        } else if (result <= 0) {
            Log.i(LOGTAG, "No valid playlist count() =" + result);
            return false;
        }
        return false;


    }

    public void  numberSongsInPlaylist(){
        String query = "SELECT COUNT(*) FROM 'playlist'";

        Cursor cursor = database.rawQuery(query, null);
        int result = cursor.getCount();
        Log.i(LOGTAG," Songs in playlist table = " + result);

    }
    public boolean inPlaylist(Song song) {

        String query = "SELECT * FROM " +
                "playlist WHERE columnId = " + song.getId();
       Cursor cursor = database.rawQuery(query, null);
        int result = cursor.getCount();
        //if no count then song is not in the playlist
        if (result == 1) {
            Log.i(LOGTAG, "results of song in playlist = " + result);
            return true;
        } else if (result == 0){
            Log.i(LOGTAG, "results of song in playlist = " + result);
            return false;
        }
        return false;
    }
}
