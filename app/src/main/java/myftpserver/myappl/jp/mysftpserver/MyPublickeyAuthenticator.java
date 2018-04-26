package myftpserver.myappl.jp.mysftpserver;

import android.util.Log;

import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.security.PublicKey;

public class MyPublickeyAuthenticator implements PublickeyAuthenticator {

    @Override
    public boolean authenticate( String username, PublicKey key, ServerSession session ) {
//        Log.d( "MyPublickey", "username -> " + username );
//        Log.d( "MyPublickey", "public key -> " + key );
//        Log.d( "MyPublickey", "session -> " + session );
        if ( username.equals( "test" ) ) {
            Log.d( "MyPublickey", "Username OK" );
            return true;
        }

        return true;
    }
}
