package myftpserver.myappl.jp.mysftpserver;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPublicKey;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppendSettingFragment.OnSaveNewData {

    private final String CLASS_NAME = getClass().getSimpleName();
    private final String FILE_PREFIX = "sftp_";
    private final String FILE_EXTENSION = ".csv";

    private SshServer mSshd = null;
    private ServerSession mSession = null;
    private ListView mMyNetworkList;
    private boolean mIsAlive = false;
    private String mAliveName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showNetworkList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu, menu );
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.menu_apl_end :
                //sftpを終了し、アプリを終了する
                break;
            case R.id.menu_append_setting :
                editSetting( null );
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSelectAction( int menuIndex, String setting ) {
        //Log.d( CLASS_NAME, "onSelectAction() menuIndex / alive : " + menuIndex + " / " + mAliveName );
        switch ( menuIndex ) {
            case 0 : //start / stop
                if ( mIsAlive ) { //SSHDサーバが起動中
                    if ( !mAliveName.equals( setting ) ) { //選択されたSSHサーバー名と違うものが起動中なら拒否。
                        String message = mAliveName + " is Alive Now !\n( Activate only one. )";
                        showAlertDialog( message );
                        return;
                    }
                    try {
                        mSshd.stop( true ); //stop immediately
                        if ( mSession != null ) { mSession.close( true ); } //session close immediately.
                        mIsAlive = false;
                        mAliveName = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else { //SSHDサーバが停止中
                    startSFTP( setting ); //開始する。
                    mIsAlive = true;
                    mAliveName = setting;
                }
                updateStatus( setting, mIsAlive);
                break;

            case 1 : //edit setting
                if ( mIsAlive && mAliveName.equals( setting ) ) {
                    showAlertDialog( "Please after Stop. [edit]" );
                    return;
                }
                String fileName = FILE_PREFIX + setting + FILE_EXTENSION;
                editSetting( fileName );
                break;

            case 2 : //remove setting
                if ( mIsAlive && mAliveName.equals( setting ) ) {
                    showAlertDialog( "Please stop. [remove]" );
                    return;
                }
                String removeName = FILE_PREFIX + setting + FILE_EXTENSION;
                removeSettingFile( removeName );
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveNewData() {
        ArrayList<MyNetworkItem> myNetworkItemArrayList;
        myNetworkItemArrayList = getMyNetworkList();
        MyNetworkListAdapter myNetworkListAdapter = new MyNetworkListAdapter( getApplicationContext(), R.layout.list_my_network, myNetworkItemArrayList );
        mMyNetworkList.setAdapter( myNetworkListAdapter );
    }

    //
    //private methods
    //
    private void editSetting(String setting ) {
        AppendSettingFragment appendSettingFragment = new AppendSettingFragment();
        Bundle args = new Bundle();
        args.putString( "setting", setting );
        args.putString( "param2", "" );
        appendSettingFragment.setArguments( args );

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add( R.id.top_activity, appendSettingFragment );
        fragmentTransaction.addToBackStack( null );
        fragmentTransaction.commit();
    }

    private void showNetworkList() {
        mMyNetworkList = findViewById( R.id.list_my_network );

        ArrayList<MyNetworkItem> myNetworkItemArrayList;
        myNetworkItemArrayList = getMyNetworkList();

        MyNetworkListAdapter myNetworkListAdapter = new MyNetworkListAdapter( getApplicationContext(), R.layout.list_my_network, myNetworkItemArrayList );
        mMyNetworkList.setAdapter( myNetworkListAdapter );

        //ListView listener.
        mMyNetworkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyNetworkItem item = (MyNetworkItem) parent.getItemAtPosition( position ); //選択した設定
                SelectActionDialog selectActionDiaglog = SelectActionDialog.newInstance( "Select Action",
                        item.getNetworkName().getText().toString(), mIsAlive);
                selectActionDiaglog.show( getSupportFragmentManager(), "selectFileDialog" );
            }
        });
    }

    private ArrayList<MyNetworkItem> getMyNetworkList() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.indexOf(FILE_PREFIX) == 0;
            }
        };

        ArrayList<MyNetworkItem> myNetworkItemArrayList = new ArrayList<>();
        File path = getFilesDir();
        File[] dirList = path.listFiles( filter );
        if ( dirList != null ) {
            for ( File file : dirList ) {
                MyNetworkItem myNetworkItem = new MyNetworkItem();
                String name = file.getName();
                name = name.substring( FILE_PREFIX.length(), ( name.length()-FILE_EXTENSION.length() ) );
                TextView textNetName = new TextView( getApplicationContext() );
                textNetName.setText( name );
                myNetworkItem.setNetworkName( textNetName );
                TextView textNetStat = new TextView( getApplicationContext() );
                textNetStat.setText( "" );
                myNetworkItem.setNetworkStatus( textNetStat );

                myNetworkItemArrayList.add( myNetworkItem );
            }
        }

        return myNetworkItemArrayList;
    }

    private MySshSetting restoreSshSettingData( String setting ) {
        String fileName = FILE_PREFIX + setting + FILE_EXTENSION;

        MySshSetting mySshSetting = new MySshSetting();
        try {
            FileInputStream fileInput = openFileInput( fileName );
            BufferedReader reader = new BufferedReader( new InputStreamReader( fileInput ) );

            String buff = reader.readLine();
            String[] data = buff.split( "," );

            mySshSetting.setUserName( data[0] );
            mySshSetting.setPassword( data[1] );
            int port = Integer.parseInt( data[2] );
            mySshSetting.setPortNo( port );
            mySshSetting.setPublicKey( data[3] );

            reader.close();
            fileInput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mySshSetting;
    }

    private void updateStatus( String setting, boolean start ) {
        MyNetworkListAdapter adapter = (MyNetworkListAdapter)mMyNetworkList.getAdapter();
        int lineNum = adapter.getCount();
        for ( int i=0; i<lineNum; i++ ) {
            MyNetworkItem item = adapter.getItem( i );
            if ( item.getNetworkName().getText().toString().equals( setting ) ) {
                String status = start?"active":" ";
                item.getNetworkStatus().setText( status );
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void removeSettingFile( String fileName ) {
        if ( fileName == null ) {
            return;
        }
        String file = getFilesDir() + "/" + fileName;
        File targetFile = new File( file );
        boolean result = targetFile.delete();

        removeItemFromList( fileName );

        String message;
        message = ( result?"有無を言わさず消しました：":"失敗！！：" ) + fileName;
        showAlertDialog( message );
    }

    private void removeItemFromList( String removeFile ) {
        MyNetworkListAdapter adapter = (MyNetworkListAdapter) mMyNetworkList.getAdapter();
        for ( int i=0; i<adapter.getCount(); i++ ) {
            String itemString = adapter.getItem( i ).getNetworkName().getText().toString();
            itemString = FILE_PREFIX + itemString + FILE_EXTENSION; //craete File Name.
            if ( removeFile.equals( itemString ) ) {
                adapter.remove( adapter.getItem( i ) );
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void startSFTP(final String setting ) {

        //============================================================================
        //restore setting data from file
        //============================================================================
        MySshSetting mySshSetting = restoreSshSettingData( setting );
        //============================================================================
        //update status
        //============================================================================
        updateStatus( setting, mIsAlive);
        //============================================================================
        //ssh
        //============================================================================
        mSshd = SshServer.setUpDefaultServer();
        //============================================================================
        //port
        //============================================================================
        mSshd.setPort( mySshSetting.getPortNo() );
        //============================================================================
        //
        //============================================================================
        mSshd.setKeyPairProvider( new SimpleGeneratorHostKeyProvider( getApplicationContext().getFilesDir() + "/hostkey.ser" ) );
//        mSshd.setKeyPairProvider( new SimpleGeneratorHostKeyProvider() );

        //============================================================================
        //認証方式の設定
        //============================================================================
        // パスワード認証方式を設定　(使わないけれど置いておく。)
        //PasswordAuthenticator passwordAuthenticator = sshd.getPasswordAuthenticator();
        mSshd.setPasswordAuthenticator( new PasswordAuthenticator() {
        //パスワード認証のロジックinterface
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                //mSession = session;
                return false;
            }
        });
        // 公開鍵認証方式を設定
        mSshd.setPublickeyAuthenticator( new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                mSession = session;
                String fileName = "sftp_" + username + ".csv";
                File file = new File( getApplicationContext().getFilesDir() + "/" + fileName );
                if ( !file.exists() ) {
                    return false;
                }
                String settingPubKey = null;
                try {
                    FileInputStream inputStream = openFileInput( fileName );
                    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
                    try {
                        String buff = bufferedReader.readLine();
                        String[] item = buff.split( "," );
                        if ( item.length >= 4 ) {
                            settingPubKey = item[3];
                        }
                        inputStream.close();
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if ( settingPubKey == null ) return false; //no setting

                File keyFile = new File( settingPubKey );
                if ( !keyFile.exists() ) return false; //no specified file

                if ( !( key instanceof RSAPublicKey ) ) return false; //only RSA type

                return true;
            }
        });

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>();
//パスワード認証はなし    userAuthFactories.add( new UserAuthPassword.Factory() );
        userAuthFactories.add( new UserAuthPublicKey.Factory() );
        mSshd.setUserAuthFactories( userAuthFactories );

        //============================================================================
        // sshのログインシェルを設定・・・セキュリティーを考慮するとない方がいい？
        //============================================================================
//android で shell 起動って adb しか知らない・・・
        EnumSet<ProcessShellFactory.TtyOptions> options;
        String[] command;
        if ( OsUtils.isWin32() ) {
            options = EnumSet.of( ProcessShellFactory.TtyOptions.ONlCr, ProcessShellFactory.TtyOptions.Echo , ProcessShellFactory.TtyOptions.ICrNl );
            command = new String[]{ "cmd" };
        } else {
            options = EnumSet.of( ProcessShellFactory.TtyOptions.ONlCr );
            command = new String[]{ "/system/bin/sh", "-i", "-l" }; //う～ん、あってる？adbでshell起動して"/system/bin/sh -i -l"は出来てるけれど。（-i:Interactive shell.  -l:Login shell）
//            command = new String[]{ "/sbin/adbd" }; //う～ん、こりゃだめでした。
        }
        //shell設定：シェルファクトリは、ユーザーがログインするたびに新しいシェルを作成するために使用されます.SSHDは、必要に応じて使用できる単一の実装を提供します。
        mSshd.setShellFactory( new ProcessShellFactory( command, options ) );

        //============================================================================
        //SCP有効化
        //============================================================================
        //CommandFactoryは、SSHサーバに直接コマンドを送信する場合に使用されます。これは、ssh localhost shutdownまたはscp xxxを実行している場合に発生します
        mSshd.setCommandFactory( new ScpCommandFactory() ); //scp有効化

        //============================================================================
        //SFTP 有効化
        //============================================================================
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
        namedFactoryList.add( new SftpSubsystem.Factory() );
        mSshd.setSubsystemFactories( namedFactoryList ); //sftp有効化

        //============================================================================
        //SSH開始
        //============================================================================
        try {
            mSshd.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDialog( String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Alert" );
        builder.setMessage( message );
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.d( CLASS_NAME, "AlertDialog : push OK" );
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
