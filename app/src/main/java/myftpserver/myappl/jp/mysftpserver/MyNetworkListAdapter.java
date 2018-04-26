package myftpserver.myappl.jp.mysftpserver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyNetworkListAdapter extends ArrayAdapter<MyNetworkItem> {

    LayoutInflater mLayoutInflater;
    int mItemResourceId;

    public MyNetworkListAdapter(@NonNull Context context, int resource, @NonNull List<MyNetworkItem> objects) {
        super(context, resource, objects);
        mItemResourceId = resource;
        mLayoutInflater = LayoutInflater.from( context );
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if ( convertView == null ) {
            convertView = mLayoutInflater.inflate( mItemResourceId, parent, false );
        }

        MyNetworkItem myNetworkItem = getItem( position );
        TextView textView = convertView.findViewById( R.id.text_network_name );
        textView.setText( myNetworkItem.getNetworkName().getText() );

        return convertView;
    }
}
