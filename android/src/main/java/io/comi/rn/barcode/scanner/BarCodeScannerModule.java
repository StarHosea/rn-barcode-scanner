package io.comi.rn.barcode.scanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.util.ArrayMap;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.google.zxing.BarcodeFormat;
import io.comi.rn.barcode.scanner.impl.CaptureActivity;
import io.comi.rn.barcode.scanner.impl.CaptureConfig;
import io.comi.rn.barcode.scanner.impl.CaptureIntentUtil;
import java.util.EnumSet;
import java.util.Map;
import javax.annotation.Nullable;

public class BarCodeScannerModule extends ReactContextBaseJavaModule {

    private static final int REQUEST_SCAN_QR = 789;
    private static final String E_SCAN_CANCELLED = "E_SCAN_CANCELLED";
    private static final String E_ACTIVITY_START_ERROR = "E_ACTIVITY_START_ERROR";

    private Promise mPromise;
    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode,
                Intent data) {
            if (requestCode == REQUEST_SCAN_QR && mPromise != null) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    mPromise.reject(E_SCAN_CANCELLED, "Bar code scan was cancelled");
                } else if (resultCode == Activity.RESULT_OK) {//扫描成功，返回扫描结果
                    String decodeResult = CaptureIntentUtil.getDecodeResult(data);
                    mPromise.resolve(decodeResult);
                }
                mPromise = null;
            }
        }
    };

    public BarCodeScannerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "BarCodeScanner";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> formats = new ArrayMap<>();
        formats.put("AZTEC", BarcodeFormat.AZTEC.name());
        formats.put("CODABAR", BarcodeFormat.CODABAR.name());
        formats.put("CODE_39", BarcodeFormat.CODE_39.name());
        formats.put("CODE_93", BarcodeFormat.CODE_93.name());
        formats.put("CODE_128", BarcodeFormat.CODE_128.name());
        formats.put("DATA_MATRIX", BarcodeFormat.DATA_MATRIX.name());
        formats.put("EAN_8", BarcodeFormat.EAN_8.name());
        formats.put("EAN_13", BarcodeFormat.EAN_13.name());
        formats.put("ITF", BarcodeFormat.ITF.name());
        formats.put("MAXICODE", BarcodeFormat.MAXICODE.name());
        formats.put("PDF_417", BarcodeFormat.PDF_417.name());
        formats.put("QR_CODE", BarcodeFormat.QR_CODE.name());
        formats.put("RSS_14", BarcodeFormat.RSS_14.name());
        formats.put("RSS_EXPANDED", BarcodeFormat.RSS_EXPANDED.name());
        formats.put("UPC_A", BarcodeFormat.UPC_A.name());
        formats.put("UPC_E", BarcodeFormat.UPC_E.name());
        formats.put("UPC_EAN_EXTENSION", BarcodeFormat.UPC_EAN_EXTENSION.name());
        return formats;
    }

    @ReactMethod
    public void openBarCodeScanner(ReadableArray formats, String title, String tip,
            Promise promise) {
        Activity activity = getCurrentActivity();
        mPromise = promise;

        try {
            CaptureConfig captureConfig = genCaptureConfig(formats, title, tip);
            CaptureActivity.startActivityForResult(getCurrentActivity(), REQUEST_SCAN_QR,
                    captureConfig);
        } catch (Exception e) {
            mPromise.reject(E_ACTIVITY_START_ERROR, "e_activity_start_error");
            mPromise = null;
        }
    }

    private CaptureConfig genCaptureConfig(ReadableArray formats, String title, String tip) {
        CaptureConfig captureConfig = new CaptureConfig();
        EnumSet<BarcodeFormat> barcodeFormats = EnumSet.noneOf(BarcodeFormat.class);
        if (formats == null || formats.size() == 0) {
            //用户没有设置扫码类型，默认支持二维码
            barcodeFormats.add(BarcodeFormat.QR_CODE);
        } else {

            for (int i = 0, len = formats.size(); i < len; i++) {
                String format = formats.getString(i);
                BarcodeFormat barcodeFormat = BarcodeFormat.valueOf(format);
                barcodeFormats.add(barcodeFormat);
            }
        }

        captureConfig.setDecodeFormats(barcodeFormats);
        captureConfig.setTitle(title);
        captureConfig.setTip(tip);
        captureConfig.setScannerWindowColor(Color.GREEN);
        captureConfig.setScannerWindowSize(new Size(500, 500));
        return captureConfig;
    }
}
