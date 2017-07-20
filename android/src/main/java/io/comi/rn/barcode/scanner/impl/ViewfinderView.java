package io.comi.rn.barcode.scanner.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import com.google.zxing.ResultPoint;
import io.comi.rn.barcode.scanner.IViewfinder;
import io.comi.rn.barcode.scanner.camera.CameraManager;
import io.comi.rn.scanner.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * Created by Star on 2016/12/7.
 */

public class ViewfinderView extends View implements IViewfinder {

    private static final long ANIMATION_DELAY = 15L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    //private static final int MAX_RESULT_POINTS = 20;
    private static final int CORNER_WIDTH = 10;
    private static final int COLOR_TIP_TEXT = 0xFFE3E3E3;
    //private static final int MIDDLE_LINE_WIDTH = 6;

    //private static final int MIDDLE_LINE_PADDING = 5;

    private static final int SPEEN_DISTANCE = 3;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    //private final int resultPointColor;
    //private List<ResultPoint> possibleResultPoints;
    //private List<ResultPoint> lastPossibleResultPoints;

    private boolean isFirst;
    private int slideTop;

    private int screenRate;
    private float density;
    private int scannerLineHeight;
    private Drawable scannerLineDrawable;
    private int windowColor = Color.GREEN;
    private Rect lastRect;

    //扫描窗口下方的文字
    private String textBelowWindow;
    private float textMarginTop = 0;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
        //resultPointColor = resources.getColor(R.color.possible_result_points);
        density = resources.getDisplayMetrics().density;
        screenRate = (int) (20 * density);
        scannerLineHeight = (int) (20 * density);
        //possibleResultPoints = new ArrayList<>(5);
        //lastPossibleResultPoints = null;
        scannerLineDrawable = resources.getDrawable(R.drawable.saomiaoxian);
        scannerLineDrawable.setColorFilter(windowColor, PorterDuff.Mode.SRC_IN);
        textMarginTop = 48 * density;
        paint.setTextSize(14 * density);
    }

    @Override
    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    /**
     * 设置扫描框下方的提示文字
     */
    public void setTextBelowWindow(String textBelowWindow) {
        this.textBelowWindow = textBelowWindow;
        postInvalidate();
    }

    /**
     * 设置扫描框的基本色
     */
    public void setWindowColor(int windowColor) {
        if (windowColor != this.windowColor) {
            this.windowColor = windowColor;
            scannerLineDrawable.setColorFilter(windowColor, PorterDuff.Mode.SRC_IN);
            if (lastRect != null) {
                invalidate(lastRect.left, lastRect.top, lastRect.right, lastRect.bottom);
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = lastRect = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //绘制半透明背景
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            //绘制扫描框的四个拐角
            paint.setColor(windowColor);
            canvas.drawRect(frame.left, frame.top, frame.left + screenRate,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
                    frame.top + screenRate, paint);
            canvas.drawRect(frame.right - screenRate, frame.top, frame.right,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
                    frame.top + screenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + screenRate,
                    frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - screenRate, frame.left + CORNER_WIDTH,
                    frame.bottom, paint);
            canvas.drawRect(frame.right - screenRate, frame.bottom - CORNER_WIDTH, frame.right,
                    frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - screenRate, frame.right,
                    frame.bottom, paint);

            //绘制扫描线
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= frame.bottom) {
                slideTop = frame.top;
            }
            //canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2,
            //        frame.right - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, paint);

            scannerLineDrawable.setBounds(frame.left, slideTop - scannerLineHeight, frame.right,
                    slideTop + scannerLineHeight);
            scannerLineDrawable.draw(canvas);

            //绘制扫描框下方的提示文字
            if (!TextUtils.isEmpty(textBelowWindow)) {
                float textCenter = frame.left  + frame.width() / 2;
                paint.setColor(COLOR_TIP_TEXT);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(textBelowWindow,textCenter,frame.bottom + textMarginTop,paint);
            }

            //List<ResultPoint> currentPossible = possibleResultPoints;
            //List<ResultPoint> currentLast = lastPossibleResultPoints;
            //if (currentPossible.isEmpty()) {
            //    lastPossibleResultPoints = null;
            //} else {
            //    possibleResultPoints = new ArrayList<>(5);
            //    lastPossibleResultPoints = currentPossible;
            //    paint.setAlpha(OPAQUE);
            //    paint.setColor(resultPointColor);
            //    for (ResultPoint point : currentPossible) {
            //        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f,
            //                paint);
            //    }
            //}
            //if (currentLast != null) {
            //    paint.setAlpha(OPAQUE / 2);
            //    paint.setColor(resultPointColor);
            //    for (ResultPoint point : currentLast) {
            //        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f,
            //                paint);
            //    }
            //}

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right,
                    frame.bottom);
        }
    }

    @Override
    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    @Override
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    @Override
    public void addPossibleResultPoint(ResultPoint point) {
        //不再渲染PossibleResultPoint
        //List<ResultPoint> points = possibleResultPoints;
        //synchronized (points) {
        //    points.add(point);
        //    int size = points.size();
        //    if (size > MAX_RESULT_POINTS) {
        //        // trim it
        //        points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
        //    }
        //}
    }
}
