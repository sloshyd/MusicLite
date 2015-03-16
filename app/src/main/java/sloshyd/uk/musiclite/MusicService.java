package sloshyd.uk.musiclite;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * Created by Darren Brooks on 13/03/2015.
 * Create service (MusicService) by extending Service gives access to IBinder onBind()
 *
 * Binder called MusicBinder is created as an inner class.  Binder allows the activity class
 * MainActivity to communicate directly with the Service without having to use intent)
 *
 * When Service is first created onCreate() is called
 *
 * Information from the service is broadcast via the LocalBroadCastManager (this is safe and more
 * efficient way of transferring data within an application but cannot do done across applications)
 *
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {


    private final String LOGTAG ="sloshy";//LogCat Tracking
    //NOTE: class declaration is needed as above without it onPrepared() method is not called
    //which is required by the service player.prepareAsync(); which creates a Thread to run the player
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();
    private boolean isPaused = false;
    private String songName;
    private String songArtist;

    @Override
    public void onCreate() {
        super.onCreate();

        //create MusicPlayer
        songPosition = 0;
        player = new MediaPlayer();
        initPlayer();

    }

    public void initPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //need to use WakeLock to allow CPU usage but FULL_WAKE_LOCK which maintains
        //screen being on was deprecated so in the activity
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
    //release resources
        player.stop();
        player.release();
        return false;
    }


    public void playSong(){
        //
        player.reset();
        //get song
        Song playSong = songs.get(songPosition);
        long currSong = playSong.getId();

        //set uri
        Uri trackUri = ContentUris.withAppendedId
                (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        //this URI appends the path and the name of the current song
        try {
            player.setDataSource(getApplicationContext(), trackUri);
            Intent intent = new Intent("songDetails");
            broadcastSongDetails(intent);


        }
        catch (Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source");
        }

        player.prepareAsync();
        //Successful invoke of this method in a valid state transfers
        // the object to the Preparing state.
        // (see http://developer.android.com/reference/android/media/MediaPlayer.html)

    }

    private void broadcastSongDetails(Intent intent){
        //gets song information and broadcasts it
        songName = songs.get(songPosition).getTitle();
        songArtist = songs.get(songPosition).getArtist();
        intent.putExtra("songName", songName);
        intent.putExtra("songArtist", songArtist);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }



    @Override
    public void onCompletion(MediaPlayer mp) {
        //when a song is finished this method is called.
        mp.reset();
        playNext();

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //method is only available because on the onPrepared Listener interface
        // must be used to start player as called by player.prepareAsync()
        mp.start();

    }



    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(LOGTAG,"Error with Music Player");
        return false;
    }

    public void playNext(){
        songPosition++;
        if(songPosition >= songs.size()){
            songPosition =0;
        }

        playSong();
    }

    public void pause(){
        player.pause();
        isPaused = true;
    }

    public void resume(){
        //resume playing from pause
        player.start();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

    }


    public String currentSongName(){
        return songName;

    }
    public String currentSongArtist(){
        return songArtist;
    }

    public boolean isPaused(){
        return isPaused;
    }

    //Binder Inner Class
    public class MusicBinder extends Binder {
        MusicService getMusicService(){

            return MusicService.this;

        }
    }

}
