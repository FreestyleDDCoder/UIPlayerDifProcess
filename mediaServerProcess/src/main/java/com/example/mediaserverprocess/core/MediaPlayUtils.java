package com.example.mediaserverprocess.core;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.example.mediaserverprocess.utils.MediaThreadPoolExecutorUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

/**
 * @author : uidq1846
 * @e-mail : liangzhanncu@163.com
 * @date : 2019-3-8 15:37
 * @desc : 抽出MediaPlayer的控制部分，精简播放界面代码
 * @version: 1.0
 */
public class MediaPlayUtils {
    private static final String TAG = "MediaPlayUtils";
    private static final int REFRESH_SEEKBAR = 0x00;
    public static final int VIDEO_WITHOUT_AUDIO = 0x01;
    public static final int AUDIO_WITHOUT_VIDEO = 0x02;
    public static final int VIDEO_WITH_AUDIO = 0x03;
    public static final int NO_VIDEO_NO_AUDIO = 0x04;
    private static final int NOTIFICATION_ERROR = 0x05;
    private static final int NOTIFICATION_VIDEO_WH_CHANGE = 0x06;
    private static final int NOTIFICATION_VIDEO_PLAY_STATUS_CHANGE = 0x07;
    private static final int VIDEO_NORMAL_STATUS = 0x08;
    //这些状态应该提取出来，避免类过于庞大，后期优化
    public static final String VIDEO_STATUS_PLAY = "com.desaysv_automotive.svmedia.VIDEO_STATUS_PLAY";
    public static final String VIDEO_STATUS_PAUSE = "com.desaysv_automotive.svmedia.VIDEO_STATUS_PAUSE";
    public static final String VIDEO_STATUS_STOP = "com.desaysv_automotive.svmedia.VIDEO_STATUS_STOP";
    public static final String VIDEO_STATUS_SETDATA = "com.desaysv_automotive.svmedia.VIDEO_STATUS_SETDATA";
    public static final String ACTION_VIDEO_NEXT = "com.desaysv_automotive.svmedia.ACTION_VIDEO_NEXT";
    public static final String ACTION_VIDEO_PRE = "com.desaysv_automotive.svmedia.ACTION_VIDEO_PRE";
    public static final String ACTION_VIDEO_PLAY = "com.desaysv_automotive.svmedia.ACTION_VIDEO_PLAY";
    public static final String ACTION_VIDEO_PAUSE = "com.desaysv_automotive.svmedia.ACTION_VIDEO_PAUSE";
    public static final String ACTION_VIDEO_STOP = "com.desaysv_automotive.svmedia.ACTION_VIDEO_STOP";
    public static final String VIDEO_STATUS_PREPARED = "com.desaysv_automotive.svmedia.ACTION_VIDEO_PREPARED";
    public static final String ACTION_VIDEO_PLAY_OR_PAUSE = "com.desaysv_automotive.svmedia.ACTION_VIDEO_PLAY_OR_PAUSE";
    private int mSeekToTime;//用于恢复播放时间
    private String mCurrentVideoPlayStatus = VIDEO_STATUS_STOP;
    private MediaPlayer mMediaPlayer;
    private int mWhat = 0;
    private Future<?> mPlayNextFuture;
    private Future<?> mPlayPreFuture;
    //用于设置界面是否可见的标志位,避免生命周期外调用播放
    private boolean isPlayViewVisible = true;
    private volatile boolean mIsPrepared = false;
    private Surface mSurface;
    //当前播放路径
    private AssetFileDescriptor mCurrentPlayPath;

    private AssetFileDescriptor Path1;
    private AssetFileDescriptor Path2;

    public int getmCurrentPlayPosition() {
        return mCurrentPlayPosition;
    }

    public void setmCurrentPlayPosition(int mCurrentPlayPosition) {
        this.mCurrentPlayPosition = mCurrentPlayPosition;
    }

    private int mCurrentPlayPosition;
    private PlayerCallBack mPlayerCallBack;
    private boolean mIsPausePlay = false;//手动暂停的标志位
    //这是屏幕的宽高
    private int displayWidth = 1920;
    private int displayHeight = 720;

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public boolean isPlayViewVisible() {
        return isPlayViewVisible;
    }

    public void setPlayViewVisible(boolean isPlayViewVisible) {
        this.isPlayViewVisible = isPlayViewVisible;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    public boolean isIsPausePlay() {
        return mIsPausePlay;
    }

    public void setIsPausePlay(boolean mIsPausePlay) {
        this.mIsPausePlay = mIsPausePlay;
    }

    public AssetFileDescriptor getCurrentPlayPath() {
        return mCurrentPlayPath;
    }

    public void setCurrentPlayPath(AssetFileDescriptor currentPlayPath) {
        this.mCurrentPlayPath = currentPlayPath;
    }

    public void setDisPlayWH(int displayWidth, int displayHeight) {
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    public MediaPlayUtils(AssetFileDescriptor currentPlayPath, AssetFileDescriptor currentPlayPath2) {
        Path1 = currentPlayPath;
        Path2 = currentPlayPath2;
        this.mCurrentPlayPath = currentPlayPath;
        initPlayer();
    }

    /**
     * 提供调用播放的方法
     *
     * @param playAction 播放动作
     */
    public void setPlayAction(String playAction) {
        Log.d(TAG, "setPlayAction: playAction == " + playAction);
        switch (playAction) {
            case ACTION_VIDEO_NEXT:
                playNext();
                break;
            case ACTION_VIDEO_PRE:
                playPre();
                break;
            case ACTION_VIDEO_PLAY:
                play();
                break;
            case ACTION_VIDEO_PAUSE:
                pause();
                break;
            case ACTION_VIDEO_STOP:
                stop();
                break;
            case ACTION_VIDEO_PLAY_OR_PAUSE:
                playOrPause();
                break;
        }
    }

    /**
     * 开始播放的线程
     */
    private Runnable mStartPlayRunnable = () -> startPlay();

    /**
     * 开始播放方法
     */
    private synchronized void startPlay() {
        initPlayer();
        Log.i(TAG, "startPlay() mCurrentPlayPath == " + mCurrentPlayPath);
        setOnVideoPlayStatusChange(VIDEO_STATUS_STOP);
        mIsPrepared = false;
        mMediaPlayer.reset();
        Log.i(TAG, "startPlay() mMediaPlayer.reset()");
        try {
            setOnVideoPlayStatusChange(VIDEO_STATUS_SETDATA);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(mCurrentPlayPath);
            }
            mMediaPlayer.prepareAsync();
            Log.i(TAG, "startPlay: mMediaPlayer.prepareAsync()");
        } catch (IOException e) {
            Log.e(TAG, "startPlay: setSource error- mCurrentPlayPath == " + mCurrentPlayPath);
            e.printStackTrace();
            notificationError(0);
        }
    }

    /**
     * 通知出错状态
     *
     * @param status 状态码
     */
    private void notificationError(int status) {
        Message obtain = Message.obtain();
        obtain.what = NOTIFICATION_ERROR;
        obtain.arg1 = status;
        mHandler.sendMessage(obtain);
    }

    /**
     * 获取当前播放状态的方法
     *
     * @return currentVideoPlayStatus
     */
    public String getCurrentVideoPlayStatus() {
        return mCurrentVideoPlayStatus;
    }

    /**
     * 播放上一曲
     */
    private void playPre() {
        //切换曲目时，清空该时间
        mSeekToTime = 0;
        //根据当前位置，找到对应在列表中的位置开播
        if (mPlayPreFuture != null) {
            mPlayPreFuture.cancel(true);
        }
        mPlayPreFuture = MediaThreadPoolExecutorUtils.getInstance().submit(mPlayPreRunnable);
    }

    /**
     * 播放上一曲的操作线程，因为获取上一曲的操作在列表很长时是耗时操作
     */
    private Runnable mPlayPreRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentPlayPath == Path1) {
                mCurrentPlayPath = Path2;
            }
            startPlay();
        }
    };

    /**
     * 播放下一曲
     */
    private void playNext() {
        //切换曲目时，清空该时间
        mSeekToTime = 0;
        //根据当前位置，找到对应在列表中的位置开播
        if (mPlayNextFuture != null) {
            mPlayNextFuture.cancel(true);
        }
        mPlayNextFuture = MediaThreadPoolExecutorUtils.getInstance().submit(mPlayNextRunnable);
    }

    /**
     * 播放下一曲的操作线程，因为获取下一曲的操作在列表很长时是耗时操作
     */
    private Runnable mPlayNextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentPlayPath == Path1) {
                mCurrentPlayPath = Path2;
            }
            startPlay();
        }
    };

    /**
     * 停止播放方法
     */
    private void stop() {
        initPlayer();
        if (mIsPrepared) {
            mIsPrepared = false;
            mMediaPlayer.stop();
            Log.d(TAG, "stop() mMediaPlayer.stop()");
        }
        mHandler.removeMessages(REFRESH_SEEKBAR);
        setOnVideoPlayStatusChange(VIDEO_STATUS_STOP);
    }

    /**
     * 播放的方法
     */
    private void play() {
        initPlayer();
        if (mIsPrepared && !mMediaPlayer.isPlaying() && !playLimited()) {
            mMediaPlayer.start();
            Log.d(TAG, " play: mediaPlayer.start()");
            setOnVideoPlayStatusChange(VIDEO_STATUS_PLAY);
            mHandler.removeMessages(REFRESH_SEEKBAR);
            mHandler.sendEmptyMessage(REFRESH_SEEKBAR);
        } else if (!mIsPrepared) {
            MediaThreadPoolExecutorUtils.getInstance().submit(mStartPlayRunnable);
        }
    }

    /**
     * 根据当前状态设置播放或是暂停属性
     */
    private void playOrPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    /**
     * 一些三方额外限制播放的判断
     * 1.目前包含手动暂停
     * 2.焦点状态
     * 3.行车限制
     * 4.可能需要增加前后台
     *
     * @return 是否限制播放
     */
    private boolean playLimited() {
        return false;
    }

    /**
     * 获取当前播放进度
     *
     * @return 播放进度
     */
    public int getCurrentPosition() {
        if (mMediaPlayer != null && isPrepared()) {//避免调用状态出错
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取当前播放位置的方法
     *
     * @return 当前在列表中的位置
     */
    public int getCurrentPlayPosition() {
        return getPositionByPath();
    }

    /**
     * 暂停的方法
     */
    private void pause() {
        initPlayer();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            Log.d(TAG, "pause() mediaPlayer.pause()");
        }
        mHandler.removeMessages(REFRESH_SEEKBAR);
        setOnVideoPlayStatusChange(VIDEO_STATUS_PAUSE);
    }

    /**
     * 释放资源
     */
    public void release() {
        mIsPrepared = false;
        mSeekToTime = 0;
        Log.d(TAG, "release()");
        if (mMediaPlayer != null) {
            //只有当在start状态和paused状态才能调用stop
            pause();
            setOnVideoPlayStatusChange(VIDEO_STATUS_STOP);
            mMediaPlayer.reset();
            Log.d(TAG, "release: mediaPlayer.reset()");
            mMediaPlayer.release();
            Log.d(TAG, "release: mediaPlayer.release()");
            mMediaPlayer = null;
            Log.d(TAG, "release: mediaPlayer = null");
        }
        mHandler.removeMessages(REFRESH_SEEKBAR);
    }

    /**
     * 连接player与播放界面
     *
     * @param mSurface mSurface
     */
    public void setSurface(Surface mSurface) {
        Log.i(TAG, "setSurface()");
        this.mSurface = mSurface;
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(mSurface);
        } else {
            initPlayer();
        }
    }

    /**
     * 初始化mediaPlayer
     */
    private void initPlayer() {
        if (mMediaPlayer == null) {//由于子线程播放，可能导致初始化了两次
            synchronized (MediaPlayer.class) {
                if (mMediaPlayer == null) {
                    mIsPrepared = false;
                    Log.d(TAG, "initPlayer() player == null");
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                    mMediaPlayer.setOnErrorListener(mOnErrorListener);
                    mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
                    mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
                    mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
                    mMediaPlayer.setOnInfoListener(mOnInfoListener);
                    if (mSurface != null) {
                        mMediaPlayer.setSurface(mSurface);
                        Log.d(TAG, "initPlayer() player.setSurface");
                    }
                }
            }
        }
    }

    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged() width == " + width + "###height == " + height);
            getVideoWH(height, width);
        }
    };

    /**
     * 原生播放完成状态回调
     */
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG, "mOnCompletionListener onCompletion()");
            if (mWhat == -38) {//暂时规避切换上一曲报错问题
                mWhat = 0;
                return;
            }
            setPlayAction(ACTION_VIDEO_STOP);
            //先回调暂停状态给ui
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onVideoPlayCompletion(mp);
            }
        }
    };

    /**
     * 原生播放失败回调
     */
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onError: what == " + what);
            mIsPrepared = false;
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                release();//如果是MEDIA_ERROR_SERVER_DIED，则先释放掉player
            }
            if (what == -38) {
                mWhat = what;
                return false;
            }
            notificationError(what);
            return false;
        }
    };

    /**
     * Interface definition of a callback to be invoked to communicate some
     * info and/or warning about the media or its playback.
     * what    the type of info or warning.
     * extra an extra code, specific to the info. Typically implementation dependent.
     * *@return True if the method handled the info, false if it didn't.Returning false, or not having an OnInfoListener at all, will cause the info to be discarded.
     */
    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onInfo: what == " + what);
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING) {
                notificationError(what);
            }
            return true;
        }
    };

    /**
     * 准备状态完成
     */
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mp) {
            Log.i(TAG, "onPrepared()");
            mIsPrepared = true;
            //增加状态回调给视频，避免播放按钮不可点击
            setOnVideoPlayStatusChange(VIDEO_STATUS_PREPARED);
            if (mSeekToTime != 0) {
                seekTo(mSeekToTime);
                mSeekToTime = 0;
            }
            if (mPlayerCallBack != null) {
                mPlayerCallBack.onDurationChange(mp.getDuration());
                mPlayerCallBack.onVideoCurrentPositionChange(mp.getCurrentPosition());
            }
            play();
            new Timer().schedule(new TimerTask() {//延迟500ms，避免切换的时候先看到上一界面的拉伸画面（观感差）
                @Override
                public void run() {
                    initAudioNormalStatus(mp);
                }
            }, 500);
        }
    };

    /**
     * 判断是否正常音视频方法块
     *
     * @param mp MediaPlayer
     */
    private void initAudioNormalStatus(MediaPlayer mp) {
        Log.d(TAG, "initAudioNormalStatus() mIsPrepared == " + mIsPrepared);
        if (!mIsPrepared) {
            return;
        }
        //用于判断音视频
        MediaPlayer.TrackInfo[] mTrackInfo;
        try {
            mTrackInfo = mp.getTrackInfo();
        } catch (RuntimeException e) {//避免文件报错后闪退
            e.printStackTrace();
            Log.e(TAG, "initAudioNormalStatus() RuntimeException");
            return;
        }
        if (mTrackInfo.length == 1) {//视频文件一定是音频，所以长度大于等于2
            if (mTrackInfo[0].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
                Log.d(TAG, "initAudioNormalStatus() Video without Audio");
                setOnVideoNormalStatus(VIDEO_WITHOUT_AUDIO);
            } else {
                Log.d(TAG, "initAudioNormalStatus() Audio without Video");
                setOnVideoNormalStatus(AUDIO_WITHOUT_VIDEO);
            }
        } else if (mTrackInfo.length > 1) {
            for (MediaPlayer.TrackInfo mt : mTrackInfo) {
                Log.d(TAG, "initAudioNormalStatus() TrackInfo ==" + mt.getTrackType() + "##TrackInfo.length" + mTrackInfo.length);
                if (mt.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) {//这个顺序不一定，只要是视频了，就退出判断
                    setOnVideoNormalStatus(VIDEO_WITH_AUDIO);
                    break;
                } else {
                    setOnVideoNormalStatus(NO_VIDEO_NO_AUDIO);
                }
            }
        }
    }

    /**
     * 提供给外界设置滑动到特定位置的方法
     */
    public void seekTo(int progress) {
        Log.i(TAG, "seekTo: progress == " + progress);
        Log.i(TAG, "seekTo: mIsPrepared == " + mIsPrepared);
        mSeekToTime = progress;
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(progress);
            Log.i(TAG, "seekTo: mMediaPlayer.seekTo");
            play();
        } else if (!mIsPrepared) {
            //startPlay();
        }
    }

    /**
     * 播放到指定位置的设置完成回调
     */
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
        }
    };

    /**
     * 获取当前视频分变率大小
     * 并且根据比例进行转换
     *
     * @param mViewHeight 视频路径
     * @param mViewWidth  视频路径
     */
    private void getVideoWH(int mViewHeight, int mViewWidth) {
        //避免分母为0，报错
        if (mViewHeight == 0) {
            mViewHeight = displayHeight;
        }
        if (mViewWidth == 0) {
            mViewWidth = displayWidth;
        }
        Log.d(TAG, "getVideoWH: vHeight = " + mViewHeight + " vWidth = " + mViewWidth);
        Log.d(TAG, "getVideoWH: displayHeight = " + displayHeight + " displayWidth = " + displayWidth);
        //每次查询需要设置videoView的大小,计算出缩放后的大小
        //小窗口是760X580，需要按比例转换进行缩放
        int mVideoFullHeight;
        int mVideoFullWidth;
        if (mViewHeight >= displayHeight && mViewWidth >= displayWidth) {
            if ((mViewWidth / (float) displayWidth) >= (mViewHeight / (float) displayHeight)) {
                mVideoFullHeight = (int) ((float) displayWidth * mViewHeight / mViewWidth);
                mVideoFullWidth = displayWidth;
            } else {
                mVideoFullHeight = displayHeight;
                mVideoFullWidth = (int) ((float) displayHeight * mViewWidth / mViewHeight);
            }
        } else if (mViewHeight < displayHeight && mViewWidth < displayWidth) {
            //如果宽大于高，则进行拉伸
            if ((mViewWidth / (float) displayWidth) >= (mViewHeight / (float) displayHeight)) {
                mVideoFullHeight = (int) ((float) displayWidth * mViewHeight / mViewWidth);
                mVideoFullWidth = displayWidth;
            } else {
                mVideoFullHeight = displayHeight;
                mVideoFullWidth = (int) ((float) displayHeight * mViewWidth / mViewHeight);
            }
        } else if (mViewHeight >= displayHeight && mViewWidth < displayWidth) {
            mVideoFullHeight = displayHeight;
            mVideoFullWidth = (int) (mViewWidth * ((float) displayHeight / mViewHeight));
        } else if (mViewHeight < displayHeight && mViewWidth >= displayWidth) {
            mVideoFullHeight = (int) (mViewHeight * ((float) displayWidth / mViewWidth));
            mVideoFullWidth = displayWidth;
        } else {
            mVideoFullHeight = displayHeight;
            mVideoFullWidth = displayWidth;
        }
        //这里需要设定拉伸的大小，回调给视频
        notificationVideoWHChange(mVideoFullWidth, mVideoFullHeight);
    }

    /**
     * @param mVideoFullWidth  全屏宽的尺寸
     * @param mVideoFullHeight 全屏高的尺寸
     */
    private void notificationVideoWHChange(int mVideoFullWidth, int mVideoFullHeight) {
        Message obtain = Message.obtain();
        obtain.what = NOTIFICATION_VIDEO_WH_CHANGE;
        obtain.arg1 = mVideoFullWidth;
        obtain.arg2 = mVideoFullHeight;
        mHandler.sendMessage(obtain);
    }

    /**
     * 注册播放回调
     *
     * @param playerCallBack playerCallBack
     */
    public void registerPlayerCallBack(PlayerCallBack playerCallBack) {
        this.mPlayerCallBack = playerCallBack;
    }

    /**
     * 移除回调
     */
    public void unregisterPlayerCallBack() {
        this.mPlayerCallBack = null;
    }

    /**
     * 封装好的相应回调
     */
    public interface PlayerCallBack {
        void onDurationChange(int Duration);

        void onVideoWHChange(int w, int h);

        void onVideoCurrentPositionChange(int currentPosition);

        void onVideoPlayStatusChange(String playStatus);

        void onVideoError(int what);

        void onVideoNormalStatus(int normalStatus);

        void onVideoPlayCompletion(MediaPlayer mp);
    }

    /**
     * 播放状态变化封装（用的地方多，所以封装下）
     *
     * @param playStatus 播放状态
     */
    private void setOnVideoPlayStatusChange(String playStatus) {
        mCurrentVideoPlayStatus = playStatus;
        Message obtain = Message.obtain();
        obtain.obj = playStatus;
        obtain.what = NOTIFICATION_VIDEO_PLAY_STATUS_CHANGE;
        mHandler.sendMessage(obtain);
    }

    /**
     * 回调告诉界面是否是正常的视频
     *
     * @param normalStatus 状态
     */
    private void setOnVideoNormalStatus(int normalStatus) {
        Message obtain = Message.obtain();
        obtain.what = VIDEO_NORMAL_STATUS;
        obtain.arg1 = normalStatus;
        mHandler.sendMessage(obtain);
    }

    /**
     * 用于间隔通知ui刷新进度条
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_SEEKBAR:
                    if (mMediaPlayer != null && isPrepared()) {
                        if (mPlayerCallBack != null) {
                            mPlayerCallBack.onVideoCurrentPositionChange(mMediaPlayer.getCurrentPosition());
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(REFRESH_SEEKBAR, 300);
                    break;
                case NOTIFICATION_ERROR:
                    if (mPlayerCallBack != null) {
                        mPlayerCallBack.onVideoError(msg.arg1);
                    }
                    break;
                case NOTIFICATION_VIDEO_WH_CHANGE:
                    if (mPlayerCallBack != null) {
                        mPlayerCallBack.onVideoWHChange(msg.arg1, msg.arg2);
                    }
                    break;
                case NOTIFICATION_VIDEO_PLAY_STATUS_CHANGE:
                    if (mPlayerCallBack != null) {
                        mPlayerCallBack.onVideoPlayStatusChange((String) msg.obj);
                    }
                    break;
                case VIDEO_NORMAL_STATUS:
                    if (mPlayerCallBack != null) {
                        mPlayerCallBack.onVideoNormalStatus(msg.arg1);
                    }
                    break;
            }
        }
    };

    /**
     * 获取下一播放路径
     *
     * @return 下一播放地址
     */
    public String getNextPath() {
        return "null";//其他情况返回空
    }

    /**
     * 获取上一播放路径
     *
     * @return 上一播放地址
     */
    public String getPrePath() {
        return "null";//其他情况返回空
    }

    /**
     * 获取当前路径地址在列表中的位置
     *
     * @return 列表中的位置
     */
    private int getPositionByPath() {
        return 0;
    }

    /**
     * 查看当前视频是否播放的状态
     *
     * @return 是否播放
     */
    public boolean isPlaying() {
        initPlayer();
        return mMediaPlayer.isPlaying();
    }
}
