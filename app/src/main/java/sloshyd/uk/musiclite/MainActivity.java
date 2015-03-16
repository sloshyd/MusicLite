package sloshyd.uk.musiclite;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import sloshyd.uk.musiclite.MusicService.MusicBinder;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends ActionBarActivity {

    private final String LOGTAG ="sloshy";//LogCat Tracking
    private ArrayList<Song> songList;
    //Start Service
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    public static String songDetails = "Loading.....";
    public TextView songInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //stops screen locking
        //Instanciate variables within onCreate()
        songList = new ArrayList<>();
        getSongList();
        songInfo = (TextView) findViewById(R.id.songDetails);
        songInfo.setText("Loading Data");

        //set up Broadcast receiver to capture playing song details
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("songDetails"));

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String songName = intent.getStringExtra("songName");
            String songArtist = intent.getStringExtra("songArtist");

            //  ... react to local broadcast message
            songInfo = (TextView) findViewById(R.id.songDetails);
            songInfo.setText(songName + "    by   " + songArtist);

        }
    };


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
    protected void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        else{
            Log.i(LOGTAG," Error onStart() Method");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getSongList()    {
        //method to retrieve list of songs
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        // Get the content:// style URI for the audio media table on the given volume.
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
                Log.i(LOGTAG, thisTitle);
            }
            while (musicCursor.moveToNext());

            Log.i(LOGTAG, "****************SHUFFLED LIST****************");
            shuffleSongs();

        }
    }

    /************************************************************
     * Method calls to MusicService
     */
    public void skip(View view){
        findViewById(R.id.checkBox).setEnabled(true);
        musicSrv.playNext();
    }

    public void pause(View view){
        //need to assign action based on the state of the checkbox
        boolean checked = ((CheckBox) view).isChecked();

        if(checked){
            musicSrv.pause();

        }
        else {
            musicSrv.resume();
        }

    }

    @Override
    protected void onDestroy() {

        stopService(playIntent);
        unbindService(musicConnection);
        musicSrv = null;

    }

    @Override
    protected void onStop() {

        System.exit(0);

    }


    private void shuffleSongs() {
        Collections.shuffle(songList);
        for (int i = 0; i < songList.size() ; i++) {
            Log.i(LOGTAG, songList.get(i).getTitle());
        }
    }
}
