package myftpserver.myappl.jp.mysftpserver;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppendSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppendSettingFragment extends Fragment implements SelectFileDialog.OnSelectListListener {
    private final String CLASS_NAME = getClass().getSimpleName();
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "setting";
    private static final String ARG_PARAM2 = "param2";

    private String mSetting;
    private String mParam2;
    private View mView;

    public interface OnSaveNewData {
        public void onSaveNewData();
    }
    private OnSaveNewData mListener;

    public AppendSettingFragment() {
        // Required empty public constructor
    }

    public static AppendSettingFragment newInstance(String setting, String param2) {
        AppendSettingFragment fragment = new AppendSettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, setting);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSetting = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_append_setting, container, false );
        //Backキー対応
        view.setFocusableInTouchMode( true ); //このViewがタッチモードでフォーカスを受け取る。
        view.requestFocus(); //このViewにフォーカスを移す。
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN ) {
                    Log.d( CLASS_NAME, "push Back Key." );
                    ActionBar actionBar = ( (AppCompatActivity)getActivity()) .getSupportActionBar();
                    actionBar.setTitle( R.string.app_name ); //タイトル変更
                    actionBar.setDisplayHomeAsUpEnabled( false ); //HOMEへ戻る「←」非表示
                }
                return false;
            }
        });
        Button btnBrowse = view.findViewById( R.id.button_browser );
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d( CLASS_NAME, "click browse button." );
                showFileSelectionDialog( "/" );
            }
        });
        mView = view;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu( true ); //オプションメニューを使用する事を宣言

        ActionBar actionBar = ( (AppCompatActivity)getActivity() ).getSupportActionBar();
        actionBar.setTitle( "設定を編集" );
        actionBar.setDisplayHomeAsUpEnabled( true );

        if ( mSetting != null ) { //既存のファイルから読み込んでviewに設定
            MySshSetting mySshSetting = restoreInitDataFromFile( mSetting );

            EditText editUserName = mView.findViewById( R.id.edit_user_name );
            editUserName.setText( mySshSetting.getUserName() );
            EditText editPassword = mView.findViewById( R.id.edit_password );
            editPassword.setText( mySshSetting.getPassword() );
            EditText editPort = mView.findViewById( R.id.edit_port_no );
            editPort.setText( String.valueOf( mySshSetting.getPortNo() ) );
            EditText editPublicKeyFile = mView.findViewById( R.id.edit_public_key );
            editPublicKeyFile.setText( mySshSetting.getPublicKey() );
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d( CLASS_NAME, "onAttach()" );
        super.onAttach(context);
        if ( context instanceof MainActivity ) {
            mListener = (OnSaveNewData) context;
        } else {
            throw new ClassCastException("activity が OnSaveNewData を実装していない。");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate( R.menu.meu_network_setting, menu );
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem( R.id.menu_apl_end ).setVisible( false );
        menu.findItem( R.id.menu_append_setting ).setVisible( false );
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.d( CLASS_NAME, "onOptionsItemSelected()" + item );
        ActionBar actionBar = ( (AppCompatActivity)getActivity()) .getSupportActionBar();
        //ちょっとさぼる・・・
        switch ( item.getItemId() ) {
            case android.R.id.home :
                Log.d( CLASS_NAME, "android.R.id.home " + item );
                actionBar.setTitle( R.string.app_name ); //タイトル変更
                actionBar.setDisplayHomeAsUpEnabled( false ); //HOMEへ戻る「←」非表示
                getFragmentManager().popBackStack();
                return true;
            case R.id.menu_save :
                saveSetting();
                actionBar.setTitle( R.string.app_name ); //タイトル変更
                actionBar.setDisplayHomeAsUpEnabled( false ); //HOMEへ戻る「←」非表示
                mListener.onSaveNewData();
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSelectListListener( File selectedFile ) {
        //Log.d( CLASS_NAME, "onSelectListListener() select->" + selectedFile.getAbsolutePath() );
        if ( selectedFile.isDirectory() ) { //ディレクトリであれば再度中身表示。
            //Log.d( CLASS_NAME, "onSelectListListener() select directory" );
            showFileSelectionDialog( selectedFile.getAbsolutePath() );
        } else { //ファイルの場合はEditViewにセット
            //Log.d( CLASS_NAME, "onSelectListListener() select file" );
            EditText editPublicKey = mView.findViewById( R.id.edit_public_key );
            editPublicKey.setText( selectedFile.getAbsolutePath() );
        }
    }

    //
    //private methods
    //
    private void showFileSelectionDialog( String path ) {
        SelectFileDialog selectFileDiaglog = SelectFileDialog.newInstance( this,"Select File", path );
        selectFileDiaglog.show( getFragmentManager(), "selectFileDialog" );
    }

    private void saveSetting() {
        EditText editUserName = mView.findViewById( R.id.edit_user_name );
        EditText editPassword = mView.findViewById( R.id.edit_password );
        EditText editPortNo = mView.findViewById( R.id.edit_port_no );
        EditText editPublicKey = mView.findViewById( R.id.edit_public_key );

        String createdFile = "sftp_" + editUserName.getText().toString() + ".csv";
        //Context.openFileOutput, Context.openFileInput はandroidで既定のFolderへアクセスする。だからパスはいらない。(パスは Context.getFileDir() で確認出来る。)
        try {
            String BR = System.getProperty( "line.separator" ); //改行コード取得
            StringBuffer writeData = new StringBuffer();
            writeData.append( editUserName.getText().toString() );
            writeData.append( "," );
            writeData.append( editPassword.getText().toString() );
            writeData.append( "," );
            writeData.append( editPortNo.getText().toString() );
            writeData.append( "," );
            writeData.append( editPublicKey.getText().toString() );
            writeData.append( "," );
            writeData.append( BR ); //改行
            FileOutputStream fileOutputStream = getContext().openFileOutput( createdFile, Context.MODE_PRIVATE );
            try {
                fileOutputStream.write( writeData.toString().getBytes() );
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private MySshSetting restoreInitDataFromFile( String settingFileName ) {
        if ( settingFileName == null ) {
            return null;
        }
        MySshSetting sshSetting = new MySshSetting();
        try {
            FileInputStream inputStream = getContext().openFileInput( settingFileName );
            BufferedReader buffer = new BufferedReader( new InputStreamReader( inputStream ) );
            try {
                String readData = buffer.readLine();
                String[] splittData = readData.split( "," );

                for ( int i=0; i<splittData.length; i++ ) {
                    switch ( i ) {
                        case 0 : //username
                            sshSetting.setUserName( splittData[0] );
                            break;
                        case 1 : //password
                            sshSetting.setPassword( splittData[1] );
                            break;
                        case 2 : //port No.
                            sshSetting.setPortNo( Integer.parseInt( splittData[2] ) );
                            break;
                        case 3 : //public key file name
                            sshSetting.setPublicKey( splittData[3] );
                            break;
                        default:
                            break;
                    }
                }

                buffer.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sshSetting;
    }
}
