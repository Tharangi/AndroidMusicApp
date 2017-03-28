package dinuka.org.musicapp.util;

/**
 * Created by dinuka on 3/27/2017.
 */

public class MusicPlayerUtil
{
    public static String getFormattedTime( int milliseconds )
    {
        String ret = "";
        int seconds = ( milliseconds / 1000 ) % 60;
        int minutes = ( ( milliseconds / ( 1000 * 60 ) ) % 60 );
        ret += minutes < 10 ? "0" + minutes + ":" : minutes + ":";
        ret += seconds < 10 ? "0" + seconds : seconds + "";
        return ret;
    }
}
