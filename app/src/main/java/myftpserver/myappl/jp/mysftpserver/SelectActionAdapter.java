package myftpserver.myappl.jp.mysftpserver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class SelectActionAdapter extends ArrayAdapter<String> {
    private LayoutInflater mLayoutInflater;
    private int mResourceId;
    private ArrayList<String> arrayList;

    public SelectActionAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
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

        String item = getItem( position );
        //Log.d( "getView", "item->"+item );

        TextView textView = convertView.findViewById( R.id.text_file );
        textView.setText( item );

        return convertView;
    }
}
