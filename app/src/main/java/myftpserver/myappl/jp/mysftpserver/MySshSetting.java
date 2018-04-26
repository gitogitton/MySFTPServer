package myftpserver.myappl.jp.mysftpserver;

public class MySshSetting {

    private String mUserName;
    private String mPassword;
    private int mPortNo;
    private String mPublicKey;

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword( String password ) {
        this.mPassword = password;
    }

    public int getPortNo() {
        return mPortNo;
    }

    public void setPortNo(int portNo) {
        this.mPortNo = portNo;
    }

    public String getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(String publicKey) {
        mPublicKey = publicKey;
    }
}
