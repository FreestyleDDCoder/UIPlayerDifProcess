package com.example.mediaserverprocess.aidlimpl;

import android.os.RemoteException;

import com.example.mediaserverprocess.core.AssetsFilesManager;
import com.example.mediaserverprocess.IActionCommandAidl;
import com.example.mediaserverprocess.core.MediaPlayUtils;

public class MediaActionImpl extends IActionCommandAidl.Stub {

    @Override
    public void play() throws RemoteException {

    }

    @Override
    public void pause() throws RemoteException {

    }

    @Override
    public void playOrPause() throws RemoteException {
        AssetsFilesManager.getInstance().getMediaPlayUtils().setPlayAction(MediaPlayUtils.ACTION_VIDEO_PLAY_OR_PAUSE);
    }

    @Override
    public void playIndex(int listIndex) throws RemoteException {

    }

    @Override
    public void next() throws RemoteException {
        AssetsFilesManager.getInstance().getMediaPlayUtils().setPlayAction(MediaPlayUtils.ACTION_VIDEO_NEXT);
    }

    @Override
    public void pre() throws RemoteException {
        AssetsFilesManager.getInstance().getMediaPlayUtils().setPlayAction(MediaPlayUtils.ACTION_VIDEO_PRE);
    }

    @Override
    public void stop() throws RemoteException {

    }
}
