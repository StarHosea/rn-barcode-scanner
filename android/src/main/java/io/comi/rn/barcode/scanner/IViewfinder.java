package io.comi.rn.barcode.scanner;

import android.graphics.Bitmap;
import com.google.zxing.ResultPoint;
import io.comi.rn.barcode.scanner.camera.CameraManager;

/**
 * Created by Star on 2016/12/7.
 */

public interface IViewfinder {

    void setCameraManager(CameraManager cameraManager);

    void drawViewfinder();

    void drawResultBitmap(Bitmap barcode);

    void addPossibleResultPoint(ResultPoint point);
}
