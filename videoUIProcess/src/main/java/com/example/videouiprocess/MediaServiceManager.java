package com.example.videouiprocess;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import com.example.mediaserverprocess.IActionCommandAidl;
import com.example.mediaserverprocess.IBinderPoolAidl;
import com.example.mediaserverprocess.IMediaAidlInterface;

public class MediaServiceManager {
    private final String TAG = "MediaServiceManager";
    @SuppressLint("StaticFieldLeak")
    private static MediaServiceManager mediaServiceManager;
    private Context context;
    private final String MEDIA_SERVICE_PKG = "com.example.mediaserverprocess";
    private final String MEDIA_SERVICE_CLS = ".service.MediaService";
    private IBinderPoolAidl iBinderPoolAidl;
    private final int MEDIA_BINDER_CODE = 0x01;
    private final int ACTION_BINDER_CODE = 0x02;
    private Surface mSurface;

    private MediaServiceManager() {
    }

    public static MediaServiceManager getInstance() {
        if (mediaServiceManager == null) {
            synchronized (MediaServiceManager.class) {
                if (mediaServiceManager == null) {
                    mediaServiceManager = new MediaServiceManager();
                }
            }
        }
        return mediaServiceManager;
    }

    public void init(Context context) {
        Log.d(TAG, "init:");
        this.context = context;
        connectMediaService();
    }

    private void connectMediaService() {
        Log.d(TAG, "connectMediaService:");
        Intent intent = new Intent(MEDIA_SERVICE_PKG + MEDIA_SERVICE_CLS);
        intent.setComponent(new ComponentName(MEDIA_SERVICE_PKG, MEDIA_SERVICE_PKG + MEDIA_SERVICE_CLS));
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected:");
                iBinderPoolAidl = IBinderPoolAidl.Stub.asInterface(service);
                try {
                    iBinderPoolAidl.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: name = " + name);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    /**
     * 绑定客户端死亡
     */
    private final IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied:");
            iBinderPoolAidl.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            iBinderPoolAidl = null;
            connectMediaService();
        }
    };

    /**
     * 获取响应的Binder
     *
     * @param binderCode binderCode
     * @return IBinder
     */
    private IBinder queryBinder(int binderCode) {
        IBinder iBinder = null;
        try {
            if (iBinderPoolAidl != null) {
                iBinder = iBinderPoolAidl.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return iBinder;
    }

    /**
     * 获取媒体类设置接口
     *
     * @return IMediaAidlInterface
     */
    private IMediaAidlInterface getIMediaAidlInterface() {
        return IMediaAidlInterface.Stub.asInterface(queryBinder(MEDIA_BINDER_CODE));
    }

    /**
     * 获取控制类设置接口
     *
     * @return IMediaAidlInterface
     */
    public IActionCommandAidl getIActionCommandAidl() {
        return IActionCommandAidl.Stub.asInterface(queryBinder(ACTION_BINDER_CODE));
    }

    /**
     * setSurface
     *
     * @param mSurface mSurface
     */
    public void setSurface(Surface mSurface) {
        this.mSurface = mSurface;
        try {
            getIMediaAidlInterface().addSurface(mSurface);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * removeSurface
     */
    public void removeSurface() {
        try {
            getIMediaAidlInterface().removieSurface(mSurface);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
