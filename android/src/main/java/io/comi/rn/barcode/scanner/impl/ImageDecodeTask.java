package io.comi.rn.barcode.scanner.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import io.comi.rn.scanner.R;
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by Star on 2016/12/23.
 */

public abstract class ImageDecodeTask
        extends AsyncTask<String, Void, ImageDecodeTask.DecodeResult> {

    private Map<DecodeHintType, ?> hints;
    private WeakReference<Context> context;
    private MaterialDialog processingDialog;

    ImageDecodeTask(Context context, Map<DecodeHintType, ?> hints) {
        this.hints = hints;
        this.context = new WeakReference<Context>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog(true);
    }

    @Override
    protected DecodeResult doInBackground(String... params) {
        String imagePath = params[0];
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }
        Result result = null;
        Bitmap image = ImageUtils.getBitmapFromFile(imagePath, 1000, 1000);
        BitmapLuminanceSource source = new BitmapLuminanceSource(image);
        GlobalHistogramBinarizer binarizer = new GlobalHistogramBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        try {
            result = new MultiFormatReader().decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return new DecodeResult(result, image);
    }

    @Override
    protected void onPostExecute(DecodeResult result) {
        showDialog(false);
        context = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        showDialog(false);
        context = null;
    }

    private void showDialog(boolean show) {
        if (show) {
            //弹出进度框
            if (processingDialog != null && processingDialog.isShowing()) {
                return;
            }
            Context context = this.context.get();
            if (context != null) {
                processingDialog = new MaterialDialog.Builder(context).progress(true, 0)
                        .content(R.string.scanner_processing)
                        .show();
            }
        } else {
            //隐藏进度框
            if (processingDialog != null && processingDialog.isShowing()) {
                processingDialog.hide();
                processingDialog.dismiss();
                processingDialog = null;
            }
        }
    }

    static class DecodeResult {
        private Result mResult;
        private Bitmap oriBitmap;

        public DecodeResult(Result result, Bitmap oriBitmap) {
            mResult = result;
            this.oriBitmap = oriBitmap;
        }

        public Result getResult() {
            return mResult;
        }

        public Bitmap getOriBitmap() {
            return oriBitmap;
        }
    }
}
