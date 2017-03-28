package dinuka.org.musicapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import dinuka.org.musicapp.util.MusicPlayerUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener
{
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int EAD_EXTERNAL_STORAGE_PERMISSION_CODE = 10000;

    private ImageView mSkipPrev;
    private ImageView mSkipNext;
    private ImageView mPlayPause;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private ListView mPlayList;

    private View playing;
    private PlayListAdapter playListAdapter;
    private MediaPlayer mediaPlayer;

    private Bitmap play;
    private Bitmap pause;

    private boolean userSeeked = false;

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        if( getSupportActionBar() != null )
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled( false );
            getSupportActionBar().setTitle( "Music Player" );
        }

        mPlayPause = ( ImageView ) findViewById( R.id.play_pause );
        mSkipNext = ( ImageView ) findViewById( R.id.next );
        mSkipPrev = ( ImageView ) findViewById( R.id.prev );
        mStart = ( TextView ) findViewById( R.id.startText );
        mEnd = ( TextView ) findViewById( R.id.endText );
        mSeekbar = ( SeekBar ) findViewById( R.id.seekBar1 );
        mPlayList = ( ListView ) findViewById( R.id.play_list );

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
        mediaPlayer.setOnPreparedListener( this );

        play = BitmapFactory.decodeResource( getResources(), R.drawable.ic_play );
        pause = BitmapFactory.decodeResource( getResources(), R.drawable.ic_pause );

        setListeners();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if( hasPermissionGranted() )
        {
            setContent();
        }
    }

    @Override
    protected void onDestroy()
    {
        mediaPlayer.release();
        super.onDestroy();
    }

    private void setListeners()
    {
        mPlayPause.setOnClickListener( this );
        mSkipNext.setOnClickListener( this );
        mSkipPrev.setOnClickListener( this );

        mediaPlayer.setOnCompletionListener( this );

        setSeekbarListener();
    }

    private void setSeekbarListener()
    {
        mSeekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
            {
                if(userSeeked)
                {
                    mediaPlayer.seekTo( progress );
                }
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {
                userSeeked = true;
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
                userSeeked = false;
            }
        } );
    }

    @Override
    public void onClick( View v )
    {
        if( v == mSkipNext )
        {
            findAndPlaySong( true );

        }
        else if( v == mSkipPrev )
        {
            findAndPlaySong( false );
        }
        else if( v == mPlayPause )
        {
            playPauseSong();
        }
    }

    private void findAndPlaySong( boolean next )
    {
        int playingSong = -1;
        ListView listView = ( ListView ) findViewById( R.id.play_list );
        for( int i = 0; i < listView.getChildCount(); i++ )
        {
            View chd = listView.getChildAt( i );
            if( chd == playing )
            {
                playingSong = i;
                break;
            }
        }
        if( next )
        {
            playingSong = ( playingSong < listView.getChildCount() - 1 ) ? playingSong + 1 : playingSong;
        }
        else
        {
            playingSong = ( playingSong == 0 ) ? playingSong : playingSong - 1;
        }

        listView.getChildAt( playingSong ).callOnClick();
    }

    private void playPauseSong()
    {
        if( mediaPlayer.isPlaying() )
        {
            mediaPlayer.pause();
            mPlayPause.setImageBitmap( play );
        }
        else
        {
            if( playing == null )
            {
                ListView listView = ( ListView ) findViewById( R.id.play_list );
                if( listView.getChildCount() > 0 )
                {
                    listView.getChildAt( 0 ).callOnClick();
                }
            }
            else
            {
                mediaPlayer.start();
                mPlayPause.setImageBitmap( pause );
            }
        }
    }

    private boolean hasPermissionGranted()
    {
        if( checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
        {
            requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EAD_EXTERNAL_STORAGE_PERMISSION_CODE );
            return false;
        }
        return true;
    }


    private void setContent()
    {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query( android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null );
        if( cursor == null && !cursor.moveToFirst() )
        {
            Toast.makeText( this, "Cannot load songs.", Toast.LENGTH_LONG );
        }
        else
        {
            playListAdapter = new PlayListAdapter( this, cursor, 0 );
            mPlayList.setAdapter( playListAdapter );
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if( requestCode == EAD_EXTERNAL_STORAGE_PERMISSION_CODE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
        {
            setContent();
        }
    }

    public void playSong( View view, long songId )
    {
        updateUI( view );
        startSong( songId );
    }

    private void updateUI( final View view )
    {
        if( playing == null )
        {
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    view.findViewById( R.id.list_item_playing ).setVisibility( View.VISIBLE );
                    view.findViewById( R.id.list_item_icon ).setVisibility( View.INVISIBLE );
                    playing = view;
                }
            } );
        }
        else
        {
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    mediaPlayer.reset();
                    playing.findViewById( R.id.list_item_playing ).setVisibility( View.INVISIBLE );
                    playing.findViewById( R.id.list_item_icon ).setVisibility( View.VISIBLE );
                    view.findViewById( R.id.list_item_playing ).setVisibility( View.VISIBLE );
                    view.findViewById( R.id.list_item_icon ).setVisibility( View.INVISIBLE );
                    playing = view;
                }
            } );
        }
    }

    private void startSong( long songId )
    {
        try
        {
            Uri contentUri = ContentUris.withAppendedId( android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId );
            mediaPlayer.setDataSource( getApplicationContext(), contentUri );
            mediaPlayer.prepare();
            mediaPlayer.start();
            mHandler.postDelayed( seekBarUpdator,1000);
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    mPlayPause.setImageBitmap( pause );
                }
            } );
        }
        catch( Exception e )
        {
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    mPlayPause.setImageBitmap( play );
                }
            } );
        }
    }

    @Override
    public void onCompletion( MediaPlayer mp )
    {
        mSkipNext.callOnClick();
    }

    private Runnable seekBarUpdator = new Runnable()
    {
        public void run()
        {
            final int mCurrentPosition = mediaPlayer.getCurrentPosition();
            mStart.setText( MusicPlayerUtil.getFormattedTime( mCurrentPosition ) );
            mEnd.setText( MusicPlayerUtil.getFormattedTime( mediaPlayer.getDuration() - mCurrentPosition ) );
            mSeekbar.setProgress(mCurrentPosition);

            mHandler.postDelayed( this, 1000 );
        }
    };

    @Override
    public void onPrepared( final MediaPlayer mp )
    {
        mSeekbar.setMax( mp.getDuration() );
        mStart.setText( "00.00" );
        mEnd.setText( MusicPlayerUtil.getFormattedTime( mp.getDuration() ) );
    }
}

