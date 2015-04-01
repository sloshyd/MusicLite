package sloshyd.uk.musiclite;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Darren Brooks on 18/03/2015.
 *
 * Lists all the songs on the device
 */
public class SongListActivity extends ListActivity {

    public static final String LOGTAG = "sloshy";//LogCat Tracking
    private ArrayList<Song> orderdSongList;
    private Datasource datasource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list_activity);
        datasource = new Datasource(this);
        datasource.open();
        orderdSongList = new ArrayList<>(datasource.findAll());

        ListView lv = (ListView) findViewById(android.R.id.list);
        SongListAdapter adapter = new SongListAdapter
                (this, R.layout.song_list_layout, orderdSongList);
        setListAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0,
                                    View v, int position, long arg3) {
                Log.i(LOGTAG, "LIST ITEM POSITION " + position);

                CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox_playList);

                //checkbox is selected
                if (cb.isChecked() == true) {

                    cb.setChecked(false);
                    //get the song object and pass into datasource to remove from playlist
                    Song song = orderdSongList.get(position);
                    datasource.removeFromPlaylist(song);
                    Log.i(LOGTAG, "Song removed from playlist " + song.getId());

                } else if (cb.isChecked() == false) {

                    cb.setChecked(true);
                    Song song = orderdSongList.get(position);
                    datasource.addSongToPlaylist(song);
                    Log.i(LOGTAG, "Song added to playlist " + song.getId());
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}