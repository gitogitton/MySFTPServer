package myftpserver.myappl.jp.mysftpserver;

import android.widget.TextView;

public class MyNetworkItem {
    TextView mNetworkName;
    TextView mNetworkStatus;

    public TextView getNetworkName() {
        return this.mNetworkName;
    }
    public void setNetworkName( TextView myNetwork ) {
        this.mNetworkName = myNetwork;
    }
    public TextView getNetworkStatus() {
        return this.mNetworkStatus;
    }
    public void setNetworkStatus( TextView status ) {
        this.mNetworkStatus = status;
    }
}
