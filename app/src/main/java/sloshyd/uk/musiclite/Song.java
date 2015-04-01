package sloshyd.uk.musiclite;

/**
 * Created by Darren Brooks on 11/03/2015.
 *
 *
 */
public class Song {

    private long id;
    private String title;
    private String artist;
    private long album_Id;

    //Constructors - two that creates Song from inputting all data the other one that creates a blank
    //object (used in the cusorToList() the objects details are then created using the setters
    public Song(long songID, String songTitle, String songArtist, long albumId) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        album_Id = albumId;
    }

    public Song(){

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum_Id(long album_Id) {
        this.album_Id = album_Id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Long getAlbumId(){
        return album_Id;
    }

    //need to override method to get useful information from listener
    @Override
    public String toString(){

        return title;
    }

}
