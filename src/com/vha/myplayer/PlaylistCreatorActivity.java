package com.vha.myplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class PlaylistCreatorActivity extends Activity  implements View.OnClickListener {

  public static final String LOG_TAG = "PlaylistCreatorActivity";
  
    private FileArrayAdapter adapter;
    private ListView listView;
    private Button btnCreate;
    private EditText editName;
    private ArrayList<File> values;
    private ArrayList<File> selectedFiles;
    
    private FileFilter fileFilter;
    
    private File ROOT_DIR;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try  {
          setContentView( R.layout.playlist_creator );
  
          listView = (ListView) findViewById( android.R.id.list );
          editName = (EditText) findViewById( R.id.edit_playlist_name );
          editName.setText( R.string.my_playlist );
          btnCreate = (Button) findViewById( R.id.btn_playlist_create );
          btnCreate.setOnClickListener(this);
          
          fileFilter = new FileFilter(PlaylistManager.MUSIC_EXTENSIONS, true);
          
          selectedFiles = new ArrayList<File>();
  
      String state = Environment.getExternalStorageState();
      //check if sdcard is readable
      if  ( state.equals(Environment.MEDIA_MOUNTED)  ||  state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) )  {
        ROOT_DIR = Environment.getExternalStorageDirectory();
        ROOT_DIR = new File( ROOT_DIR.getAbsolutePath() + File.separatorChar + ".myplayer" + File.separatorChar + "playlists" );
        if  ( !ROOT_DIR.exists() )  {
          ROOT_DIR.mkdirs();
        }
      }
        }  catch (Exception e)  {
          Log.d(LOG_TAG, e.getMessage() );
        }
    }
    
    private void fill()  {
      try  {
        values = new ArrayList<File>();
        
        for (File f : selectedFiles)  {
          if  ( f.isDirectory() )  {
            fill(f);
          }  else  {
            values.add(f);
          }
        }
        
          adapter = new FileArrayAdapter(this, values);
          listView.setAdapter(adapter);
      }  catch (Exception e)  {
          Log.d(LOG_TAG, e.getMessage() );
      }
    }


    private void fill(File dir)  {
    //
      try  {
      File [] files = dir.listFiles(fileFilter);
      if  ( files != null )  for (File file : files) {
        if  ( file.isDirectory() )  {
          fill(file);
        }  else  {
          values.add( file );
        }
      }
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
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
  }

  public static final int REQUEST_PLAYLIST_CREATE = 1000;
  public static final int RESULT_PLAYLIST_CREATED = 1001;
  
  @Override
  public void onClick(View v) {
    //on click button create
    try {
      File f;
      String s = ROOT_DIR.getAbsolutePath() + File.separatorChar + editName.getText().toString() + ".m3u";
      f = new File( s );
      if  ( f.exists() )  {
        int number = 1;
        do  {
          s = ROOT_DIR.getAbsolutePath() + File.separatorChar + editName.getText().toString() +
              " (" + number + ").m3u"
            ;
          f = new File(s);
          number++;
        }  while  ( f.exists() );
      }  else  {
        f.createNewFile();
      }
      
      FileOutputStream fou = new FileOutputStream( f );
      PrintWriter out = new PrintWriter(fou);
      
      for (File file : values)  {
        out.println( file.getAbsolutePath() );
      }
      out.flush();
      out.close();
      fou.close();
      
          setResult( RESULT_PLAYLIST_CREATED, new Intent() );
          finish();
      
    } catch (FileNotFoundException e) {
          Log.d(LOG_TAG, e.getMessage() );
    } catch (IOException e) {
          Log.d(LOG_TAG, e.getMessage() );
    }
    
  }
  
  

  
  private static final int MENU_CHOOSE_FOLDER = Menu.FIRST;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)  {
    menu.add(0, MENU_CHOOSE_FOLDER, 0, "Add Folder");
    return  true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)  {
    switch ( item.getItemId() )  {
    case MENU_CHOOSE_FOLDER:
      startActivityForResult(
        new Intent(getApplicationContext(), FolderChooserActivity.class),
        FolderChooserActivity.REQUEST_FOLDER_CHOOSE
      );  
    break;
    }
    return  true;
  }
  

  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch ( requestCode )  {
        case FolderChooserActivity.REQUEST_FOLDER_CHOOSE:
          if  ( resultCode != FolderChooserActivity.RESULT_FOLDER_CHOOSED )  break;
          File folder = new File( data.getStringExtra( FolderChooserActivity.RESULT_FOLDER ) );
          selectedFiles.add(folder);
          fill();
        break;
        }
    }
}

