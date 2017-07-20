/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.comi.rn.barcode.scanner.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import io.comi.rn.barcode.scanner.camera.open.CameraFacing;
import io.comi.rn.barcode.scanner.camera.open.OpenCamera;
import java.util.List;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
public final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";

    private final Context context;
    private int cwNeededRotation;
    private int cwRotationFromDisplayToCamera;
    private Point screenResolution;
    private Point cameraResolution;
    private Point bestPreviewSize;
    private Point previewSizeOnScreen;
    //自动对焦
    private boolean enableAutoFocus = true;
    //连续对焦
    private boolean enableContinueFocusMode = true;
    //反转色扫描
    private boolean enableInvertScan = false;
    //场景模式
    private boolean enableBarcodeSceneMode = false;
    //会议
    private boolean enableMeeting = false;
    //闪光灯模式（默认关闭）
    private FrontLightMode torchMode = FrontLightMode.OFF;
    //曝光
    private boolean enableExposure = false;

    public CameraConfigurationManager(Context context) {
        this.context = context;
    }

    public boolean autoFocus() {
        return enableAutoFocus;
    }

    public boolean continueFocusMode() {
        return enableContinueFocusMode;
    }

    public void setEnableAutoFocus(boolean enableAutoFocus) {
        this.enableAutoFocus = enableAutoFocus;
    }

    public void setEnableContinueFocusMode(boolean enableContinueFocusMode) {
        this.enableContinueFocusMode = enableContinueFocusMode;
    }

    public boolean isEnableInvertScan() {
        return enableInvertScan;
    }

    public void setEnableInvertScan(boolean enableInvertScan) {
        this.enableInvertScan = enableInvertScan;
    }

    public boolean isEnableBarcodeSceneMode() {
        return enableBarcodeSceneMode;
    }

    public void setEnableBarcodeSceneMode(boolean enableBarcodeSceneMode) {
        this.enableBarcodeSceneMode = enableBarcodeSceneMode;
    }

    public boolean isEnableMeeting() {
        return enableMeeting;
    }

    public void setEnableMeeting(boolean enableMeeting) {
        this.enableMeeting = enableMeeting;
    }

    public FrontLightMode getTorchMode() {
        return torchMode;
    }

    public void setTorchMode(FrontLightMode torchMode) {
        this.torchMode = torchMode;
    }

    public boolean isEnableExposure() {
        return enableExposure;
    }

    public void setEnableExposure(boolean enableExposure) {
        this.enableExposure = enableExposure;
    }

    public int getCwRotationFromDisplayToCamera(){
        return cwRotationFromDisplayToCamera;
    }
    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(OpenCamera camera) {
        Camera.Parameters parameters = camera.getCamera().getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotationFromNaturalToDisplay = 270;
                break;
            default:
                // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("Bad rotation: " + displayRotation);
                }
        }
        Log.i(TAG, "Display at: " + cwRotationFromNaturalToDisplay);

        int cwRotationFromNaturalToCamera = camera.getOrientation();
        Log.i(TAG, "Camera at: " + cwRotationFromNaturalToCamera);

        // Still not 100% sure about this. But acts like we need to flip this:
        if (camera.getFacing() == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
            Log.i(TAG, "Front camera overriden to: " + cwRotationFromNaturalToCamera);
        }

        cwRotationFromDisplayToCamera =
                (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
        Log.i(TAG, "Final display orientation: " + cwRotationFromDisplayToCamera);
        if (camera.getFacing() == CameraFacing.FRONT) {
            Log.i(TAG, "Compensating rotation for front camera");
            cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360;
        } else {
            cwNeededRotation = cwRotationFromDisplayToCamera;
        }
        Log.i(TAG, "Clockwise rotation from display to camera: " + cwNeededRotation);

        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        screenResolution = theScreenResolution;
        Log.i(TAG, "Screen resolution in current orientation: " + screenResolution);
        cameraResolution =
                CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
        Log.i(TAG, "Camera resolution: " + cameraResolution);
        bestPreviewSize =
                CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
        Log.i(TAG, "Best available preview size: " + bestPreviewSize);

        boolean isScreenPortrait = screenResolution.x < screenResolution.y;
        boolean isPreviewSizePortrait = bestPreviewSize.x < bestPreviewSize.y;

        if (isScreenPortrait == isPreviewSizePortrait) {
            previewSizeOnScreen = bestPreviewSize;
        } else {
            previewSizeOnScreen = new Point(bestPreviewSize.y, bestPreviewSize.x);
        }
        Log.i(TAG, "Preview size on screen: " + previewSizeOnScreen);
    }

    void setDesiredCameraParameters(OpenCamera camera, boolean safeMode) {

        Camera theCamera = camera.getCamera();
        Camera.Parameters parameters = theCamera.getParameters();

        if (parameters == null) {
            Log.w(TAG,
                    "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG, "In camera initConfig safe mode -- most settings will not be honored");
        }

        initializeTorch(parameters, safeMode);

        CameraConfigurationUtils.setFocus(parameters, enableAutoFocus, enableContinueFocusMode,
                safeMode);
        if (!safeMode) {
            if (enableInvertScan) {
                CameraConfigurationUtils.setInvertColor(parameters);
            }

            if (enableBarcodeSceneMode) {
                CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            }

            if (enableMeeting) {
                CameraConfigurationUtils.setVideoStabilization(parameters);
                CameraConfigurationUtils.setFocusArea(parameters);
                CameraConfigurationUtils.setMetering(parameters);
            }
        }

        parameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y);

        //TODO: 设置AntiBanding
        List<String> supportedAntibanding = parameters.getSupportedAntibanding();
        if (supportedAntibanding != null && supportedAntibanding.contains(
                Camera.Parameters.ANTIBANDING_AUTO)) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        }
        parameters.setRotation(cwRotationFromDisplayToCamera);
        theCamera.setParameters(parameters);

        theCamera.setDisplayOrientation(cwRotationFromDisplayToCamera);
        Log.d(TAG, "旋转: ---------------" + cwRotationFromDisplayToCamera);
        Camera.Parameters afterParameters = theCamera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null && (bestPreviewSize.x != afterSize.width
                || bestPreviewSize.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size "
                    + bestPreviewSize.x
                    + 'x'
                    + bestPreviewSize.y
                    +
                    ", but after setting it, preview size is "
                    + afterSize.width
                    + 'x'
                    + afterSize.height);
            bestPreviewSize.x = afterSize.width;
            bestPreviewSize.y = afterSize.height;
        }
    }

    Point getBestPreviewSize() {
        return bestPreviewSize;
    }

    Point getPreviewSizeOnScreen() {
        return previewSizeOnScreen;
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    int getCWNeededRotation() {
        return cwNeededRotation;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = parameters.getFlashMode();
                return flashMode != null && (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)
                        || Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
            }
        }
        return false;
    }

    void setTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, newSetting, false);
        camera.setParameters(parameters);
    }

    private void initializeTorch(Camera.Parameters parameters, boolean safeMode) {
        boolean currentSetting = torchMode == FrontLightMode.ON;
        doSetTorch(parameters, currentSetting, safeMode);
    }

    private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
        CameraConfigurationUtils.setTorch(parameters, newSetting);
        if (!safeMode && enableExposure) {
            CameraConfigurationUtils.setBestExposure(parameters, newSetting);
        }
    }
}
