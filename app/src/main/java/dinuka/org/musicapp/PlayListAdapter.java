package dinuka.org.musicapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dinuka on 3/21/2017.
 */

public class PlayListAdapter extends CursorAdapter
{
    private int imagesSize;
    private Bitmap defaultIcon;
    private Bitmap playingIcon;
    private MainActivity activity;

    public PlayListAdapter( MainActivity activity, Cursor cursor, int flags )
    {
        super( activity, cursor, flags );
        this.imagesSize = ( int ) activity.getResources().getDimension( R.dimen.songImageSize );
        this.activity = activity;
        this.defaultIcon = BitmapFactory.decodeResource( activity.getResources(), R.drawable.ic_default_art );
        this.playingIcon = BitmapFactory.decodeResource( activity.getResources(), R.drawable.play_orange );
    }

    @Override
    public View newView( Context context, Cursor cursor, ViewGroup parent )
    {
        View view = LayoutInflater.from( context ).inflate( R.layout.play_list_item, parent, false );
        ViewHolder viewHolder = new ViewHolder( view );
        view.setTag( viewHolder );

        return view;
    }

    @Override
    public void bindView( View view, Context context, Cursor cursor )
    {
        ViewHolder viewHolder = ( ViewHolder ) view.getTag();
        int titleColumn = cursor.getColumnIndex( MediaStore.Audio.Media.TITLE );
        int artistColumn = cursor.getColumnIndex( MediaStore.Audio.Media.ARTIST );
        int uriColumn = cursor.getColumnIndex( MediaStore.Audio.Media.DATA );
        int trackColumn = cursor.getColumnIndex( MediaStore.Audio.Media.TRACK );
        int albumColumn = cursor.getColumnIndex( MediaStore.Audio.Media.ALBUM );
        int idColumn = cursor.getColumnIndex( MediaStore.Audio.Media._ID );

        viewHolder.titleView.setText( cursor.getString( titleColumn ) );
        viewHolder.artistView.setText( cursor.getString( artistColumn ) );
        viewHolder.descriptionView.setText( cursor.getString( albumColumn ) );

        viewHolder.songId = cursor.getLong( idColumn );
        viewHolder.uri = cursor.getString( uriColumn );
        viewHolder.iconView.setImageBitmap( getMusicFileImage( viewHolder.uri ) );
        viewHolder.playingIconView.setImageBitmap( getScaledImage( playingIcon ) );
        String trackNumber = cursor.getString( trackColumn );


    }

    public class ViewHolder implements View.OnClickListener
    {
        public final ImageView iconView;
        public final ImageView playingIconView;
        public final TextView titleView;
        public final TextView artistView;
        public final TextView descriptionView;

        private String uri = null;
        private long songId = -1;

        public ViewHolder( View view )
        {
            view.setOnClickListener( this );
            iconView = ( ImageView ) view.findViewById( R.id.list_item_icon );
            playingIconView = ( ImageView ) view.findViewById( R.id.list_item_playing );
            titleView = ( TextView ) view.findViewById( R.id.list_item_title );
            artistView = ( TextView ) view.findViewById( R.id.list_item_artist );
            descriptionView = ( TextView ) view.findViewById( R.id.list_item_description );
        }

        @Override
        public void onClick( View v )
        {
            activity.playSong( v, songId );
        }
    }

    private Bitmap getMusicFileImage( String uri )
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try
        {
            mmr.setDataSource( uri );
        }
        catch( Exception e )
        {
            return null;
        }
        byte[] imageBytes = mmr.getEmbeddedPicture();
        Bitmap image = null;
        if( imageBytes != null )
        {
            image = BitmapFactory.decodeByteArray( imageBytes, 0, imageBytes.length );
        }
        else
        {
            image = defaultIcon;
        }
        image = getScaledImage( image );
        mmr.release();

        return image;
    }

    private Bitmap getScaledImage( Bitmap image )
    {
        return Bitmap.createScaledBitmap( image, imagesSize, imagesSize, true );
    }

}
