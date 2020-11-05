package com.example.videouiprocess;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "VideoUIProcessMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button pre = findViewById(R.id.pre);
        pre.setOnClickListener(this);
        Button playOrPause = findViewById(R.id.playOrPause);
        playOrPause.setOnClickListener(this);
        Button next = findViewById(R.id.next);
        next.setOnClickListener(this);
        TextureView textureView = findViewById(R.id.tv);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable:");
                @SuppressLint("Recycle")
                Surface mSurface = new Surface(surface);
                MediaServiceManager.getInstance().setSurface(mSurface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureSizeChanged: width = " + width + " height = " + height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTextureDestroyed:");
                MediaServiceManager.getInstance().removeSurface();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pre:
                try {
                    MediaServiceManager.getInstance().getIActionCommandAidl().pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.playOrPause:
                try {
                    MediaServiceManager.getInstance().getIActionCommandAidl().playOrPause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next:
                try {
                    MediaServiceManager.getInstance().getIActionCommandAidl().next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
