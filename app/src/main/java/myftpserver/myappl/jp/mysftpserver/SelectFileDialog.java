package myftpserver.myappl.jp.mysftpserver;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class SelectFileDialog extends DialogFragment {

    private final String CLASS_NAME = getClass().getSimpleName();
    private String mTitle = null;
    private String mPath = null;

    public interface OnSelectListListener {
        public void onSelectListListener( File selctedFile );
    }
    private OnSelectListListener mListener;

    public static SelectFileDialog newInstance(Fragment fragment, String title, String path ) {
        SelectFileDialog selectFileDiaglog = new SelectFileDialog();
        selectFileDiaglog.setTargetFragment( fragment, 0 ); //listener登録のために呼び出し元Fragmentを記録
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString( "path", path );
        selectFileDiaglog.setArguments(args);
        return selectFileDiaglog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //Log.d( CLASS_NAME, "onCreate()" );
        super.onCreate(savedInstanceState);
        if ( getArguments() != null ) {
            mTitle = getArguments().getString( "title" );
            mPath = getArguments().getString( "path" );
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Log.d( CLASS_NAME, "onViewCreate()" );
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mListener = (OnSelectListListener) getTargetFragment();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
//below : customize title        builder.setTitle( mTitle ); //normal title
        TextView titleText = new TextView( getContext() );
        titleText.setText( mTitle );
        titleText.setTextColor( Color.BLUE );
        titleText.setTextSize( COMPLEX_UNIT_SP, 20 );
        titleText.setPadding( 10, 10, 10, 10 );
        builder.setCustomTitle( titleText );

        final ArrayList<File> arrayList = getFileList( mPath );
        SelectFileAdapter selectFileAdapter = null;
        if ( arrayList != null ) {
            selectFileAdapter = new SelectFileAdapter(Objects.requireNonNull(getContext()),
                    R.layout.select_list_item, arrayList);
        } else {
            ArrayList<File> emptyList = new ArrayList<>();
            emptyList.clear();
            selectFileAdapter = new SelectFileAdapter(Objects.requireNonNull(getContext()),
                    R.layout.select_list_item, emptyList);
        }

        builder.setSingleChoiceItems( selectFileAdapter, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.d( CLASS_NAME, "setSingleChoiceItems().onClick() which->" + which + " (size= " + arrayList.size() + " )" );
                if ( which > arrayList.size() ) {
                    return;
                }
                File file = arrayList.get( which );
                //Log.d( CLASS_NAME, "setSingleChoiceItems().onClick() item->" + file.getAbsolutePath() );
                mListener.onSelectListListener(file );
                dismiss();
            }
        });

        return builder.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //Log.d( CLASS_NAME, "onDismiss()" );
        super.onDismiss(dialog);
    }

    //
    //private methods
    //
    private ArrayList<File> getFileList( String path ) {
        //Log.d( CLASS_NAME, "getFileList() path->" + path );
        ArrayList<File> arrayList = new ArrayList<>();
        File targetPaht = new File( path );
        if ( !targetPaht.canRead() ) {
            Log.d( CLASS_NAME, "can't read." );
            return null;
        }
        File[] dirList = targetPaht.listFiles();
        if ( dirList == null ) {
            return null;
        }
        if ( dirList.length == 0 ) {
            return null;
        }
        boolean first = true;
        for ( File file : dirList ) {
            if ( file.isDirectory() && !file.canRead() ) { //読み込み不可のディレクトリーは表示しない
                continue;
            }
            if ( first && !path.equals( "/" ) ) { //初回でルートでなければ親ディレクトリーへ戻る「../」を表示
            }
            arrayList.add( file );
            first = false;
        }

        return arrayList;
    }
}
