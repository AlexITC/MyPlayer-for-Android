package com.vha.myplayer;

import java.io.File;

import android.os.Parcel;
import android.os.Parcelable;

public class MySong  implements  Comparable<MySong>, Parcelable {
  private File file;
  private String name;
  private String extension;
  public MySong(File f)  {
    file = f;
    //get extension and name
    int index = file.getName().indexOf('.');
    if  ( index < 0 )  {
      throw new IllegalArgumentException("Invalid file extension");
    }
    name = file.getName().substring(0, index);
    extension = file.getName().substring(1 + index);
    if  ( extension.isEmpty() )  {
      throw new IllegalArgumentException("Invalid file extension");
    }
  }
  public File getFile()  {
    return  file;
  }
  public String getName()  {
    return  name;
  }
  public int compareTo(MySong that)  {
    return  file.compareTo(that.getFile());
  }
  
  public String toString()  {
    return  name;
  }
  
  

    // 99.9% of the time you can just ignore this
  @Override
  public int describeContents() {
    return 0;
  }

    // write your object's data to the passed-in Parcel
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(file);
  }

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private MySong(Parcel in) {
      this( (File) in.readValue(null) );
    }
  
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<MySong> CREATOR = new Parcelable.Creator<MySong>() {
        public MySong createFromParcel(Parcel in) {
            return new MySong(in);
        }

        public MySong[] newArray(int size) {
            return new MySong[size];
        }
    };

  
  
}
