package com.vha.myplayer;

import java.io.File;
import java.util.Comparator;


public class FileComparator implements Comparator<File>  {

  @Override
  public int compare(File obj, File that) {

    int a = obj.isDirectory()  ?  0  :  1;
    int b = that.isDirectory()  ?  0  :  1;
    if  ( a != b )  return  a - b;
    
    return  obj.getName().toLowerCase().compareTo( that.getName().toLowerCase() );
  }
  
}

