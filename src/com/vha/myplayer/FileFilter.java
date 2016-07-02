package com.vha.myplayer;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Class to filter files with given extension and/or directories
 * */
public class FileFilter implements FilenameFilter {
  //
  private String extensions [];
  private boolean acceptFolders;
  //
  public FileFilter(String extensions [], boolean folders)  {
    acceptFolders = folders;
    if  ( extensions == null )  extensions = new String [0];
    this.extensions = extensions.clone();
  }
  
  public boolean accept(File dir, String name) {
    File f = new File(dir, name);
    if  ( f.isDirectory() )  {
      return  acceptFolders;
    }
    //check for extension
    String ext;
    int index = name.lastIndexOf('.');
    if  ( index < 0 )  ext = "";
    else  ext = name.substring(1 + index);
    //
    for (String extension : extensions)  {
      if  ( extension.equalsIgnoreCase(ext) )  {
        return  true;
      }
    }
    return  false;
  }
}