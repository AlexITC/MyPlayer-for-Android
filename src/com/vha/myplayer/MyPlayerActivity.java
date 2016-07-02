package com.vha.myplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MyPlayerActivity extends Activity
  implements OnCompletionListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
  //
  
  public static final String LOG_TAG = "MyPlayerActivity";
  
  //Buttons
  private ImageButton btnPlay;
  private ImageButton btnNext;
  private ImageButton btnPrevious;
  private ImageButton btnPlaylist;
  private ImageButton btnRepeat;
  private ImageButton btnShuffle;
  //SeekBar
  private SeekBar songProgressBar;
  //Labels
  private TextView songTitleLabel;
  private TextView songCurrentDurationLabel;
  private TextView songTotalDurationLabel;
  private TextView songTrackCount;
  //
  private ImageView songCover;
  // Media Player
  private MediaPlayer mp;
  // Handler to update UI timer, progress bar etc,.
  private Handler mHandler = new Handler();
  private PlaylistManager playlistManager;
  private Utils utils;
  //

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(LOG_TAG, "onCreate()");
    try  {
        
          requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.my_player_layout);
      
      // All player buttons
      btnPlay = (ImageButton) findViewById(R.id.btnPlay);
      btnNext = (ImageButton) findViewById(R.id.btnNext);
      btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
      btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
      btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
      btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
      
      songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
      songTitleLabel = (TextView) findViewById(R.id.songTitle);
      songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
      songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
      songTrackCount = (TextView) findViewById(R.id.songTrackCount);
      songCover = (ImageView) findViewById( R.id.songCover );
      
      songCover.setImageResource( R.drawable.no_cover );

      // Mediaplayer
      mp = new MediaPlayer();
      
      // Listeners
      songProgressBar.setOnSeekBarChangeListener(this); // Important
      mp.setOnCompletionListener(this); // Important
      
      //
      btnPlay.setOnClickListener(this);
      btnNext.setOnClickListener(this);
      btnPrevious.setOnClickListener(this);
      btnRepeat.setOnClickListener(this);
      btnShuffle.setOnClickListener(this);
      btnPlaylist.setOnClickListener(this);
      

      utils = new Utils();
      
      playlistManager = new PlaylistManager();
      
      loadState();
    }  catch (Exception e)  {
      Toast.makeText(this, R.string.error_sdcard_not_mounted, Toast.LENGTH_SHORT).show();
      Log.d(LOG_TAG, "Exception in onCreate() method: " + e.getMessage() );
    }
  }
  

  private final String SHARED_PREFS_FILE = "MPPrefs";
  private final String KEY_CURRENT_SONG = "CURRENT_SONG";
  private final String KEY_CURRENT_TIME = "CURRENT_TIME";
  private final String KEY_SHUFFLE = "SHUFFLE";
  private final String KEY_REPEAT = "REPEAT";
  
  private SharedPreferences getSettings()  {
    return  this.getApplicationContext().getSharedPreferences(SHARED_PREFS_FILE, 0);
  }
  
  
  protected void onPause()  {
    super.onPause();
    Log.d(LOG_TAG, "onPause()");
    saveState();
  }

  private void saveState()  {

    Log.d(LOG_TAG, "saveState()");
    
    try {
      File f = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + 
        File.separatorChar + ".myplayer" +
        File.separatorChar + "playlist.m3u"
      );
      if  ( f.exists() )  f.delete();
      f.createNewFile();
      
      FileOutputStream fou = new FileOutputStream( f );
      PrintWriter out = new PrintWriter(fou);
      
      for (MySong obj : playlistManager.getCurrentPlaylist())  {
        out.println( obj.getFile().getAbsolutePath() );
      }
      out.flush();
      out.close();
      fou.close();
      
      SharedPreferences.Editor editor = getSettings().edit();
      editor.putInt(KEY_CURRENT_SONG, playlistManager.getCurrentSongNumber());
      editor.putInt(KEY_CURRENT_TIME, mp.getCurrentPosition());
      editor.putBoolean(KEY_REPEAT, mp.isLooping());
      editor.putBoolean(KEY_SHUFFLE, playlistManager.isShuffle());
      editor.commit();
      
    } catch (FileNotFoundException e) {
      Log.d(LOG_TAG, e.getMessage() );
    } catch (IOException e) {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  private void loadState()  {

    Log.d(LOG_TAG, "loadState()");
    
    
    try {
      File f = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + 
        File.separatorChar + ".myplayer" +
        File.separatorChar + "playlist.m3u"
      );
      FileInputStream fis = new FileInputStream( f );
      InputStreamReader isr = new InputStreamReader( fis );
      BufferedReader br = new BufferedReader(isr);
      
      String line;
      playlistManager = new PlaylistManager();
      
      while  ( (line = br.readLine()) != null )  {
        File file = new File(line);
        playlistManager.appendToPlaylist(file);
      }

      br.close();
      isr.close();
      fis.close();
      
      
      SharedPreferences pref = getSettings();
      playlistManager.chooseSong( pref.getInt(KEY_CURRENT_SONG, 0) );
      int position = pref.getInt(KEY_CURRENT_TIME, 0);
      
      

          mp.reset();
          File currentFile = playlistManager.getCurrentSongFile();
      mp.setDataSource( currentFile.getPath() );
      
      mp.prepare();
      mp.seekTo(position);
    //  mp.start();
      // Displaying Song title
      String songTitle = playlistManager.getCurrentSongName();
      
          songTitleLabel.setText(songTitle);
      
      // set Progress bar values
      songProgressBar.setProgress(0);
      songProgressBar.setMax(100);
      
      // Updating progress bar
      updateProgressBar();
      
      if  ( pref.getBoolean(KEY_REPEAT, false) )  {
        mp.setLooping(true);
        btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
      }
      if  ( pref.getBoolean(KEY_SHUFFLE, false) )  {
        playlistManager.switchShuffle();
        btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
      }
      
      
      
    } catch (FileNotFoundException e) {
      Log.d(LOG_TAG, e.getMessage() );
    } catch (IOException e) {
      Log.d(LOG_TAG, e.getMessage() );
    } catch (Exception e) {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  
  /**
   * Play button click event
   * plays a song and changes button to pause image
   * pauses a song and changes button to play image
   * */
  private void play()  {
    try  {
      if  ( playlistManager.getPlaylistSize() == 0 )  {
        // loading all songs list
        playlistManager.loadDefaultPlaylist();
        nextTrack();
        btnPlay.setImageResource(R.drawable.btn_pause);
        return;
      }
      // check for already playing
      if(mp.isPlaying()){
        mp.pause();
        // Changing button image to play button
        btnPlay.setImageResource(R.drawable.btn_play);
      }else{
        // Resume song
        mp.start();
        // Changing button image to pause button
        btnPlay.setImageResource(R.drawable.btn_pause);
      }
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  
  /**
   * Next button click event
   * Plays next song by taking currentSongIndex + 1
   * */
  private void nextTrack()  {
    try {
      playlistManager.nextSong();
      playSong();
    } catch (Exception e) {
      Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  /**
   * Back button click event
   * Plays previous song by currentSongIndex - 1
   * */
  private void previousTrack()  {
    try {
      playlistManager.previousSong();
      playSong();
    } catch (Exception e) {
      Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  /**
   * Button Click event for Repeat button
   * Enables repeat flag to true
   * */
  private void switchRepeat()  {
    try  {
      if  ( playlistManager.getPlaylistSize() == 0 )  {
        Toast.makeText(this, R.string.error_choose_a_playlist, Toast.LENGTH_SHORT).show();
        return;
      }
      mp.setLooping( !mp.isLooping() );
      if  ( mp.isLooping() )  {
        Toast.makeText(this, R.string.repeat_on, Toast.LENGTH_SHORT).show();
        btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
      }  else  {
        Toast.makeText(this, R.string.repeat_off, Toast.LENGTH_SHORT).show();
        btnRepeat.setImageResource(R.drawable.btn_repeat);
      }
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  /**
   * Button Click event for Shuffle button
   * Enables shuffle flag to true
   * */
  private void switchShuffle()  {
    try  {
      if  ( playlistManager.getPlaylistSize() == 0 )  {
        Toast.makeText(this, "First choose a playlist", Toast.LENGTH_SHORT).show();
        return;
      }
      playlistManager.switchShuffle();
      if  ( playlistManager.isShuffle() )  {
        Toast.makeText(getApplicationContext(), R.string.shuffle_on, Toast.LENGTH_SHORT).show();
        btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
      }  else  {
        Toast.makeText(getApplicationContext(), R.string.shuffle_off, Toast.LENGTH_SHORT).show();
        btnShuffle.setImageResource(R.drawable.btn_shuffle);
      }
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  /**
   * Button Click event for Play list click event
   * Launches list activity which displays list of songs
   * */
  private void showPlaylist()  {
    try  {
      Intent intent = new Intent( getApplicationContext(), PlayListViewerActivity.class );
      intent.putParcelableArrayListExtra( PlaylistManager.PLAYLIST_DATA,  playlistManager.getCurrentPlaylist() );
      intent.putExtra( PlaylistManager.SONG_DATA, playlistManager.getCurrentSongNumber() );
      intent.putExtra( PlayListViewerActivity.DATA_TITLE, this.getString( R.string.now_playing) );
      
      startActivityForResult(
        intent, 
        PlayListViewerActivity.REQUEST_PLAYLIST_CHOOSE_SONG
      );
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  @Override
  public void onClick(View v) {
    switch ( v.getId() )  {
    //
    case R.id.btnPlaylist:
      showPlaylist();
    break;
    //
    case R.id.btnShuffle:
      switchShuffle();
    break;
    //
    case R.id.btnRepeat:
      switchRepeat();
    break;
    //
    case R.id.btnPrevious:
      previousTrack();
    break;
    //
    case R.id.btnNext:
      nextTrack();
    break;
    //
    case R.id.btnPlay:
      play();
    break;
    }
  }
  
  /**
   * Function to play a song
   * @param songIndex - index of song
   * */
  public void  playSong(){
    // Play song
    try {
          mp.reset();
          File currentFile = playlistManager.getCurrentSongFile();
      mp.setDataSource( currentFile.getPath() );
      
      mp.prepare();
      mp.start();
      // Displaying Song title
      String songTitle = playlistManager.getCurrentSongName();
      
          songTitleLabel.setText(songTitle);
      
          // Changing Button Image to pause image
      btnPlay.setImageResource(R.drawable.btn_pause);
      
      // set Progress bar values
      songProgressBar.setProgress(0);
      songProgressBar.setMax(100);
      
      // Updating progress bar
      updateProgressBar();
      
    } catch (IllegalArgumentException e) {
      Log.d(LOG_TAG, e.getMessage() );
    } catch (IllegalStateException e) {
      Log.d(LOG_TAG, e.getMessage() );
    } catch (IOException e) {
      Log.d(LOG_TAG, e.getMessage() );
    } catch (Exception e) {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }

  /**
   * Update timer on seekbar
   * */
  public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);        
    }  
  
  /**
   * Background Runnable thread
   * */
  private Runnable mUpdateTimeTask = new Runnable() {
    public void run() {
      try  {
        long totalDuration = mp.getDuration();
        long currentDuration = mp.getCurrentPosition();
        
        // Displaying Total Duration time
        songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
        // Displaying time completed playing
        songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
        
        //updating track count
        songTrackCount.setText( (playlistManager.getCurrentSongNumber() + 1) + "/" + playlistManager.getPlaylistSize() );
        
        // Updating progress bar
        int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));

        songProgressBar.setProgress(progress);
        
        // Running this thread after 100 milliseconds
        mHandler.postDelayed(this, 100);
      }  catch (Exception e)  {
      //  Log.d(LOG_TAG, e.getMessage() );
      }
    }
  };
  public boolean onKeyDown(int keyCode, KeyEvent event)  {
    if ( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0  &&  mp.isPlaying() )  {
      moveTaskToBack(true);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
    
  /**
   * 
   * */
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    
  }

  /**
   * When user starts moving the progress handler
   * */
  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // remove message Handler from updating progress bar
    mHandler.removeCallbacks(mUpdateTimeTask);
    }
  
  /**
   * When user stops moving the progress hanlder
   * */
  @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    try  {
      mHandler.removeCallbacks(mUpdateTimeTask);
      int totalDuration = mp.getDuration();
      int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
      
      // forward or backward to certain seconds
      mp.seekTo(currentPosition);
      
      // update timer progress again
      updateProgressBar();
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
    }

  /**
   * On Song Playing completed
   * if repeat is ON play same song again
   * if shuffle is ON play random song
   * */
  @Override
  public void onCompletion(MediaPlayer arg0) {
    nextTrack();
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
    Log.d(LOG_TAG, "onDestroy()" );
    mp.release();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch ( requestCode )  {
    case PlayListViewerActivity.REQUEST_PLAYLIST_CHOOSE_SONG:
      if  ( resultCode == PlayListViewerActivity.RESULT_PLAYLIST_SONG_CHOOSED )  {
        int index = data.getIntExtra( PlaylistManager.SONG_DATA, 0 );
        playlistManager.chooseSong(index);
        playSong();
      }
    break;
    case PlaylistChooserActivity.REQUEST_PLAYLIST_CHOOSE:
      if  ( resultCode == PlaylistChooserActivity.RESULT_PLAYLIST_CHOOSED )  {
        boolean shuffle = playlistManager.isShuffle();
        playlistManager = (PlaylistManager) data.getParcelableExtra(PlaylistManager.PLAYLIST_TAG);
        playSong();
        if  (shuffle)  playlistManager.switchShuffle();
      }
    break;
    }
  }
  
  private static final int MENU_CHOOSE_PLAYLIST = Menu.FIRST;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)  {
    menu.add( 0, MENU_CHOOSE_PLAYLIST, 0, R.string.playlist_chooser_title );
    return  true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)  {
    switch ( item.getItemId() )  {
    case MENU_CHOOSE_PLAYLIST:
      startActivityForResult(
        new Intent(getApplicationContext(), PlaylistChooserActivity.class),
        PlaylistChooserActivity.REQUEST_PLAYLIST_CHOOSE
      );
    break;
    }
    return  true;
  }

  
}