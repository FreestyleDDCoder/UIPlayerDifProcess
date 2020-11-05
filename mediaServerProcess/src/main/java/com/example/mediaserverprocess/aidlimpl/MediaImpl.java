package com.example.mediaserverprocess.aidlimpl;

import android.os.RemoteException;
import android.view.Surface;

import com.example.mediaserverprocess.core.AssetsFilesManager;
import com.example.mediaserverprocess.IMediaAidlInterface;

/**
 * 实现当前surface传输
 */
public class MediaImpl extends IMediaAidlInterface.Stub {

    @Override
    public void addSurface(Surface surface) throws RemoteException {
        AssetsFilesManager.getInstance().getMediaPlayUtils().setSurface(surface);
    }

    @Override
    public void removieSurface(Surface surface) throws RemoteException {

    }
}
