package com.book.chapter.two;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import gloomyfish.opencvdemo.ImageSelectUtils;
import gloomyfish.opencvdemo.R;

public class ReadMatinfoActivity extends AppCompatActivity implements View.OnClickListener {
    private int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_matinfo);
        Button selectBtn = (Button)this.findViewById(R.id.select_image_btn);
        Button getInfoBtn = (Button)this.findViewById(R.id.get_matInfo_btn);
        selectBtn.setOnClickListener(this);
        getInfoBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.get_matInfo_btn:
                bitmap2MatDemo();
                break;
            default:
                break;
        }
    }

    private void bitmap2MatDemo() {
        Bitmap bm = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Mat m = new Mat();
        Utils.bitmapToMat(bm, m);
        Imgproc.circle(m, new Point(m.cols()/2, m.rows()/2), 50,
                new Scalar(255, 0, 0, 255), 2, 8, 0);
        Utils.matToBitmap(m, bm);

        ImageView iv = (ImageView)this.findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bm);
    }

    private void mat2BitmapDemo(int index) {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        int width = src.cols();
        int height = src.rows();
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst, bm);
        dst.release();

        ImageView iv = (ImageView)this.findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bm);
    }

    private void basicDrawOnCanvas() {
        // 创建Bitmap对象
        Bitmap bm = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);

        // 创建画布与画笔风格
        Canvas canvas = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setStyle(Paint.Style.FILL_AND_STROKE);

        // 绘制直线
        canvas.drawLine(10, 10, 490, 490, p);
        canvas.drawLine(10, 490, 490, 10, p);

        // 绘制矩形
        android.graphics.Rect rect = new android.graphics.Rect();
        rect.set(50, 50, 150, 150); // 矩形左上角点，与右下角点坐标
        canvas.drawRect(rect, p);

        // 绘制圆
        p.setColor(Color.GREEN);
        canvas.drawCircle(400, 400, 50, p);

        // 绘制文本
        p.setColor(Color.RED);
        canvas.drawText("Basic Drawing on Canvas", 40, 40, p);

        // 显示结果
        ImageView iv = (ImageView)this.findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bm);
        bm.recycle();
    }

    private void basicDrawOnMat() {
        Mat src = Mat.zeros(500, 500, CvType.CV_8UC3);

        Imgproc.ellipse(src, new Point(250, 250), new Size(100, 50),
                360, 0, 360, new Scalar(0, 0, 255), 2, 8, 0);

        Imgproc.putText(src, "Basic Drawing Demo", new Point(20, 20),
                Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(0, 255, 0), 1);
        Rect rect = new Rect();
        rect.x = 50;
        rect.y = 50;
        rect.width = 100;
        rect.height = 100;
        Imgproc.rectangle(src, rect.tl(), rect.br(), //矩形
                new Scalar(255, 0, 0), 2, 8, 0);
        Imgproc.circle(src, new Point(400, 400), 50,
                new Scalar(0, 255, 0), 2, 8, 0);
        Imgproc.line(src, new Point(10, 10), new Point(490, 490),
                new Scalar(0, 255, 0), 2, 8, 0);
        Imgproc.line(src, new Point(10, 490), new Point(490, 10),
                new Scalar(255, 0, 0), 2, 8, 0);

        Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst, bm);
        ImageView iv = (ImageView)this.findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bm);
    }

    private void getMatInfo() {
        //Mat src = Imgcodecs.imread(fileUri.getPath());
        Mat src = Imgcodecs.imread(fileUri.getPath(), Imgcodecs.IMREAD_COLOR);
        int width = src.cols();
        int height = src.rows();
        int dims = src.dims();
        int channels = src.channels();
        int depth = src.depth();
        int type = src.type();
        // 1
        Mat m1 = new Mat();
        m1.create(new Size(3, 3), CvType.CV_8UC3);
        Mat m2 = new Mat();
        m2.create(3, 3, CvType.CV_8UC3);

        Mat m3 = Mat.eye(3, 3,CvType.CV_8UC3);
        Mat m4 = Mat.eye(new Size(3, 3),CvType.CV_8UC3);
        Mat m5 = Mat.zeros(new Size(3, 3), CvType.CV_8UC3);
        Mat m6 = Mat.ones(new Size(3, 3), CvType.CV_8UC3);

        Mat m7 = new Mat(3, 3, CvType.CV_8UC3);
        m7.setTo(new Scalar(255, 255, 255));

        // 创建Mat对象并保存
        Mat image = new Mat(500, 500, CvType.CV_8UC3);
        image.setTo(new Scalar(127, 127, 127));
        ImageSelectUtils.saveImage(image);

        Mat m8 = new Mat(500, 500, CvType.CV_8UC3);
        m8.setTo(new Scalar(127, 127, 127));
        Mat result = new Mat();
        m8.copyTo(result);
    }

    public void getBitmapInfo() {
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.lena);
        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap.Config config = bm.getConfig();

        int a=0, r=0, g=0, b=0;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                // 读取像素
                int pixel = bm.getPixel(col, row);
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);
                // 修改像素
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                // 保存到Bitmap中
                bm.setPixel(col, row, Color.argb(a, r, g, b));
            }
        }

        int[] pixels = new int[width*height];
        bm.getPixels(pixels, 0, width, 0, 0, width, height);
        
    }

    private void scanPixelsDemo() {
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.lena).copy(Bitmap.Config.ARGB_8888, true);
        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap.Config config = bm.getConfig();
        int[] pixels = new int[width*height];
        bm.getPixels(pixels, 0, width, 0, 0, width, height);
        int a=0, r=0, g=0, b=0;
        int index = 0;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                // 读取像素
                index = width*row + col;
                a=(pixels[index]>>24)&0xff;
                r=(pixels[index]>>16)&0xff;
                g=(pixels[index]>>8)&0xff;
                b=pixels[index]&0xff;
                // 修改像素
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                // 保存到Bitmap中
                pixels[index] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        bm.setPixels(pixels, 0, width, 0, 0, width, height);
        ImageView iv = (ImageView)this.findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bm);
        bm.recycle();

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
        ImageView imageView = (ImageView)this.findViewById(R.id.matInfo_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }
}
