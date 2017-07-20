package io.comi.rn.barcode.scanner;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import io.comi.rn.barcode.scanner.camera.CameraConfigurationManager;
import io.comi.rn.barcode.scanner.camera.CameraManager;
import io.comi.rn.barcode.scanner.camera.FrontLightMode;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

public abstract class AbsCaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "AbsCaptureActivity";
    private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

    private CameraManager mCameraManager;
    //private ViewfinderView mViewfinderView;

    private boolean mHasSurface;
    private InactivityTimer mInactivityTimer;

    private BeepManager mBeepManager;
    //自动开启闪光灯
    private AmbientLightManager mAmbientLightManager;

    private CameraConfigurationManager mCameraConfigurationManager;

    private CaptureActivityHandler mHandler;

    protected Collection<BarcodeFormat> mDecodeFormats;
    protected String mCharacterSet;
    protected Map<DecodeHintType, ?> mDecodeHints;
    protected Size mScannerWindowSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHasSurface = false;
        mInactivityTimer = new InactivityTimer(this);
        mBeepManager = new BeepManager(this, getBeepSource());
        mCameraConfigurationManager = new CameraConfigurationManager(this);
        mAmbientLightManager = new AmbientLightManager(this, mCameraConfigurationManager);
        mCameraManager = new CameraManager(getApplicationContext(), mCameraConfigurationManager);
        initConfig();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getViewfinder().setCameraManager(mCameraManager);
        if (mScannerWindowSize != null && mScannerWindowSize.getHeight() > 0 && mScannerWindowSize.getWidth() > 0) {
            mCameraManager.setManualFramingRect(mScannerWindowSize.getWidth(), mScannerWindowSize.getHeight());
        }
        mBeepManager.updatePrefs();
        mAmbientLightManager.start(mCameraManager);
        mInactivityTimer.onResume();

        SurfaceHolder surfaceHolder = getSurfaceView().getHolder();
        if (mHasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    protected abstract SurfaceView getSurfaceView();

    public abstract IViewfinder getViewfinder() ;

    public void drawViewfinder() {
        getViewfinder().drawViewfinder();
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    public Handler getHandler() {
        return mHandler;
    }

    /**
     * 一个有效条码已经被识别到，
     *
     * @param rawResult 解析原始结果
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        mInactivityTimer.onActivity();
        if (barcode != null) {
            mBeepManager.playBeepSoundAndVibrate();
            if (handleDecodeInternally(rawResult, barcode)) {
                restartAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            }
        }
    }

    /**
     * 延迟 m 毫秒之后重启扫码识别任务
     */
    public void restartAfterDelay(long delayMS) {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(IDecodeMessageIds.RESTART_PREVIEW, delayMS);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHasSurface = false;
    }

    /**
     * 处理解析结果
     *
     * @return 如果返回true，则处理完这次结果后继续扫描，如果返回false，则终结扫描
     */
    public abstract boolean handleDecodeInternally(Result rawResult, Bitmap barcode);

    @Override
    protected void onPause() {
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        mInactivityTimer.onPause();
        mAmbientLightManager.stop();
        mBeepManager.close();
        mCameraManager.closeDriver();
        if (!mHasSurface) {
            SurfaceHolder surfaceHolder = getSurfaceView().getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mInactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            if (mHandler == null) {
                mHandler = new CaptureActivityHandler(this, mDecodeFormats, mDecodeHints,
                        mCharacterSet, mCameraManager);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    protected void initConfig() {
        mDecodeFormats = EnumSet.of(BarcodeFormat.QR_CODE);
        mDecodeHints = null;
        mCharacterSet = null;
        mScannerWindowSize = null;
    }


    /** 该方法必须实现，用于定制扫描提示音 */
    @RawRes
    protected abstract int getBeepSource();

    /** 闪光灯模式：自动，关闭，开启 */
    protected void setLightMode(FrontLightMode lightMode) {
        mCameraConfigurationManager.setTorchMode(lightMode);
        mCameraManager.setTorch(lightMode == FrontLightMode.ON);
        mAmbientLightManager.updateConfig();
    }

    /**
     * 用于设置扫描提示音，震动等
     */
    protected BeepManager getBeepManager() {
        return mBeepManager;
    }
}
