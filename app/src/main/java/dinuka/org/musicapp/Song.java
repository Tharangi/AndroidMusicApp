package dinuka.org.musicapp;

/**
 * Created by dinuka on 3/21/2017.
 */

public class Song
{
    private String uri;
    private String title;
    private String artist;
    private String album;
    private int trackNumber = 0;
    private float duration = 0.0f;

    public Song( String uri, String title, String artist, String album, String trackNumber, String duration )
    {
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.album = album;
        try
        {
            this.trackNumber = Integer.parseInt( trackNumber );
            this.duration = ( Long.parseLong( duration )/ 6000 );
        }
        catch( Exception e )
        {
            this.trackNumber = 0;
        }
    }

    public String getUri()
    {
        return uri;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getAlbum()
    {
        return album;
    }

    public int getTrackNumber()
    {
        return trackNumber;
    }

    public float getDuration()
    {
        return duration;
    }
}
