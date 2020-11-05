package com.example.mediaserverprocess.aidlimpl;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.mediaserverprocess.IBinderPoolAidl;

public class BinderPoolImpl extends IBinderPoolAidl.Stub {
    private final String TAG = "BinderPoolImpl";
    public final static int MEDIA_BINDER_CODE = 0x01;
    public final static int ACTION_BINDER_CODE = 0x02;

    @Override
    public IBinder queryBinder(int binderCode) throws RemoteException {
        Log.d(TAG, "queryBinder: binderCode = " + binderCode);
        IBinder binder = null;
        switch (binderCode) {
            case MEDIA_BINDER_CODE:
                binder = new MediaImpl();
                break;
            case ACTION_BINDER_CODE:
                binder = new MediaActionImpl();
                break;
        }
        return binder;
    }
}
