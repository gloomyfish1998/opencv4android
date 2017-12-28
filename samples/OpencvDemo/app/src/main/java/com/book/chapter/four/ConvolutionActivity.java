package com.book.chapter.four;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import gloomyfish.opencvdemo.ImageSelectUtils;
import gloomyfish.opencvdemo.R;

public class ConvolutionActivity extends AppCompatActivity implements View.OnClickListener {
    private int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convolution);
        Button selectBtn = (Button)this.findViewById(R.id.select_image_btn);
        Button processBtn = (Button)this.findViewById(R.id.convolution_btn);
        selectBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.convolution_btn:
                blurImage(0);
                break;
            default:
                break;
        }
    }

    private void blurImage(int type) {
        // read image
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        Mat dst = new Mat();

        if(type == 0) {
            Imgproc.blur(src, dst, new Size(5, 5), new Point(-1, -1), Core.BORDER_DEFAULT);
        } else if(type == 1) {
            Imgproc.GaussianBlur(src, dst, new Size(0, 0), 15);
        }else if(type == 2) {
            Imgproc.medianBlur(src, dst, 5);
        } else if(type == 3) {
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Imgproc.dilate(src, dst, kernel);
        }else if(type == 4) {
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Imgproc.erode(src, dst, kernel);
        }else if(type == 5) {
            Imgproc.bilateralFilter(src, dst, 0, 150, 15);
        }else if(type == 6) {
            Imgproc.pyrMeanShiftFiltering(src, dst, 10, 50);
        } else if(type == 7) {
            customFilter(src, dst, 0);
        } else if(type == 8) {
            morphologyDemo(src, dst, 0);
        } else if(type == 9) {
            thresholdDemo(src, dst);
        }else if(type == 10) {
            adpThresholdDemo(src, dst);
        }

        // 转换为Bitmap，显示
        Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);

        // show
        ImageView iv = (ImageView)this.findViewById(R.id.chapter4_imageView);
        iv.setImageBitmap(bm);

        // release memory
        src.release();
        dst.release();
        result.release();
    }

    private void thresholdDemo(Mat src, Mat dst) {
        int t = 127;
        int maxValue = 255;
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        // Imgproc.threshold(gray, dst, t, maxValue, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        Imgproc.threshold(gray, dst, t, maxValue, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        gray.release();
    }

    private void adpThresholdDemo(Mat src, Mat dst) {
        int t = 127;
        int maxValue = 255;
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(src, dst, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 10);
        gray.release();
    }

    private void morphologyDemo(Mat src, Mat dst, int option) {

        // 创建结构元素
        Mat k = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT, new Size(15, 15), new Point(-1, -1));

        // 形态学操作
        switch (option) {
            case 0: // 膨胀
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_DILATE, k);
                break;
            case 1: // 腐蚀
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_ERODE, k);
                break;
            case 2: // 开操作
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_OPEN, k);
                break;
            case 3: // 闭操作
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_CLOSE, k);
                break;
            case 4: // 黑帽
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_BLACKHAT, k);
                break;
            case 5: // 顶帽
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_TOPHAT, k);
                break;
            case 6: // 基本梯度
                Imgproc.morphologyEx(src, dst, Imgproc.MORPH_GRADIENT, k);
                break;
            default:
                break;
        }
    }

    private void customFilter(Mat src, Mat dst, int type) {
        if(type == 1) {
            Mat k = new Mat(3, 3, CvType.CV_32FC1);
            float[] data = new float[]{1.0f/9.0f,1.0f/9.0f,1.0f/9.0f,
                    1.0f/9.0f, 1.0f/9.0f, 1.0f/9.0f,
                    1.0f/9.0f, 1.0f/9.0f, 1.0f/9.0f};
            k.put(0, 0, data);
        } else if(type == 2) {
            Mat k = new Mat(3, 3, CvType.CV_32FC1);
            float[] data = new float[]{0,1.0f/8.0f,0,
                                        1.0f/8.0f, 0.5f, 1.0f/8.0f,
                                        0, 1.0f/8.0f, 0};
            k.put(0, 0, data);
        } else if(type == 3) {
            Mat kx = new Mat(3, 3, CvType.CV_32FC1);
            Mat ky = new Mat(3, 3, CvType.CV_32FC1);

            float[] robert_x = new float[]{-1,0,0,1};
            kx.put(0, 0, robert_x);

            float[] robert_y = new float[]{0,1,-1,0};
            ky.put(0, 0, robert_y);

            Imgproc.filter2D(src, dst, -1, kx);
            Imgproc.filter2D(src, dst, -1, ky);
        }
    }

    private void pickUpImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri uri = data.getData();
                File f = new File(ImageSelectUtils.getRealPath(uri, getApplicationContext()));
                fileUri = Uri.fromFile(f);
            }
        }
        // display it
        if(fileUri == null) return;
        ImageView imageView = (ImageView)this.findViewById(R.id.chapter4_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }
}
