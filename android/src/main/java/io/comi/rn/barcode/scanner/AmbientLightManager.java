/*
 * Copyright (C) 2012 ZXing authors
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

package io.comi.rn.barcode.scanner;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import io.comi.rn.barcode.scanner.camera.CameraConfigurationManager;
import io.comi.rn.barcode.scanner.camera.CameraManager;
import io.comi.rn.barcode.scanner.camera.FrontLightMode;

/**
 * 闪光灯，自动开启闪光灯
 */
final class AmbientLightManager implements SensorEventListener {

    private static final float TOO_DARK_LUX = 45.0f;
    private static final float BRIGHT_ENOUGH_LUX = 450.0f;

    private final Context context;
    private CameraManager cameraManager;
    private Sensor lightSensor;
    private CameraConfigurationManager mCameraConfigure;

    AmbientLightManager(Context context, CameraConfigurationManager cameraConfigurationManager) {
        this.context = context;
        this.mCameraConfigure = cameraConfigurationManager;
    }

    void start(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
        updateConfig();
    }

    void stop() {
        if (lightSensor != null) {
            SensorManager sensorManager =
                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(this);
            cameraManager = null;
            lightSensor = null;
        }
    }

    /**
     * {@link CameraConfigurationManager} 更新数据后，这里要更新
     */
    public void updateConfig() {
        //自动开启闪光灯
        if (mCameraConfigure.getTorchMode() == FrontLightMode.AUTO) {
            SensorManager sensorManager =
                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }else {
            //关闭自动开启闪光灯
            SensorManager sensorManager =
                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(this);
            lightSensor = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ambientLightLux = sensorEvent.values[0];
        if (cameraManager != null) {
            if (ambientLightLux <= TOO_DARK_LUX) {
                cameraManager.setTorch(true);
            } else if (ambientLightLux >= BRIGHT_ENOUGH_LUX) {
                cameraManager.setTorch(false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
}
