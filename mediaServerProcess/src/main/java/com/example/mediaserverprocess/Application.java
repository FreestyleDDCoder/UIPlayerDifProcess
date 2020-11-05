package com.example.mediaserverprocess;

import com.example.mediaserverprocess.core.AssetsFilesManager;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AssetsFilesManager.getInstance().init(this.getApplicationContext());
    }
}
