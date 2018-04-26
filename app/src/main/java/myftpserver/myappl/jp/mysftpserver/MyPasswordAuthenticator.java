package myftpserver.myappl.jp.mysftpserver;

import android.util.Log;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class MyPasswordAuthenticator implements PasswordAuthenticator {
    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        Log.d( "MyPasswordAuthenticator", "username / password -> " + username + " / " + password );
        if ( username.equals( "test" ) && password.equals( "test" ) ) {
            return true;
        }
        return false;
    }
}
