package io.comi.rn.barcode.scanner.impl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import io.comi.rn.barcode.scanner.Size;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 启动CaptureActivity时传Intent
 * Created by Star on 2016/12/8.
 */

public class CaptureIntentUtil {

    private static final String KEY_TITLE = "bs_title";

    private static final String KEY_DECODE_FORMATS = "bs_getDecodeFormats";
    private static final String KEY_CHARSET = "bs_charSet";
    private static final String KEY_FRAME_WIDTH = "bs_frameWidth";
    private static final String KEY_FRAME_HEIGHT = "bs_frameHeight";
    private static final String KEY_TIP = "bs_tip";
    private static final String KEY_WINDOW_COLOR = "bs_windowColor";

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final String KEY_RESULT_TEXT = "result_text";
    private static final String KEY_MATERIAL_DESIGN = "bs_material";
    private static final String KEY_IMAGE_PICKER_WINDOW_WIDTH = "ip_window_width";
    private static final String KEY_IMAGE_PICKER_WINDOW_HEIGHT = "ip_window_height";

    static void putCaptureConfig(Intent intent, CaptureConfig captureConfig) {
        if (intent == null || captureConfig == null) {
            return;
        }
        putDecodeFormats(intent, captureConfig.getDecodeFormats());
        putDecodeCharset(intent, captureConfig.getCharSet());
        Size scannerWindowSize = captureConfig.getScannerWindowSize();
        if (scannerWindowSize != null) {
            putScannerWindowSize(intent, scannerWindowSize.getWidth(),
                    scannerWindowSize.getHeight());
        }
        putTitle(intent, captureConfig.getTitle());
        putTip(intent, captureConfig.getTip());
        putWindowColor(intent,captureConfig.getScannerWindowColor());
        putMaterialMode(intent,captureConfig.isMaterial());
        puImagePickerWindowSize(intent,captureConfig.getImagePickerWindowSize());
    }

    private static void puImagePickerWindowSize(Intent intent, Size imagePickerWindowSize) {
        if (imagePickerWindowSize == null){
            return;
        }
        intent.putExtra(KEY_IMAGE_PICKER_WINDOW_WIDTH,imagePickerWindowSize.getWidth());
        intent.putExtra(KEY_IMAGE_PICKER_WINDOW_HEIGHT,imagePickerWindowSize.getWidth());
    }

    /**
     * 向Intent中加入要解码的类型
     */
    private static void putDecodeFormats(Intent intent, Set<BarcodeFormat> formats) {
        if (formats == null || formats.isEmpty()) {
            return;
        }
        StringBuilder formatsString = new StringBuilder();
        int lastIndex = formats.size() - 1;
        int curIndex = 0;
        for (BarcodeFormat format : formats) {
            formatsString.append(format.name());
            if (curIndex == lastIndex) {
                break;
            }
            formatsString.append(",");
            curIndex++;
        }
        String format = formatsString.toString();
        intent.putExtra(KEY_DECODE_FORMATS, format);
    }

    /** 扫码窗口大小 */
    private static void putScannerWindowSize(Intent intent, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        intent.putExtra(KEY_FRAME_WIDTH, width);
        intent.putExtra(KEY_FRAME_HEIGHT, height);
    }

    /** 解码字符集 */
    private static void putDecodeCharset(Intent intent, String charSet) {
        if (TextUtils.isEmpty(charSet)) {
            return;
        }
        intent.putExtra(KEY_CHARSET, charSet);
    }

    /** 页面标题 */
    private static void putTitle(Intent intent, String title) {
        intent.putExtra(KEY_TITLE, title);
    }

    /** 扫码提示文字 */
    private static void putTip(Intent intent, String tip) {
        intent.putExtra(KEY_TIP, tip);
    }

    private static void putWindowColor(Intent intent, int windowColor) {
        intent.putExtra(KEY_WINDOW_COLOR, windowColor);
    }

    private static void putMaterialMode(Intent intent, boolean isMaterial){
        intent.putExtra(KEY_MATERIAL_DESIGN,isMaterial);
    }

    static Set<BarcodeFormat> getDecodeFormats(@NonNull Intent intent) {
        List<String> scanFormats = null;
        String scanFormatsString = intent.getStringExtra(KEY_DECODE_FORMATS);
        if (scanFormatsString != null) {
            scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
        }
        if (scanFormats != null) {
            Set<BarcodeFormat> formats = EnumSet.noneOf(BarcodeFormat.class);
            for (String scanFormat : scanFormats) {
                formats.add(BarcodeFormat.valueOf(scanFormat));
            }
            return formats;
        }
        return null;
    }

    static String getDecodeCharset(@NonNull Intent intent) {
        return intent.getStringExtra(KEY_CHARSET);
    }

    static Size getScannerWindowSize(@NonNull Intent intent) {
        int w = intent.getIntExtra(KEY_FRAME_WIDTH, 0);
        int h = intent.getIntExtra(KEY_FRAME_HEIGHT, 0);
        return new Size(w, h);
    }

    static String getTitle(@NonNull Intent intent) {
        return intent.getStringExtra(KEY_TITLE);
    }

    static String getTip(@NonNull Intent intent) {
        return intent.getStringExtra(KEY_TIP);
    }

    static int getWindowColor(@NonNull Intent intent) {
        return intent.getIntExtra(KEY_WINDOW_COLOR, Color.GREEN);
    }

    static void putResult(Intent intent, Result result) {
        if (intent == null || result == null) {
            return;
        }
        intent.putExtra(KEY_RESULT_TEXT, result.getText());
    }

    /**
     * 解析 intent 中包含的扫码结果
     *
     * @param intent {@link Activity#onActivityResult(int, int, Intent)} 中获取的intent
     * @return 解析后的结果
     */
    public static String getDecodeResult(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(KEY_RESULT_TEXT);
    }

    public static boolean getMaterialMode(Intent intent) {
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra(KEY_MATERIAL_DESIGN,false);
    }

    public static Size getImagePickerWindowSize(Intent intent){

        if (intent == null){
            return new Size(800,800);
        }
        return new Size(intent.getIntExtra(KEY_IMAGE_PICKER_WINDOW_WIDTH,CaptureConfig.DEFAULT_IMAGE_PICKER_WIDTH),intent.getIntExtra(KEY_IMAGE_PICKER_WINDOW_HEIGHT,CaptureConfig.DEFAULT_IMAGE_PICKER_HEIGHT));
    }
}
