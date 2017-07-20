package io.comi.rn.barcode.scanner.impl;

import com.google.zxing.BarcodeFormat;
import io.comi.rn.barcode.scanner.Size;
import java.util.Set;

/**
 * 扫码界面配置参数
 * Created by Star on 2016/12/9.
 */

public class CaptureConfig {

    public static final int DEFAULT_IMAGE_PICKER_WIDTH = 800;
    public static final int DEFAULT_IMAGE_PICKER_HEIGHT = 800;

    /**
     * 需要识别的条码类型
     */
    private Set<BarcodeFormat> mDecodeFormats;
    /**
     * 识别条码的charSet，一般不需要设置
     */
    private String mCharSet;
    /**
     * 扫描界面的标题
     */
    private String mTitle;
    /**
     * 扫描窗口下方的提示文字
     */
    private String mTip;
    /**
     * 扫描窗口的大小
     */
    private Size mScannerWindowSize;
    /**
     * 扫描窗口的颜色
     */
    private int mScannerWindowColor;

    /**
     * Material Design Mode
     */
    private boolean isMaterial;

    /**
     * 选择本地图片扫码过程中需要用户手动截取一下图片，
     * 这个参数表示截图框的大小，默认是宽高800
     */
    private Size mImagePickerWindowSize =
            new Size(DEFAULT_IMAGE_PICKER_WIDTH, DEFAULT_IMAGE_PICKER_HEIGHT);

    public CaptureConfig() {
    }

    public CaptureConfig(Set<BarcodeFormat> decodeFormats, Size scannerWindowSize, String charSet,
            String title, String tip) {
        mDecodeFormats = decodeFormats;
        mScannerWindowSize = scannerWindowSize;
        mCharSet = charSet;
        mTitle = title;
        mTip = tip;
    }

    public Set<BarcodeFormat> getDecodeFormats() {
        return mDecodeFormats;
    }

    public void setDecodeFormats(Set<BarcodeFormat> decodeFormats) {
        mDecodeFormats = decodeFormats;
    }

    public Size getScannerWindowSize() {
        return mScannerWindowSize;
    }

    public void setScannerWindowSize(Size scannerWindowSize) {
        mScannerWindowSize = scannerWindowSize;
    }

    public String getCharSet() {
        return mCharSet;
    }

    public void setCharSet(String charSet) {
        mCharSet = charSet;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTip() {
        return mTip;
    }

    public void setTip(String tip) {
        mTip = tip;
    }

    public int getScannerWindowColor() {
        return mScannerWindowColor;
    }

    public void setScannerWindowColor(int scannerWindowColor) {
        this.mScannerWindowColor = scannerWindowColor;
    }

    public boolean isMaterial() {
        return isMaterial;
    }

    public void setMaterial(boolean material) {
        isMaterial = material;
    }

    public Size getImagePickerWindowSize() {
        return mImagePickerWindowSize;
    }

    public void setImagePickerWindowSize(Size imagePickerWindowSize) {
        mImagePickerWindowSize = imagePickerWindowSize;
    }
}
