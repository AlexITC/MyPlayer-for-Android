package com.vha.myplayer;
 
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

public class PlaylistManager  implements Parcelable {
  
  public static final String PLAYLIST_DATA = "PLAYLIST";
  public static final String SONG_DATA = "CURRENT_SONG";
  public static final String PLAYLIST_TAG = "PLAYLIST_MANAGER";
  // SDCard Path
  public File EXTERNAL_STORAGE;
  //filters
  private FileFilter fileFilter;
  //lists
  private ArrayList<Integer> shuffleList;
  
  private ArrayList<MySong> playlist = new ArrayList<MySong>();
  

  private boolean shuffle = false;
  private int currentSong = -1;
  
  public static final String [] MUSIC_EXTENSIONS = { "MP3" };
  
  // Constructor
  public PlaylistManager() throws Exception {
    String state = Environment.getExternalStorageState();
    //check if sdcard is readable
    if  ( state.equals(Environment.MEDIA_MOUNTED)  ||  state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) )  {
      EXTERNAL_STORAGE = Environment.getExternalStorageDirectory();
      EXTERNAL_STORAGE = new File( EXTERNAL_STORAGE.getParent() );
      
      fileFilter = new FileFilter( MUSIC_EXTENSIONS, true );
      
      shuffleList = new ArrayList<Integer>();
      
      return;
    }
    throw new Exception( "Cannot read sdcard" );
  }
  
  
  /**
   * Function to read all mp3 files from sdcard
   **/
  public void loadDefaultPlaylist()  {
    loadPlaylist(EXTERNAL_STORAGE);
  }

  /**
   * Function to read all mp3 files from the given directoy
   * and store the details in ArrayList
   **/
  public void loadPlaylist(File directory)  {
    shuffleList.clear();
    playlist.clear();
    
    fillPlayList(directory);
    //generate indexs
    for (int i = 0;  i < playlist.size();  i++)  shuffleList.add(i);
    //
    currentSong = -1;
  }
  /**
   * Function to append all mp3 files from the given directoy
   * to the current playlist
   **/
  public void appendToPlaylist(File f)  {
    if  ( f.isDirectory() )  {
      int oldSize = playlist.size();
      
      fillPlayList(f);
      //generate indexs
      for (int i = oldSize;  i < playlist.size();  i++)  shuffleList.add(i);
      return;
    }
    shuffleList.add( playlist.size() );
    playlist.add( new MySong(f) );
  }


  /**
   * Function to get the current playing file
   **/
  public File getCurrentSongFile() throws Exception  {
    if  ( currentSong < 0 )  {
      throw new Exception("No hay un sonido actualmente");
    }
    if  (shuffle)  {
      return  playlist.get( shuffleList.get(currentSong) ).getFile();
    }
    return  playlist.get( currentSong ).getFile();
  }

  /**
   * Function to get the current playing name (without extension)
   **/
  public String getCurrentSongName() throws Exception  {
    if  ( currentSong < 0 )  {
      throw new Exception("No hay un sonido actualmente");
    }
    if  (shuffle)  {
      return  playlist.get( shuffleList.get(currentSong) ).getName();
    }
    return  playlist.get( currentSong ).getName();
  }

  /**
   * Move to next song in playlist
   **/
  public void nextSong() throws Exception  {
    if  ( playlist.size() == 0 )  {
      throw new Exception("Playlist is empty");
    }
    if  ( ++currentSong == playlist.size() )  {
      currentSong = 0;
    }
  }

  /**
   * Move to previous song in playlist
   **/
  public void previousSong() throws Exception  {
    if  ( playlist.size() == 0 )  {
      throw new Exception("Playlist is empty");
    }
    if  ( --currentSong < 0 )  {
      currentSong = playlist.size() - 1;
    }
  }

  /**
   * Choose specific song in playlist
   **/
  public void chooseSong(int index)  {
    currentSong = index;
    if  (shuffle)  {
      shuffle();
    }
  }
  

  public ArrayList<MySong> getCurrentPlaylist()  {
    return  playlist;
  }
  
  public void switchShuffle()  {
    shuffle = !shuffle;
    if  (shuffle)  {
      shuffle();
    }  else  {
      currentSong = shuffleList.get(currentSong);
    }
  }
  
  private void shuffle()  {
    shuffleList.remove( Integer.valueOf(currentSong) );
    Collections.shuffle(shuffleList);
    shuffleList.add(0, currentSong);
    currentSong = 0;
  }
  
  public boolean isShuffle()  {
    return  shuffle;
  }
  
  public int getCurrentSongNumber()  {
    return  shuffle ? shuffleList.get(currentSong)  :  currentSong;
  }
  
  public int getPlaylistSize()  {
    return  playlist.size();
  }
  
  /**
   * Recursive function to load all MP3's from the given directory
   * and store the details in ArrayList
   **/
  private void fillPlayList(File dir) {
    //
    File [] files = dir.listFiles(fileFilter);
    if  ( files != null )  for (File file : files) {
      if  ( file.isDirectory() )  {
        fillPlayList(file);
      }  else  {
        playlist.add( new MySong(file) );
      }
    }
  }


  
  
  
  
  
  
    // 99.9% of the time you can just ignore this
  @Override
  public int describeContents() {
    return 0;
  }

    // write your object's data to the passed-in Parcel
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(EXTERNAL_STORAGE);

    dest.writeBooleanArray(new boolean [] {shuffle} );
    dest.writeInt(currentSong);
    
    dest.writeList(shuffleList);
    
    dest.writeTypedList(playlist);
  }

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private PlaylistManager(Parcel in) {
      EXTERNAL_STORAGE = (File) in.readValue(null);
    fileFilter = new FileFilter( MUSIC_EXTENSIONS, true );
    
      boolean v [] = new boolean [1];
      in.readBooleanArray(v);
      shuffle = v[0];
      currentSong = in.readInt();
      
    shuffleList = new ArrayList<Integer>();
      in.readList(shuffleList, null);

    playlist = new ArrayList<MySong>();
    in.readTypedList(playlist, MySong.CREATOR);

    }
  
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<PlaylistManager> CREATOR = new Parcelable.Creator<PlaylistManager>() {
        public PlaylistManager createFromParcel(Parcel in) {
            return new PlaylistManager(in);
        }

        public PlaylistManager[] newArray(int size) {
            return new PlaylistManager[size];
        }
    };


  
}
