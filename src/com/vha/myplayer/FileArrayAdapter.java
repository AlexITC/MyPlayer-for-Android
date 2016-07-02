package com.vha.myplayer;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<File>  {

  private Context context;
  private File [] values;
  
  static class ViewHolder  {
    public TextView text;
    public ImageView image;
  }
  

  public FileArrayAdapter(Context context, File [] values) {
    super(context, R.layout.file_row_view, values);
    this.values = values;
    this.context = context;
  }
  public FileArrayAdapter(Context context, ArrayList<File> values) {
    super(context, R.layout.file_row_view, values);
    this.values = values.toArray( new File [] {} );
    this.context = context;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent)  {
    View rowView = convertView;
    ViewHolder holder;
    if  ( rowView == null )  {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      rowView = inflater.inflate(R.layout.file_row_view, null);
      holder = new ViewHolder();
      holder.text = (TextView) rowView.findViewById(R.id.file_row_label);
      holder.image = (ImageView) rowView.findViewById(R.id.file_row_icon);
      rowView.setTag(holder);
    }
    
    holder = (ViewHolder) rowView.getTag();
    
    String name = values[position].getName();
    int index = name.lastIndexOf('.');
    if  ( index > 0 )  name = name.substring(0, index);
    holder.text.setText( name );
    
    if  ( values[position].isDirectory() )  {
      holder.image.setImageResource( R.drawable.directory_icon );
    }  else  {
      holder.image.setImageResource( R.drawable.file_icon );
    }
    return  rowView;
  }
}

