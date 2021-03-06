package com.jaronho.sdk.utils.view;

import android.app.Activity;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Author:  jaron.ho
 * Date:    2017-02-13
 * Brief:   视频播放器
 */

public class VideoPlayer implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener,
        OnInfoListener, OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {
    public static abstract class SurfaceCreatedHandler {
        public abstract void onCallback(VideoPlayer vp);
    }
    public static abstract class SurfaceDestroyedHandler {
        public abstract void onCallback(VideoPlayer vp);
    }
    public static abstract class PreparedHandler {
        public abstract boolean onCallback(VideoPlayer vp); // 返回true:自动播放,false:不自动播放
    }
    public static abstract class SeekCompleteHandler {
        public abstract void onCallback(VideoPlayer vp);
    }
    public static abstract class CompleteHandler {
        public abstract void onCallback(VideoPlayer vp);
    }
    public static abstract class ErrorHandler {
        public abstract void onCallback(VideoPlayer vp, int what, int extra);
    }
    public enum FitType {
        FIXED_SIZE,     // 固定宽高
        VIDEO_SIZE,     // 视频宽高
        SHOW_ALL,       // 全屏显示,全部展示不裁剪,宽高中大的铺满屏幕,小的留有黑边
        SIDE_FIT,       // 全屏显示,全屏展示不留黑边,宽高中小的铺满屏幕,大的超出屏幕
        FULL_FIT        // 全屏显示,拉伸变形,使铺满屏幕
    }

    private Activity mActivity = null;
    private SurfaceView mSurfaceView = null;
    private MediaPlayer mPlayer = null;
    private int mCurrentPosition = 0;
    private SurfaceCreatedHandler mSurfaceCreatedHandler = null;
    private SurfaceDestroyedHandler mSurfaceDestroyedHandler = null;
    private PreparedHandler mPreparedHandler = null;
    private SeekCompleteHandler mSeekCompleteHandler = null;
    private CompleteHandler mCompleteHandler = null;
    private ErrorHandler mErrorHandler = null;
    private FitType mFitType = FitType.VIDEO_SIZE;
    private String mLogTag = "";
    private boolean mSeekFlag = false;
    private boolean mPlayFlag = false;

    public VideoPlayer(Activity activity, SurfaceView surfaceView, boolean screenOn) {
        mActivity = activity;
        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(this);
        mPlayer = new MediaPlayer();
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnInfoListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnVideoSizeChangedListener(this);
        mPlayer.setScreenOnWhilePlaying(screenOn);
    }

    private void showLog(String msg) {
        if (mLogTag.length() > 0) {
           Log.d(mLogTag, msg);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        showLog("onBufferingUpdate, percent = " + percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        showLog("onCompletion");
        if (null != mCompleteHandler) {
            mCompleteHandler.onCallback(this);
        }
    }

    private String errorString(int code) {
        switch (code) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                return "MEDIA_ERROR_UNKNOWN";
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return "MEDIA_ERROR_SERVER_DIED";
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                return "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
            case MediaPlayer.MEDIA_ERROR_IO:
                return "MEDIA_ERROR_IO";
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                return "MEDIA_ERROR_MALFORMED";
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                return "MEDIA_ERROR_UNSUPPORTED";
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return "MEDIA_ERROR_TIMED_OUT";
            case -2147483648:
                return "MEDIA_ERROR_SYSTEM";
        }
        return String.valueOf(code);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        showLog("onError, what = " + errorString(what) + ", extra = " + errorString(extra));
        if (null != mErrorHandler) {
            mErrorHandler.onCallback(this, what, extra);
        }
        return false;
    }

    private String infoString(int code) {
        switch (code) {
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                return "MEDIA_INFO_UNKNOWN";
            case 2:
                return "MEDIA_INFO_STARTED_AS_NEXT";
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                return "MEDIA_INFO_VIDEO_RENDERING_START";
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                return "MEDIA_INFO_VIDEO_TRACK_LAGGING";
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                return "MEDIA_INFO_BUFFERING_START";
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                return "MEDIA_INFO_BUFFERING_END";
            case 703:
                return "MEDIA_INFO_NETWORK_BANDWIDTH";
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                return "MEDIA_INFO_BAD_INTERLEAVING";
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                return "MEDIA_INFO_NOT_SEEKABLE";
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                return "MEDIA_INFO_METADATA_UPDATE";
            case 803:
                return "MEDIA_INFO_EXTERNAL_METADATA_UPDATE";
            case 900:
                return "MEDIA_INFO_TIMED_TEXT_ERROR";
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                return "MEDIA_INFO_UNSUPPORTED_SUBTITLE";
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                return "MEDIA_INFO_SUBTITLE_TIMED_OUT";
        }
        return String.valueOf(code);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        showLog("onInfo, what = " + infoString(what) + ", extra = " + infoString(extra));
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        showLog("onPrepared");
        if (FitType.FIXED_SIZE != mFitType) {
            // 获取video的宽和高
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            if (FitType.VIDEO_SIZE != mFitType) {
                Point displaySize = new Point();
                mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                float wRatio = (float) videoWidth / (float) displaySize.x;
                float hRatio = (float) videoHeight / (float) displaySize.y;
                // 进行缩放
                if (FitType.SHOW_ALL == mFitType) {
                    float ratioMax = Math.max(wRatio, hRatio);
                    videoWidth = (int) Math.ceil((float) videoWidth / ratioMax);
                    videoHeight = (int) Math.ceil((float) videoHeight / ratioMax);
                } else if (FitType.SIDE_FIT == mFitType) {
                    float ratioMin = Math.min(wRatio, hRatio);
                    videoWidth = (int) Math.ceil((float) videoWidth / ratioMin);
                    videoHeight = (int) Math.ceil((float) videoHeight / ratioMin);
                } else if (FitType.FULL_FIT == mFitType) {
                    videoWidth = (int) Math.ceil((float) videoWidth / wRatio);
                    videoHeight = (int) Math.ceil((float) videoHeight / hRatio);
                }
            }
            // 设置SurfaceView的布局参数
            ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
            params.width = videoWidth;
            params.height = videoHeight;
            mSurfaceView.setLayoutParams(params);
        }
        mp.seekTo(0);
        boolean doStart = true;
        if (null != mPreparedHandler) {
            doStart = mPreparedHandler.onCallback(this);
        }
        if (doStart) {
            mp.start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mSeekFlag) {
            showLog("onSeekComplete");
            if (null != mSeekCompleteHandler) {
                mSeekCompleteHandler.onCallback(this);
            }
        }
        mSeekFlag = false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        showLog("onVideoSizeChanged, width = " + width + ", height = " + height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        showLog("surfaceCreated");
        if (null != mPlayer) {
            mPlayer.setDisplay(holder);
            // 创建SurfaceHolder的时候,如果存在上次播放的位置,则按照上次播放位置进行播放
            if (mCurrentPosition > 0) {
                mPlayer.seekTo(mCurrentPosition);
                mCurrentPosition = 0;
            }
            if (mPlayFlag) {
                mPlayer.start();
            }
        }
        mPlayFlag = false;
        if (null != mSurfaceCreatedHandler) {
            mSurfaceCreatedHandler.onCallback(this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        showLog("surfaceChanged, format = " + format + ", width = " + width + ", height = " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        showLog("surfaceDestroyed");
        // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
        if (null != mPlayer) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                mPlayFlag = true;
            }
            mCurrentPosition = mPlayer.getCurrentPosition();
        }
        if (null != mSurfaceDestroyedHandler) {
            mSurfaceDestroyedHandler.onCallback(this);
        }
    }

    // 播放
    public void play(String path, boolean isLoop) {
        showLog("play, path = " + path + ", isLoop = " + String.valueOf(isLoop));
        try {
            if (null != mPlayer) {
                mPlayer.reset();
                mPlayer.setDataSource(path);
                mPlayer.setLooping(isLoop);
                mPlayer.prepareAsync();
            }
        } catch (IOException e) {
            showLog("play, " + e.toString());
            e.printStackTrace();
        }
    }

    // 重播
    public void replay() {
        showLog("replay");
        if (null != mPlayer) {
            mPlayer.seekTo(0);
            mPlayer.start();
        }
    }

    // 指定播放位置,position(毫秒)
    public void seekTo(int position) {
        showLog("seekTo, position = " + position);
        mSeekFlag = true;
        if (null != mPlayer) {
            mPlayer.seekTo(position);
        }
    }

    // 暂停
    public void pause() {
        showLog("pause");
        if (null != mPlayer && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    // 继续
    public void resume() {
        showLog("resume");
        if (null != mPlayer && !mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    // 停止
    public void stop(boolean isRelease) {
        showLog("stop, isRelease = " + String.valueOf(isRelease));
        if (null != mPlayer) {
            mPlayer.stop();
            if (isRelease) {
                mPlayer.release();
                mPlayer = null;
            }
        }
    }

    // 获取视频长度(毫秒)
    public int getLength() {
        return (null != mPlayer) ? mPlayer.getDuration() : 0;
    }

    // 获取当前播放位置(毫秒)
    public int getPosition() {
        return (null != mPlayer) ? mPlayer.getCurrentPosition() : 0;
    }

    // 是否已销毁
    public boolean isDestroyed() {
        return null == mPlayer;
    }

    // 是否在播放
    public boolean isPlaying() {
        return (null != mPlayer) && mPlayer.isPlaying();
    }

    // 获取播放器
    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    // 获取表层
    public SurfaceView getView() {
        return mSurfaceView;
    }

    // 设置表层被创建处理器(当播放器被创建,或从后台切换到前台时触发)
    public void setSurfaceCreatedHandler(SurfaceCreatedHandler handler) {
        mSurfaceCreatedHandler = handler;
    }

    // 设置表层被销毁处理器(当播放器被销毁,或从前台进入到后台时触发)
    public void setSurfaceDestroyedHandler(SurfaceDestroyedHandler handler) {
        mSurfaceDestroyedHandler = handler;
    }

    // 设置准备完成处理器(当就绪可以立马播放时触发)
    public void setPreparedHandler(PreparedHandler handler) {
        mPreparedHandler = handler;
    }

    // 设置指定播放位置完成处理器
    public void setSeekCompleteHandler(SeekCompleteHandler handler) {
        mSeekCompleteHandler = handler;
    }

    // 设置结束处理器(当不循环播放时才可被触发)
    public void setCompleteHandler(CompleteHandler handler) {
        mCompleteHandler = handler;
    }

    // 设置错误处理器
    public void setErrorHandler(ErrorHandler handler) {
        mErrorHandler = handler;
    }

    // 设置适配类型
    public void setFitType(FitType fitType) {
        mFitType = fitType;
    }

    // 设置日志标签
    public void setLogTag(String tag) {
        mLogTag = tag;
    }
}
