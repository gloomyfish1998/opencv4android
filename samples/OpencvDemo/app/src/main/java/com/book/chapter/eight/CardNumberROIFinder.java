package com.book.chapter.eight;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gloomy fish on 2017/12/07.
 */

public class CardNumberROIFinder {

    public static Bitmap extractNumberROI(Bitmap input, Bitmap template) {
        Mat src = new Mat();
        Mat tpl = new Mat();
        Mat dst = new Mat();
        Mat fixSrc = new Mat();
        Utils.bitmapToMat(input, src);

        Utils.bitmapToMat(template, tpl);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.Canny(dst, dst, 200, 400, 3, false);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        Imgproc.findContours(dst, contours, hierachy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2BGR);
        int width = input.getWidth();
        int height = input.getHeight();
        Rect roiArea = null;
        for(int i=0; i<contours.size(); i++) {
            List<Point> points = contours.get(i).toList();
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if(rect.width < width && rect.width > (width / 2)) {
                if(rect.height <= (height / 4)) continue;
                roiArea = rect;
            }
        }
        // clip ROI Area
        Mat result = src.submat(roiArea);

        // fix size, in order to match template
        Size fixSize = new Size(547, 342);
        Imgproc.resize(result, fixSrc, fixSize);
        result = fixSrc;

        // detect location
        int result_cols =  result.cols() - tpl.cols() + 1;
        int result_rows = result.rows() - tpl.rows() + 1;
        Mat mr = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // template match
        Imgproc.matchTemplate(result, tpl, mr, Imgproc.TM_CCORR_NORMED);
        Core.normalize(mr, mr, 0, 1, Core.NORM_MINMAX, -1);
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(mr);
        Point maxLoc = minMaxLocResult.maxLoc;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types

        // find id number ROI
        Rect idNumberROI = new Rect((int)(maxLoc.x+tpl.cols()), (int)maxLoc.y, (int)(result.cols() - (maxLoc.x+tpl.cols())-40), tpl.rows()-10);
        Mat idNumberArea = result.submat(idNumberROI);

        // 返回对象
        Bitmap bmp = Bitmap.createBitmap(idNumberArea.cols(), idNumberArea.rows(), conf);
        Utils.matToBitmap(idNumberArea, bmp);

        // 释放内存
        idNumberArea.release();
        idNumberArea.release();
        result.release();
        fixSrc.release();
        src.release();
        dst.release();
        return bmp;
    }

    public static void deSkewText(Mat textImage, Mat dst) {
        // 二值化图像
        Mat gray = new Mat();
        Mat binary = new Mat();
        Imgproc.cvtColor(textImage, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, binary, 0, 255,Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        // 寻找文本区域最新外接矩形
        int w = binary.cols();
        int h = binary.rows();
        List<Point> points = new ArrayList<>();
        int p = 0;
        byte[] data = new byte[w*h];
        binary.get(0, 0, data);
        int index = 0;
        for(int row=0; row<h; row++) {
            for(int col=0; col<w; col++) {
                index = row*w + col;
                p = data[index]&0xff;
                if(p == 255) {
                    points.add(new Point(col, row));
                }
            }
        }
        RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(points.toArray(new Point[0])));
        double angle = box.angle;
        if (angle < -45.)
            angle += 90.;

        Point[] vertices = new Point[4];
        box.points(vertices);
        // de-skew 偏斜校正
        Mat rot_mat = Imgproc.getRotationMatrix2D(box.center, angle, 1);
        Imgproc.warpAffine(binary, dst, rot_mat, binary.size(), Imgproc.INTER_CUBIC);
        Core.bitwise_not(dst, dst);

        gray.release();
        binary.release();
        rot_mat.release();
    }
}
