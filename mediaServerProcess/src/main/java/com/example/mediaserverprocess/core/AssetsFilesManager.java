package com.example.mediaserverprocess.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import java.io.IOException;

public class AssetsFilesManager {
    private final String TAG = "AssetsFilesManager";
    @SuppressLint("StaticFieldLeak")
    private static AssetsFilesManager assetsFilesManager;
    private Context context;

    public String[] getAssetsList() {
        return assetsList;
    }

    public MediaPlayUtils getMediaPlayUtils() {
        return mediaPlayUtils;
    }

    private String[] assetsList;
    private MediaPlayUtils mediaPlayUtils;

    private AssetsFilesManager() {
    }

    public static AssetsFilesManager getInstance() {
        if (assetsFilesManager == null) {
            synchronized (AssetsFilesManager.class) {
                if (assetsFilesManager == null) {
                    assetsFilesManager = new AssetsFilesManager();
                }
            }
        }
        return assetsFilesManager;
    }

    public void init(Context context) {
        Log.d(TAG, "init:");
        this.context = context;
        try {
            assetsList = context.getAssets().list("movie");
            Log.d(TAG, "assetsList : " + assetsList[assetsList.length - 2] + "  " + assetsList[assetsList.length - 1]);
            AssetFileDescriptor assetFileDescriptor1 = context.getAssets().openFd("movie/" + assetsList[0]);
            AssetFileDescriptor assetFileDescriptor2 = context.getAssets().openFd("movie/" + assetsList[1]);
            mediaPlayUtils = new MediaPlayUtils(assetFileDescriptor1, assetFileDescriptor2);
            //assetFileDescriptor1.close();
            //assetFileDescriptor2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
