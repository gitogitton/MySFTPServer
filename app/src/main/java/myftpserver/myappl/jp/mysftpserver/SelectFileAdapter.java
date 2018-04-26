package myftpserver.myappl.jp.mysftpserver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//public class SelectFileAdapter extends ArrayAdapter<String> {
public class SelectFileAdapter extends ArrayAdapter<File> {
    private LayoutInflater mLayoutInflater;
    private int mResourceId;
    private ArrayList<File> arrayList;

    public SelectFileAdapter(@NonNull Context context, int resource, @NonNull ArrayList<File> objects) {
        super(context, resource, objects);
        this.mLayoutInflater = LayoutInflater.from( context );
        this.mResourceId = resource;
        this.arrayList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if ( convertView == null ) {
            convertView = mLayoutInflater.inflate( mResourceId, parent, false );
        }

        File item = (File)getItem( position );
        //Log.d( "getView", "item->"+item );

        String dispStr = item.getName();
        if ( item.isDirectory() ) { //when item is directory, append "/".
            dispStr = "/" + dispStr;
        }
        TextView textView = convertView.findViewById( R.id.text_file );
        textView.setText( dispStr );

        return convertView;
    }
}
