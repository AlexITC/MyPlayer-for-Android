package com.vha.myplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class PlaylistChooserActivity extends Activity
  implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

  public static final String LOG_TAG = "PlaylistChooserActivity";
  
    private File ROOT_DIR;
    private File [] values;
    private FileArrayAdapter adapter;
    private ListView listView;
    private FileFilter fileFilter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try  {
          setContentView( R.layout.playlist_chooser );
  
          listView = (ListView) findViewById( android.R.id.list );
          listView.setOnItemClickListener(this);
          listView.setOnItemLongClickListener(this);
          
      String state = Environment.getExternalStorageState();
      //check if sdcard is readable
      if  ( state.equals(Environment.MEDIA_MOUNTED)  ||  state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) )  {
        ROOT_DIR = Environment.getExternalStorageDirectory();
        ROOT_DIR = new File( ROOT_DIR.getAbsolutePath() + File.separatorChar + 
          ".myplayer"+ File.separatorChar + 
          "playlists"
        );
        if  ( !ROOT_DIR.exists() )  {
          ROOT_DIR.mkdir();
        }
        
        fileFilter = new FileFilter( new String [] {"m3u"}, false );
        
            fill(ROOT_DIR);
      }
        }  catch (Exception e)  {
          Log.d(LOG_TAG, e.getMessage() );
        }
    }
    
    private void fill(File f)  {
      try  {
          values = f.listFiles( fileFilter );
          if  ( values == null )  values = new File [] {};
          Arrays.sort(values);
          adapter = new FileArrayAdapter(this, values);
          listView.setAdapter(adapter);
      }  catch (Exception e)  {
          Log.d(LOG_TAG, e.getMessage() );
      }
    }


  public boolean onKeyDown(int keyCode, KeyEvent event)  {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0  )  {
      exit();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
    
  private void exit()  {
        setResult( RESULT_CANCELED, new Intent() );
        finish();
  }

  

  public static final int REQUEST_PLAYLIST_CHOOSE = 4000;
  public static final int RESULT_PLAYLIST_CHOOSED = 4001;
  
  private PlaylistManager plm;

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    File f = values[position];

    try {
      FileInputStream fis = new FileInputStream( f );
      InputStreamReader isr = new InputStreamReader( fis );
      BufferedReader br = new BufferedReader(isr);
      
      String line;
      plm = new PlaylistManager();
      
      while  ( (line = br.readLine()) != null )  {
        File file = new File(line);
        plm.appendToPlaylist(file);
      }

      br.close();
      isr.close();
      fis.close();
      

      Intent intent = new Intent( getApplicationContext(), PlayListViewerActivity.class );
      
      line = f.getName();
      int index = line.lastIndexOf('.');
      if  ( index > 0 )  line = line.substring(0, index);
      
      intent.putExtra( PlayListViewerActivity.DATA_TITLE, line );
      intent.putParcelableArrayListExtra( PlaylistManager.PLAYLIST_DATA,  plm.getCurrentPlaylist() );
      
      startActivityForResult(
        intent, 
        PlayListViewerActivity.REQUEST_PLAYLIST_CHOOSE_SONG
      );
      
    } catch (FileNotFoundException e) {
          Log.d(LOG_TAG, e.getMessage() );
    } catch (IOException e) {
          Log.d(LOG_TAG, e.getMessage() );
    } catch (Exception e) {
          Log.d(LOG_TAG, e.getMessage() );
    }
  }
  

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
    
    try  {
      final AlertDialog.Builder dialog = new AlertDialog.Builder( PlaylistChooserActivity.this );
      dialog.setTitle("Confirm");
      dialog.setMessage("Do you wanna delete this playlist?");
  
      dialog.setPositiveButton("OK", new OnClickListener() {
        public void onClick(DialogInterface dialog, int which)  {
          values[position].delete();
              fill(ROOT_DIR);
        }
      });
      dialog.setNegativeButton("Cancel", new OnClickListener() {
        public void onClick(final DialogInterface dialog, final int which)  {
          dialog.cancel();
        }
      });
      
      AlertDialog d = dialog.create();
      d.show();
      return true;
    }  catch (Exception e)  {
          Log.d(LOG_TAG, e.getMessage() );
    }
    return false;
  }
  
  
  
  

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch ( requestCode )  {
    //playlist created, update ui
        case PlaylistCreatorActivity.REQUEST_PLAYLIST_CREATE:
          if  ( resultCode == PlaylistCreatorActivity.RESULT_PLAYLIST_CREATED )  {
              fill(ROOT_DIR);
          }
        break;
        //choosed song from playlist
    case PlayListViewerActivity.REQUEST_PLAYLIST_CHOOSE_SONG:
      if  ( resultCode == PlayListViewerActivity.RESULT_PLAYLIST_SONG_CHOOSED )  {
        int index = data.getIntExtra( PlaylistManager.SONG_DATA, 0 );
        
        plm.chooseSong(index);
        
        Intent intent = new Intent();
        intent.putExtra(PlaylistManager.PLAYLIST_TAG, plm);
        
            setResult( RESULT_PLAYLIST_CHOOSED, intent );
            finish();
      }
    break;
    }
  }
  
  
  

  private static final int MENU_PLAYLIST_NEW = Menu.FIRST;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)  {
    menu.add(0, MENU_PLAYLIST_NEW, 0, R.string.playlist_create_label);
    return  true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)  {
    switch ( item.getItemId() )  {
    case MENU_PLAYLIST_NEW:
      startActivityForResult(
        new Intent( getApplicationContext(), PlaylistCreatorActivity.class ), 
        PlaylistCreatorActivity.REQUEST_PLAYLIST_CREATE
      );
    break;
    }
    return  true;
  }

  
  
}

