package myftpserver.myappl.jp.mysftpserver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class SelectActionDialog extends DialogFragment {

    private final String CLASS_NAME = getClass().getSimpleName();
    private String mTitle = null;
    private String mSetting = null;
    private boolean mAlive = false;

    public static SelectActionDialog newInstance( String title, String setting, boolean serverAlive ) {
        SelectActionDialog selectFileDiaglog = new SelectActionDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("setting", setting);
        args.putBoolean("alive", serverAlive);
        selectFileDiaglog.setArguments(args);
        return selectFileDiaglog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //Log.d( CLASS_NAME, "onCreate()" );
        super.onCreate(savedInstanceState);
        if ( getArguments() != null ) {
            mTitle = getArguments().getString( "title" );
            mSetting = getArguments().getString( "setting" ); //Activityに戻る時に一緒に返している
            mAlive = getArguments().getBoolean( "alive" );
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

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
//below : customizu title        builder.setTitle( mTitle ); // normal title
        TextView titleText = new TextView( getContext() );
        String s = mSetting + " : " + mTitle;
        titleText.setText( s );
        titleText.setTextColor( Color.BLUE );
        titleText.setTextSize( COMPLEX_UNIT_SP, 20 );
        titleText.setPadding( 10, 10, 10, 10 );
        builder.setCustomTitle( titleText );

        final ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Start / Stop SFTP");
        arrayList.add( "Edit Setting" );
        arrayList.add( "Remove Setting" );

        SelectActionAdapter selectActionAdapter = new SelectActionAdapter( getContext(), R.layout.select_list_item, arrayList );

        builder.setSingleChoiceItems( selectActionAdapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d( CLASS_NAME, "setSingleChoiceItems().onClick() which->" + which + " (size= " + arrayList.size() + " )" );
                //Log.d( CLASS_NAME, "setSingleChoiceItems().onClick() item->" + F.getAbsolutePath() );
                ( (MainActivity)getContext() ).onSelectAction( which, mSetting ); //activityのメソッドを呼ぶ
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
}
