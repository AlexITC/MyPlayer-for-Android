package com.vha.myplayer;


import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


public class FolderChooserActivity extends ListActivity {

    private File currentDir;
    private File ROOT_DIR;
    private Comparator<File> fileComparator;
    private File [] values;
    private FileArrayAdapter adapter;
    private FileFilter fileFilter;

    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

    String state = Environment.getExternalStorageState();
    //check if sdcard is readable
    if  ( state.equals(Environment.MEDIA_MOUNTED)  ||  state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) )  {
      currentDir = ROOT_DIR = Environment.getExternalStorageDirectory();
      
      currentDir = new File( currentDir.getParent() );
          ROOT_DIR = currentDir;
      
          fileComparator = new FileComparator();
          
          fileFilter = new FileFilter(null, true);
          
          fill(currentDir);
    }
    }
    
    
    private void fill(File f)  {
        this.setTitle( f.getName() );
        
        values = f.listFiles( fileFilter );
        if  ( values == null )  values = new File [] {};
        Arrays.sort(values, fileComparator);
        adapter = new FileArrayAdapter(this, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        File f = adapter.getItem(position);
      currentDir = new File( f.getPath() );
      fill(currentDir);
    }
    

  public boolean onKeyDown(int keyCode, KeyEvent event)  {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0  )  {
      if  ( currentDir.equals(ROOT_DIR) )  {
        exit();
        return  true;
      }
      currentDir = currentDir.getParentFile();
      if  ( currentDir != null )  {
        fill(currentDir);
        return true;
      }
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
    
  
  

  public static final int REQUEST_FOLDER_CHOOSE = 2000;
  public static final int RESULT_FOLDER_CHOOSED = 2001;
  
  public static final String RESULT_FOLDER = "PATH";
  
  private static final int MENU_CHOOSE_FOLDER = Menu.FIRST;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)  {
    menu.add(0, MENU_CHOOSE_FOLDER, 0, R.string.choose_this_folder);
    return  true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)  {
    switch ( item.getItemId() )  {
    case MENU_CHOOSE_FOLDER:
          Intent intent = new Intent();
          intent.putExtra( RESULT_FOLDER, currentDir.getPath());
          setResult(RESULT_FOLDER_CHOOSED, intent);
          finish();
    break;
    }
    return  true;
  }
    
    
}

