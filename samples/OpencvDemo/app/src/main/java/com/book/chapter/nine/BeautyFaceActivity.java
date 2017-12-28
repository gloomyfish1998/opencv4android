package com.book.chapter.nine;

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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import gloomyfish.opencvdemo.ImageSelectUtils;
import gloomyfish.opencvdemo.R;

public class BeautyFaceActivity extends AppCompatActivity implements View.OnClickListener{
    private int option;
    private float sigma = 30.0f;
    private int REQUEST_CAPTURE_IMAGE = 1;
    private Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty_face);
        Button selectBtn = this.findViewById(R.id.select_image_btn);
        Button processBtn = this.findViewById(R.id.nine_process_btn);

        selectBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        option = getIntent().getIntExtra("TYPE", 0);
        System.loadLibrary("face_detection");
        if(option == 1) {
            this.setTitle("积分图计算演示");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.nine_process_btn:
                Integral_Image_Demo();
                break;
            default:
                break;
        }
    }

    public void Integral_Image_Demo() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        Mat dst = new Mat(src.size(), src.type());
        Mat mask = new Mat(src.size(), CvType.CV_8UC1);
        Mat sum = new Mat();
        Mat sqsum = new Mat();
        int w = src.cols();
        int h = src.rows();
        int ch = src.channels();
        int[] data1 = new int[(w+1)*(h+1)*ch];
        float[] data2 = new float[(w+1)*(h+1)*ch];
        Imgproc.integral2(src, sum, sqsum, CvType.CV_32S, CvType.CV_32F);
        sum.get(0, 0, data1);
        sqsum.get(0, 0, data2);
        if(option == 1) {
            blur_demo(src, sum, dst);
        } else if(option == 2) {
            FastEPFilter(src, data1, data2, dst);
        } else if(option == 3) {
            generateMask(src, mask);
            Core.bitwise_and(src, src, dst, mask);
        } else if(option == 4) {
            beautySkinFilter(src.getNativeObjAddr(), dst.getNativeObjAddr(),sigma, false);
            //generateMask(src, mask);
            //FastEPFilter(src, data1, data2, dst);
            //blendImage(src, dst, mask);
            //enhanceEdge(src, dst, mask);
        }

        // 转换为Bitmap，显示
        Bitmap bm = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);

        // show
        ImageView iv = (ImageView)this.findViewById(R.id.chapter9_imageView);
        iv.setImageBitmap(bm);

        // release memory
        src.release();
        dst.release();
        sum.release();
        sqsum.release();
        data1 = null;
        data2 = null;
        mask.release();
        result.release();
    }

    private void enhanceEdge(Mat src, Mat dst, Mat mask) {
        Imgproc.Canny(src, mask, 150, 300, 3, true);
        Core.bitwise_and(src, src, dst, mask);
        Imgproc.GaussianBlur(dst, dst, new Size(3, 3), 0.0);
    }

    private void blendImage(Mat src, Mat dst, Mat mask) {
        Mat blur_mask = new Mat();
        Mat blur_mask_f = new Mat();

        // 高斯模糊
        Imgproc.GaussianBlur(mask, blur_mask, new Size(3, 3), 0.0);
        blur_mask.convertTo(blur_mask_f, CvType.CV_32F);
        Core.normalize(blur_mask_f, blur_mask_f, 1.0, 0, Core.NORM_MINMAX);

        // 获取数据
        int w = src.cols();
        int h = src.rows();
        int ch = src.channels();
        byte[] data1 = new byte[w*h*ch];
        byte[] data2 = new byte[w*h*ch];
        float[] mdata = new float[w*h];
        blur_mask_f.get(0, 0, mdata);
        src.get(0, 0, data1);
        dst.get(0, 0, data2);

        // 高斯权重混合
        for(int row=0; row<h; row++) {
            for(int col=0; col<w; col++) {
                int b1 = data1[row*ch*w + col*ch]&0xff;
                int g1 = data1[row*ch*w + col*ch+1]&0xff;
                int r1 = data1[row*ch*w + col*ch+2]&0xff;

                int b2 = data2[row*ch*w + col*ch]&0xff;
                int g2 = data2[row*ch*w + col*ch+1]&0xff;
                int r2 = data2[row*ch*w + col*ch+2]&0xff;

                float w2 = mdata[row*w + col];
                float w1 = 1.0f - w2;

                b2 = (int)(b2*w2 + w1*b1);
                g2 = (int)(g2*w2 + w1*g1);
                r2 = (int)(r2*w2 + w1*r1);

                data2[row*ch*w + col*ch]=(byte)b2;
                data2[row*ch*w + col*ch+1]=(byte)g2;
                data2[row*ch*w + col*ch+2]=(byte)r2;
            }
        }
        dst.put(0, 0, data2);

        // 释放内存
        blur_mask.release();
        blur_mask_f.release();
        data1 = null;
        data2 = null;
        mdata = null;
    }

    private void generateMask(Mat src, Mat mask) {
        int w = src.cols();
        int h = src.rows();
        byte[] data = new byte[3];
        Mat ycrcb = new Mat();
        DefaultSkinFinder skinFinder = new DefaultSkinFinder();
        Imgproc.cvtColor(src, ycrcb, Imgproc.COLOR_BGR2YCrCb);
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                ycrcb.get(row, col, data);
                int y = data[0]&0xff;
                int cr = data[1]&0xff;
                int cb = data[2]&0xff;
                //if ((y > 80) && (85 < cb && cb < 135) && (135 < cr && cr < 180)) {
                if(skinFinder.yCrCbSkin(y, cr, cb)) {
                    mask.put(row, col, new byte[]{(byte) 255});
                }
                //}
            }
        }
        ycrcb.release();
    }

    private void FastEPFilter(Mat src, int[] sum, float[] sqsum, Mat dst) {
        int w = src.cols();
        int h = src.rows();
        int x2 = 0, y2 = 0;
        int x1 = 0, y1 = 0;
        int ksize = 15;
        int radius = ksize / 2;
        int ch = src.channels();
        byte[] data = new byte[ch*w*h];
        src.get(0, 0, data);
        int cx = 0, cy = 0;
        float sigma2 = sigma*sigma;
        for (int row = radius; row < h + radius; row++) {
            y2 = (row + 1)>h ? h : (row + 1);
            y1 = (row - ksize) < 0 ? 0 : (row - ksize);
            for (int col = 0; col < w + radius; col++) {
                x2 = (col + 1)>w ? w : (col + 1);
                x1 = (col - ksize) < 0 ? 0 : (col - ksize);
                cx = (col - radius) < 0 ? 0 : col - radius;
                cy = (row - radius) < 0 ? 0 : row - radius;
                int num = (x2 - x1)*(y2 - y1);
                for (int i = 0; i < ch; i++) {
                    int s = getblockMean(sum, x1, y1, x2, y2, i, w+1);
                    float var = getblockSqrt(sqsum, x1, y1, x2, y2, i, w+1);

                    // 计算系数K
                    float dr = (var - (s*s) / num) / num;
                    float mean = s / num;
                    float kr = dr / (dr + sigma2);

                    // 得到滤波后的像素值
                    int r = data[cy*ch*w + cx*ch+i]&0xff;
                    r = (int)((1 - kr)*mean + kr*r);
                    data[cy*ch*w + cx*ch+i] = (byte)r;
                }
            }
        }
        dst.put(0, 0, data);
    }

    private int getblockMean(int[] sum, int x1, int y1, int x2, int y2, int i, int w) {
        int tl = sum[y1*3*w + x1*3+i];
        int tr = sum[y2*3*w + x1*3+i];
        int bl = sum[y1*3*w + x2*3+i];
        int br = sum[y2*3*w + x2*3+i];
        int s = (br - bl - tr + tl);
        return s;
    }

    private float getblockSqrt(float[] sum, int x1, int y1, int x2, int y2, int i, int w) {
        float tl = sum[y1*3*w + x1*3+i];
        float tr = sum[y2*3*w + x1*3+i];
        float bl = sum[y1*3*w + x2*3+i];
        float br = sum[y2*3*w + x2*3+i];
        float var = (br - bl - tr + tl);
        return var;
    }

    private void blur_demo(Mat src, Mat sum, Mat dst) {
        int w = src.cols();
        int h = src.rows();
        int x2 = 0, y2 = 0;
        int x1 = 0, y1 = 0;
        int ksize = 15;
        int radius = ksize / 2;
        int ch = src.channels();
        byte[] data = new byte[ch*w*h];
        int[] tl = new int[3];
        int[] tr = new int[3];
        int[] bl = new int[3];
        int[] br = new int[3];
        int cx = 0;
        int cy = 0;
        for (int row = 0; row < h+radius; row++) {
            y2 = (row+1)>h?h:(row+1);
            y1 = (row - ksize) < 0 ? 0 : (row - ksize);
            for (int col = 0; col < w+radius; col++) {
                x2 = (col+1)>w?w:(col+1);
                x1 = (col - ksize) < 0 ? 0 : (col - ksize);
                sum.get(y1, x1,tl);
                sum.get(y2, x1,tr);
                sum.get(y1, x2,bl);
                sum.get(y2, x2,br);
                cx = (col - radius) < 0 ? 0 : col - radius;
                cy = (row - radius) < 0 ? 0 : row - radius;
                for (int i = 0; i < ch; i++) {
                    int num = (x2 - x1)*(y2 - y1);
                    int x = (br[i] - bl[i] - tr[i] + tl[i]) / num;
                    data[cy*ch*w + cx*ch+i] = (byte)x;
                }
            }
        }
        dst.put(0, 0, data);
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
        ImageView imageView = (ImageView)this.findViewById(R.id.chapter9_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }

    public native void beautySkinFilter(long srcAddress, long dstAddress, float sigma, boolean blur);
}
