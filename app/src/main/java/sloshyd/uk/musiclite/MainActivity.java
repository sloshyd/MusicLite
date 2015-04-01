package sloshyd.uk.musiclite;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import sloshyd.uk.musiclite.MusicService.MusicBinder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends ActionBarActivity {

    public static final String LOGTAG = "sloshy";//LogCat Tracking
    private ArrayList<Song> songList;
    //Start Service
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    //public static String songDetails = "Loading.....";
    public TextView songInfo;
    //public Bitmap coverArt;
    private Long albumId;
    private Datasource datasource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //stops screen locking
        //Instanciate variables within onCreate()
        songList = new ArrayList<>();

        datasource = new Datasource(this);
        datasource.open();
        //datasource.createDummyPlaylist();
        getSongList();




        //set up Broadcast receiver to capture playing song details
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("songDetails"));

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String songName = intent.getStringExtra("songName");
            String songArtist = intent.getStringExtra("songArtist");
            albumId = intent.getLongExtra("albumId", 0);// need to have a default value

            //  ... react to local broadcast message
            songInfo = (TextView) findViewById(R.id.textViewSongName);
            songInfo.setText(songName);
            songInfo = (TextView) findViewById(R.id.textViewArtist);
            songInfo.setText(songArtist);
            //get image
            GetSongCoverArt artwork = new GetSongCoverArt();
            artwork.execute(albumId);
        }
    };


    private void onUpdate(){
        musicSrv.playSong();
    }
    //connect to the Service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicService.MusicBinder) service;
            //get Service
            musicSrv = binder.getMusicService();
            //pass list to service
            musicSrv.setList(songList);
            musicBound = true;
            musicSrv.playSong();
        }

        @Override
        //breaks Binding and closes the service connection in system crash not directly called
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    //onStart() called after onCreate() and is called after a onResume()
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        } else {
            Log.i(LOGTAG, " Error onStart() Method");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        datasource.open();


        Log.i(LOGTAG, "onResume() called");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 ){
            Log.i(LOGTAG, "onActivityResults Called");
            //finish();
            // refresh playlist held by MusicService
            datasource.open();
            getMusicList();
            Log.i(LOGTAG, " refreshed song list");
            musicSrv.setList(songList);
            musicSrv.playSong();
            Log.i(LOGTAG, " play song");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
         //   return true;
       // }
        if (id == R.id.play_list) {

            Intent i = new Intent(MainActivity.this, SongListActivity.class);
            startActivityForResult(i, 1000);// this will notify the MainActivity when activity stops
            // 1000 is any code to describe the calling activity here MainActivity = 1000

        }

        return super.onOptionsItemSelected(item);
    }

    public void getSongList() {
        //method to retrieve list of songs
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        // Get the content:// style URI for the audio media table on the given volume.
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumId = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long thisAlbumId = musicCursor.getLong(albumId);
                //songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbumId));
                Log.i(LOGTAG, thisTitle + " : ID: " + thisId);
                Song song = (new Song(thisId, thisTitle, thisArtist, thisAlbumId));

                datasource.createData(song);//adds data to the ALL_SONGS

            }
            while (musicCursor.moveToNext());


            getMusicList();

        }
    }

    private void getMusicList() {
        if (datasource.isPlaylist() == false) {
            Log.i(LOGTAG, "isPlayList " + datasource.isPlaylist());
            songList = (ArrayList<Song>) datasource.findAll();
            shuffleSongs();

        } else {
            Log.i(LOGTAG, "isPlayList " + datasource.isPlaylist());
            songList = (ArrayList<Song>) datasource.findMyPlaylist();
            shuffleSongs();
        }
    }

    /**
     * *********************************************************
     * Method calls to MusicService
     */
    public void skip(View view) {
        findViewById(R.id.checkBox).setEnabled(true);
        musicSrv.playNext();
    }

    public void pause(View view) {
        //need to assign action based on the state of the checkbox
        boolean checked = ((CheckBox) view).isChecked();

        if (checked) {
            musicSrv.pause();

        } else {
            musicSrv.resume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        datasource.close();

        Log.i(LOGTAG, "onPause() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();// calling super class allows certain clean up and avoids error
        stopService(playIntent);
        unbindService(musicConnection);
        musicSrv = null;
        datasource.close();
        Log.i(LOGTAG, "onDestroy() called");

    }

    @Override
    protected void onStop() {
        super.onStop();// calling super class allows ceratin clean up and avoids error
//                    stopService(playIntent);


        datasource.close();

    }

    private void shuffleSongs() {
        Collections.shuffle(songList);
        for (int i = 0; i < songList.size(); i++) {
            Log.i(LOGTAG, songList.get(i).getTitle());
        }
    }


    //method to get album art
    private class GetSongCoverArt extends AsyncTask<Long, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(Long... params) {

            Context context = MainActivity.this;
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uriSongArtWork = ContentUris.withAppendedId(sArtworkUri, params[0]);
            //not params[0] as params is an array but only one is being passed so will be in position [0]
            Log.d(MainActivity.LOGTAG, uriSongArtWork.toString());
            ContentResolver res = context.getContentResolver();
            try {
                InputStream in = res.openInputStream(uriSongArtWork);
                return BitmapFactory.decodeStream(in);
                //need to Return the Bitmap this is expected from the AsyncTask definition
                //and will pass to the onPostExecute()
            } catch (FileNotFoundException e) {
                Log.e(MainActivity.LOGTAG, e.getMessage());

            }
            Log.i(LOGTAG," No image found");
            return null;

        }

        @Override
        protected void onPreExecute() {
            //Setup is done here
        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            //Update a progress bar here, or ignore it, it's up to you
        }

        @Override
        protected void onPostExecute(Bitmap img) {
            ImageView albumArtImage = (ImageView) findViewById(R.id.albumArtView);
            if (img != null) {
                albumArtImage.setImageBitmap(img);
            }
            else {
                    Log.i(LOGTAG," Loading no Album Artwork");
                    Bitmap icon = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                            R.drawable.noalbumart);
                    albumArtImage.setImageBitmap(icon);

            }
        }

    }
}


