package sloshyd.uk.musiclite;

import android.app.Activity;
import android.content.Context;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Darren Brooks on 18/03/2015.
 * This custom adapter is called by SongListActivity to manage the display
 * of the array list
 */
public class SongListAdapter extends ArrayAdapter<Song> {

    private Context context;
    private List<Song> songList;
    private CheckBox checkbox;
    private Datasource datasource;

       //need to override the superclass constructor
    public SongListAdapter(Context context, int resource, List<Song> objects) {
        super(context, resource, objects);
        this.context = context;
        this.songList = objects;
        datasource = new Datasource(context);
        datasource.open();


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.song_list_layout, parent, false);

        //Display song name in TextView
        Song song = songList.get(position);
        TextView tvSongName = (TextView) view.findViewById(R.id.text_song_name);
        tvSongName.setText(song.getTitle());
        TextView tvArtist  = (TextView) view.findViewById(R.id.text_song_artist);
        tvArtist.setText(song.getArtist());
        datasource.numberSongsInPlaylist();

        if (datasource.inPlaylist(song) == false){
            checkbox = (CheckBox) view.findViewById(R.id.checkBox_playList);
            checkbox.setChecked(false);
        }
        else if (datasource.inPlaylist(song) == true){
            checkbox = (CheckBox) view.findViewById(R.id.checkBox_playList);
            checkbox.setChecked(true);
        }

        return view;
    }
}

