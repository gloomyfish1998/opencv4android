package com.book.chapter.five;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gloomyfish.opencvdemo.ImageSelectUtils;
import gloomyfish.opencvdemo.R;

public class ImageAnalysisActivity extends AppCompatActivity implements View.OnClickListener{
    private int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);
        Button selectBtn = (Button)this.findViewById(R.id.select_image_btn);
        Button processBtn = (Button)this.findViewById(R.id.analysis_measure_btn);
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
            case R.id.analysis_measure_btn:
                analysisImage(13);
                break;
            default:
                break;
        }
    }

    private void analysisImage(int section) {
        // read image
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        Mat dst = new Mat();

        // 演示程序部分
        if(section == 0) {
            sobelDemo(src, dst);
        } else if(section == 1) {
            scharrDemo(src, dst);
        } else if(section == 2) {
            laplianDemo(src,dst);
        } else if(section == 3) {
            edge2Demo(src, dst);
        } else if(section == 4) {
            houghLinePDemo(src, dst);
        } else if(section == 5) {
            houghLinesDemo(src, dst);
        } else if(section == 6) {
            houghCircleDemo(src, dst);
        } else if(section == 7) {
            findContoursDemo(src, dst);
        } else if(section == 8) {
            measureContours(src, dst);
        } else if(section == 9) {
            displayHistogram(src, dst);
        } else if(section == 10) {
            equalizeHistogram(src, dst);
        } else if(section == 11) {
            compareHistogram(src, dst);
        } else if(section == 12) {
            backProjectionHistogram(src, dst);
        } else if(section == 13) {
            matchTemplateDemo(src, dst);
        }


        // 转换为Bitmap，显示
        Bitmap bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);

        // show
        ImageView iv = (ImageView)this.findViewById(R.id.chapter5_imageView);
        iv.setImageBitmap(bm);

        // release memory
        src.release();
        dst.release();
        result.release();
    }

    private void matchTemplateDemo(Mat src, Mat dst) {
        String tplFilePath = fileUri.getPath().replaceAll("lena", "tmpl");
        Mat tpl = Imgcodecs.imread(tplFilePath);
        int height = src.rows() - tpl.rows() + 1;
        int width = src.cols() - tpl.cols() + 1;
        Mat result = new Mat(height, width, CvType.CV_32FC1);

        // 模板匹配
        int method = Imgproc.TM_CCOEFF_NORMED;
        Imgproc.matchTemplate(src, tpl, result, method);
        Core.MinMaxLocResult minMaxResult = Core.minMaxLoc(result);
        Point maxloc = minMaxResult.maxLoc;
        Point minloc = minMaxResult.minLoc;

        Point matchloc = null;
        if(method == Imgproc.TM_SQDIFF || method == Imgproc.TM_SQDIFF_NORMED) {
            matchloc = minloc;
        } else {
            matchloc = maxloc;
        }
        // 绘制
        src.copyTo(dst);
        Imgproc.rectangle(dst, matchloc, new Point(matchloc.x+tpl.cols(), matchloc.y + tpl.rows()), new Scalar(0, 0, 255), 2, 8, 0);

        tpl.release();
        result.release();
    }

    private void backProjectionHistogram(Mat src, Mat dst) {
        Mat hsv = new Mat();
        String sampleFilePath = fileUri.getPath().replaceAll("target", "sample");
        Mat sample = Imgcodecs.imread(sampleFilePath);
        Imgproc.cvtColor(sample, hsv, Imgproc.COLOR_BGR2HSV);
        Mat mask = Mat.ones(sample.size(), CvType.CV_8UC1);
        Mat mHist = new Mat();
        Imgproc.calcHist(Arrays.asList(hsv), new MatOfInt(0, 1), mask, mHist, new MatOfInt(30, 32), new MatOfFloat(0, 179, 0, 255));
        System.out.println(mHist.rows());
        System.out.println(mHist.cols());

        Mat srcHSV = new Mat();
        Imgproc.cvtColor(src, srcHSV, Imgproc.COLOR_BGR2HSV);

        Imgproc.calcBackProject(Arrays.asList(srcHSV), new MatOfInt(0, 1), mHist, dst, new MatOfFloat(0, 179, 0, 255), 1);
        Core.normalize(dst, dst, 0, 255, Core.NORM_MINMAX);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2BGR);
    }

    private void compareHistogram(Mat src, Mat dst) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, dst);

        // 直方图一
        List<Mat> images = new ArrayList<>();
        images.add(gray);
        Mat mask = Mat.ones(src.size(), CvType.CV_8UC1);
        Mat hist1 = new Mat();
        Imgproc.calcHist(images, new MatOfInt(0), mask, hist1, new MatOfInt(256), new MatOfFloat(0, 255));
        Core.normalize(hist1, hist1, 0, 255, Core.NORM_MINMAX);

        // 直方图二
        images.clear();
        images.add(dst);
        Mat hist2 = new Mat();
        Imgproc.calcHist(images, new MatOfInt(0), mask, hist2, new MatOfInt(256), new MatOfFloat(0, 255));
        Core.normalize(hist2, hist2, 0, 255, Core.NORM_MINMAX);

        // 比较直方图
        double[] distances = new double[7];
        distances[0] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
        distances[1] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CHISQR);
        distances[2] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_INTERSECT);
        distances[3] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_BHATTACHARYYA);
        distances[4] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_HELLINGER);
        distances[5] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CHISQR_ALT);
        distances[6] = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_KL_DIV);

        for(int i=0; i<distances.length; i++) {
            Log.i("Hist distance", "Distance Type : " + i + " d(H1,H2)=" + distances[i]);
        }
        src.copyTo(dst);
        gray.release();
        hist1.release();
        hist2.release();
    }

    private void equalizeHistogram(Mat src, Mat dst) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, dst);
        gray.release();
    }

    private void displayHistogram(Mat src, Mat dst) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // 计算直方图数据并归一化
        List<Mat> images = new ArrayList<>();
        images.add(gray);
        Mat mask = Mat.ones(src.size(), CvType.CV_8UC1);
        Mat hist = new Mat();
        Imgproc.calcHist(images, new MatOfInt(0), mask, hist, new MatOfInt(256), new MatOfFloat(0, 255));
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
        int height = hist.rows();

        dst.create(400, 400, src.type());
        dst.setTo(new Scalar(200, 200, 200));
        float[] histdata = new float[256];
        hist.get(0, 0, histdata);
        int offsetx = 50;
        int offsety = 350;

        // 绘制直方图
        Imgproc.line(dst, new Point(offsetx, 0), new Point(offsetx, offsety), new Scalar(0, 0, 0));
        Imgproc.line(dst, new Point(offsetx, offsety), new Point(400, offsety), new Scalar(0, 0, 0));
        for(int i=0; i<height-1; i++) {
            int y1 = (int)histdata[i];
            int y2 = (int)histdata[i+1];
            Rect rect = new Rect();
            rect.x = offsetx+i;
            rect.y = offsety-y1;
            rect.width = 1;
            rect.height = y1;
            Imgproc.rectangle(dst, rect.tl(), rect.br(), new Scalar(15, 15, 15));
        }

        // 释放内存
        gray.release();
    }

    private void measureContours(Mat src, Mat dst) {
        Mat gray= new Mat();
        Mat binary = new Mat();

        // 二值
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        // 轮廓发现
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        // 测量轮廓
        dst.create(src.size(), src.type());
        for(int i=0; i<contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            double w = rect.width;
            double h = rect.height;
            double rate = Math.min(w, h)/Math.max(w, h);
            Log.i("Bound Rect", "rate : " + rate);
            RotatedRect minRect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            w = minRect.size.width;
            h = minRect.size.height;
            rate = Math.min(w, h)/Math.max(w, h);
            Log.i("Min Bound Rect", "rate : " + rate);

            double area = Imgproc.contourArea(contours.get(i), false);
            double arclen = Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            Log.i("contourArea", "area : " + rate);
            Log.i("arcLength", "arcLength : " + arclen);
            Imgproc.drawContours(dst, contours, i, new Scalar(0, 0, 255), 1);
        }

        // 释放内存
        gray.release();
        binary.release();
    }

    private void findContoursDemo(Mat src, Mat dst) {
        Mat gray= new Mat();
        Mat binary = new Mat();

        // 二值
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // 轮廓发现
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        // 绘制轮廓
        dst.create(src.size(), src.type());
        for(int i=0; i<contours.size(); i++) {
            Imgproc.drawContours(dst, contours, i, new Scalar(0, 0, 255), 2);
        }

        // 释放内存
        gray.release();
        binary.release();
    }

    private void houghCircleDemo(Mat src, Mat dst) {
        Mat gray = new Mat();
        Imgproc.pyrMeanShiftFiltering(src, gray, 15, 80);
        Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(gray, gray, new Size(3, 3),  0);

        // detect circles
        Mat circles = new Mat();
        dst.create(src.size(), src.type());
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, 20, 100, 30, 10, 200);
        for(int i=0; i<circles.cols(); i++) {
            float[] info = new float[3];
            circles.get(0, i, info);
            Imgproc.circle(dst, new Point((int)info[0], (int)info[1]), (int)info[2],
                    new Scalar(0, 255, 0), 2, 8, 0);
        }
        circles.release();
        gray.release();
    }

    private void houghLinePDemo(Mat src, Mat dst) {
        Mat edges = new Mat();
        Imgproc.Canny(src, edges, 50, 150, 3, true);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180.0, 100, 50, 10);

        Mat out = Mat.zeros(src.size(), src.type());
        for(int i=0; i<lines.rows(); i++) {
            int[] oneline = new int[4];
            lines.get(i, 0, oneline);
            Imgproc.line(out, new Point(oneline[0], oneline[1]),
                    new Point(oneline[2], oneline[3]),
                    new Scalar(0, 0, 255), 2, 8, 0);
        }
        out.copyTo(dst);

        // 释放内存
        out.release();
        edges.release();
    }

    private void houghLinesDemo(Mat src, Mat dst) {
        Mat edges = new Mat();
        Imgproc.Canny(src, edges, 50, 150, 3, true);

        Mat lines = new Mat();
        Imgproc.HoughLines(edges, lines, 1,Math.PI/180.0, 200);
        Mat out = Mat.zeros(src.size(), src.type());
        float[] data = new float[2];
        for(int i=0; i<lines.rows(); i++) {
            lines.get(i, 0, data);
            float rho = data[0], theta = data[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point();
            Point pt2 = new Point();
            pt1.x = Math.round(x0 + 1000*(-b));
            pt1.y = Math.round(y0 + 1000*(a));
            pt2.x = Math.round(x0 - 1000*(-b));
            pt2.y = Math.round(y0 - 1000*(a));
            Imgproc.line(out, pt1, pt2, new Scalar(0,0,255), 3, Imgproc.LINE_AA, 0);
        }
        out.copyTo(dst);
        out.release();
        edges.release();
    }

    private void edgeDemo(Mat src, Mat dst) {
        Mat edges = new Mat();
        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0);

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(src, edges, 50, 150, 3, true);
        Core.bitwise_and(src, src, dst, edges);
    }

    private void edge2Demo(Mat src, Mat dst) {
        // X方向梯度
        Mat gradx = new Mat();
        Imgproc.Sobel(src, gradx, CvType.CV_16S, 1, 0);

        // Y方向梯度
        Mat grady = new Mat();
        Imgproc.Sobel(src, grady, CvType.CV_16S, 0, 1);

        // 边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(gradx, grady, edges, 50, 150);
        Core.bitwise_and(src, src, dst, edges);

        // 释放内存
        edges.release();
        gradx.release();
        grady.release();
    }

    private void sobelDemo(Mat src, Mat dst) {
        // X方向梯度
        Mat gradx = new Mat();
        Imgproc.Sobel(src, gradx, CvType.CV_32F, 1, 0);
        Core.convertScaleAbs(gradx, gradx);
        Log.i("OpenCV", "XGradient....");
        // Y方向梯度
        Mat grady = new Mat();
        Imgproc.Sobel(src, grady, CvType.CV_32F, 0, 1);
        Core.convertScaleAbs(grady, grady);
        Log.i("OpenCV", "YGradient....");

        Core.addWeighted(gradx,0.5, grady, 0.5, 0, dst);
        gradx.release();
        grady.release();
        Log.i("OpenCV", "Gradient.....");
    }

    private void scharrDemo(Mat src, Mat dst) {
        // X方向梯度
        Mat gradx = new Mat();
        Imgproc.Scharr(src, gradx, CvType.CV_32F, 1, 0);
        Core.convertScaleAbs(gradx, gradx);

        // Y方向梯度
        Mat grady = new Mat();
        Imgproc.Scharr(src, grady, CvType.CV_32F, 0, 1);
        Core.convertScaleAbs(grady, grady);

        Core.addWeighted(gradx,0.5, grady, 0.5, 0, dst);
    }

    private void laplianDemo(Mat src, Mat dst) {
        Imgproc.Laplacian(src, dst, CvType.CV_32F, 3, 1.0, 0);
        Core.convertScaleAbs(dst, dst);
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
        ImageView imageView = (ImageView)this.findViewById(R.id.chapter5_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }
}
