package io.comi.rn.barcode.scanner.impl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.Result;
import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;
import io.comi.rn.barcode.scanner.AbsCaptureActivity;
import io.comi.rn.barcode.scanner.IViewfinder;
import io.comi.rn.barcode.scanner.Size;
import io.comi.rn.barcode.scanner.camera.FrontLightMode;
import io.comi.rn.scanner.R;

/**
 * 扫码界面成品
 * Created by Star on 2016/12/8.
 */

public class CaptureActivity extends AbsCaptureActivity implements View.OnClickListener {

    private static final String TAG = "CaptureActivity";
    public static final int RESULT_CODE_SCAN = 1001;
    private static final int RESULT_LOAD_IMAGE = 123;

    private SurfaceView mSurfaceView;
    private ViewfinderView mViewfinderView;
    private ImageView mFlashSwitcherView;

    private ImageDecodeTask mDecodeTask;

    //标题
    protected String mTitle;
    protected String mTip;
    protected boolean mIsMaterial;
    protected int mWindowColor;
    protected Size mImagePickerWindowsSize;
    private boolean isFlashOn;
    private MaterialDialog mAlertDialog;
    private ImagePicker mImagePicker;

    public static void startActivityForResult(Activity activity, int requestCode,
            CaptureConfig captureConfig) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        CaptureIntentUtil.putCaptureConfig(intent, captureConfig);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //默认闪光灯是关闭的
        isFlashOn = false;
        setContentView(R.layout.activity_capture);
        setupHeader();
        setupScannerView();
        setupImagePicker();
    }

    //初始化顶部导航栏
    private void setupHeader() {
        //返回键
        ImageView backView = (ImageView) findViewById(R.id.backView);
        TextView titleView = (TextView) findViewById(R.id.titleView);

        RelativeLayout headerView = (RelativeLayout) findViewById(R.id.headerView);
        ImageView imagePickerView = (ImageView) findViewById(R.id.imagePicker);
        mFlashSwitcherView = (ImageView) findViewById(R.id.flashSwitcher);

        backView.setOnClickListener(this);
        titleView.setText(mTitle);
        imagePickerView.setOnClickListener(this);
        mFlashSwitcherView.setOnClickListener(this);
        if (mIsMaterial) {
            //根据是否Material Design 来设置标题的大小
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.material_title_text_size));
            //返回箭头换成 <-
            backView.setImageResource(R.drawable.material_back);
        }
    }

    //初始化扫描界面
    private void setupScannerView() {
        mViewfinderView = (ViewfinderView) findViewById(R.id.viewfinderView);
        mSurfaceView = (SurfaceView) findViewById(R.id.previewView);

        mViewfinderView.setWindowColor(mWindowColor);
        mViewfinderView.setTextBelowWindow(mTip);
    }

    /**
     * 初始化扫码参数
     */
    @Override
    protected void initConfig() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mDecodeFormats = CaptureIntentUtil.getDecodeFormats(intent);
        mCharacterSet = CaptureIntentUtil.getDecodeCharset(intent);
        mScannerWindowSize = CaptureIntentUtil.getScannerWindowSize(intent);
        mTitle = CaptureIntentUtil.getTitle(intent);
        mTip = CaptureIntentUtil.getTip(intent);
        mWindowColor = CaptureIntentUtil.getWindowColor(intent);
        mIsMaterial = CaptureIntentUtil.getMaterialMode(intent);
        mImagePickerWindowsSize = CaptureIntentUtil.getImagePickerWindowSize(intent);
    }

    @Override
    protected SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    @Override
    public IViewfinder getViewfinder() {
        return mViewfinderView;
    }

    /**
     * 重写此方法来在扫码页面内部处理编码结果，
     * 默认的处理方式是返回结果个上一个页面
     */
    @Override
    public boolean handleDecodeInternally(Result rawResult, Bitmap barcode) {
        if (rawResult == null) {
            Log.d(TAG, "没有识别到结果");
            showNoScanResultDialog();
            return true;
        }
        Log.d(TAG, "扫描识别结果：" + rawResult.getText());
        Intent intent = new Intent();
        CaptureIntentUtil.putResult(intent, rawResult);
        setResult(Activity.RESULT_OK, intent);

        finish();
        return false;
    }

    @Override
    protected int getBeepSource() {
        return R.raw.beep;
    }

    @Override
    public void onClick(View v) {
        //返回键
        if (v.getId() == R.id.backView) {
            finish();
            return;
        }
        //闪光灯开关
        if (v.getId() == R.id.flashSwitcher) {
            if (isFlashOn) {
                setLightMode(FrontLightMode.OFF);
                isFlashOn = false;
                mFlashSwitcherView.setImageResource(R.drawable.flash_off);
            } else {
                setLightMode(FrontLightMode.ON);
                isFlashOn = true;
                mFlashSwitcherView.setImageResource(R.drawable.flash_on);
            }
        }
        //选择本地图片
        if (v.getId() == R.id.imagePicker) {
            mImagePicker.startChooser(this, new ImagePicker.Callback() {
                @Override
                public void onPickImage(Uri imageUri) {

                }

                @Override
                public void onCropImage(Uri imageUri) {
                    if (imageUri == null) {
                        Toast.makeText(CaptureActivity.this, R.string.no_image_selected,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    decodeImage(imageUri.getPath());
                }

                @Override
                public void cropConfig(CropImage.ActivityBuilder builder) {
                    builder.setMultiTouchEnabled(true)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setRequestedSize(640, 640)
                            .setAspectRatio(5, 5);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 选择本地图片的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    /**
     * 返回键按下时，如果当前还在执行解码本地图片的任务，则停止任务，
     * 否则，关闭页面
     */
    @Override
    public void onBackPressed() {
        if (mDecodeTask != null && mDecodeTask.getStatus() == AsyncTask.Status.RUNNING) {
            mDecodeTask.cancel(true);
            return;
        }
        super.onBackPressed();
    }

    /**
     * //TODO:图片选择页面的国际化
     * 选择本地图片的初始化。这里使用了第三方库，
     */
    private void setupImagePicker() {
        mImagePicker = new ImagePicker();
        mImagePicker.setCropImage(true);
    }

    /**
     * 解析本地图片
     */
    private void decodeImage(String imagePath) {
        if (mDecodeTask != null && mDecodeTask.getStatus() == AsyncTask.Status.RUNNING) {
            mDecodeTask.cancel(true);
        }
        mDecodeTask = new ImageDecodeTask(CaptureActivity.this, mDecodeHints) {
            @Override
            protected void onPostExecute(DecodeResult decodeResult) {
                super.onPostExecute(decodeResult);
                handleDecodeInternally(decodeResult.getResult(), decodeResult.getOriBitmap());
            }
        };
        mDecodeTask.execute(imagePath);
    }

    /**
     * 弹出对话框,提示找不到条码
     */
    private void showNoScanResultDialog() {
        mAlertDialog = new MaterialDialog.Builder(this).content(R.string.scanner_result_not_found)
                .cancelable(false)
                .positiveText(R.string.scanner_dialog_ok)
                .show();
    }

    /**
     * 销毁时，注意要销毁解码任务和对话框
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDecodeTask != null) {
            mDecodeTask.cancel(true);
            mDecodeTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }
}
