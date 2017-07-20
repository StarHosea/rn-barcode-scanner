package io.comi.rn.barcode.scanner.impl;

import android.graphics.Bitmap;
import com.google.zxing.LuminanceSource;

/**
 * Created by Star on 2016/12/23.
 */

public class BitmapLuminanceSource extends LuminanceSource {

    private byte[] bitmapPixels;

    public BitmapLuminanceSource(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        // 首先，要取得该图片的像素数组内容
        int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
        this.bitmapPixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(data, 0, getWidth(), 0, 0, getWidth(), getHeight());

        // 将int数组转换为byte数组
        for (int i = 0; i < data.length; i++) {
            this.bitmapPixels[i] = (byte) data[i];
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        System.arraycopy(bitmapPixels, y * getWidth(), row, 0, getWidth());
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return bitmapPixels;
    }
}
