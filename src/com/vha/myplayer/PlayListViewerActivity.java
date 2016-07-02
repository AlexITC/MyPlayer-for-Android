package com.vha.myplayer;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlayListViewerActivity extends ListActivity  implements AdapterView.OnItemClickListener {
  
  public static final String LOG_TAG = "PlaylistCreatorActivity";
  
  // Songs list
  private ArrayList<MySong> playlist;

  public static final int REQUEST_PLAYLIST_CHOOSE_SONG = 3000;
  public static final int RESULT_PLAYLIST_SONG_CHOOSED = 3001;
  
  public static final String DATA_TITLE = "TITLE";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.playlist);
    
    try  {
      Intent data = this.getIntent();
      playlist = data.getParcelableArrayListExtra( PlaylistManager.PLAYLIST_DATA );
      int index = data.getIntExtra(PlaylistManager.SONG_DATA, 0);
      if  ( index < 0 )  index = 0;
      
      String title = data.getStringExtra(DATA_TITLE);
      if  ( title != null )  this.setTitle(title);
      
          ArrayAdapter<MySong> adapter = new ArrayAdapter<MySong>( this, android.R.layout.simple_list_item_1, playlist );
          setListAdapter(adapter);
  
      // selecting single ListView item
      ListView lv = getListView();
      lv.setSelection( index );
      // listening to single listitem click
      lv.setOnItemClickListener(this);
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }
  
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    try  {
      // getting listitem index
      int songIndex = position;
      
      // Starting new intent
      Intent intent = new Intent();
      // Sending songIndex to PlayerActivity
      intent.putExtra( PlaylistManager.SONG_DATA, songIndex);
      setResult(RESULT_PLAYLIST_SONG_CHOOSED, intent);
      // Closing PlayListView
      finish();
    }  catch (Exception e)  {
      Log.d(LOG_TAG, e.getMessage() );
    }
  }

}
