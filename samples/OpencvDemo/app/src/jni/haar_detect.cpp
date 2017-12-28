#include<jni.h>
#include<opencv2/opencv.hpp>
#include <iostream>
#include<vector>
#include <android/log.h>

#define  LOG_TAG    "MYHAARDETECTION"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace std;

extern"C" {

    int get_block_sum(Mat &sum, int x1, int y1, int x2, int y2, int i) {
        int tl = sum.ptr<int>(y1)[x1 * 3 + i]; //at<Vec3i>(y1, x1)[i];
        int tr = sum.ptr<int>(y2)[x1 * 3 + i];// at<Vec3i>(y2, x1)[i];
        int bl = sum.ptr<int>(y1)[x2 * 3 + i];//at<Vec3i>(y1, x2)[i];
        int br = sum.ptr<int>(y2)[x2 * 3 + i];// at<Vec3i>(y2, x2)[i];
        int s = (br - bl - tr + tl);
        return s;
    }

    float get_block_sqrt_sum(Mat &sum, int x1, int y1, int x2, int y2, int i) {
        float tl = sum.ptr<float>(y1)[x1 * 3 + i];// .at<Vec3f>(y1, x1)[i];
        float tr = sum.ptr<float>(y2)[x1 * 3 + i];// at<Vec3f>(y2, x1)[i];
        float bl = sum.ptr<float>(y1)[x2 * 3 + i];// <Vec3f>(y1, x2)[i];
        float br = sum.ptr<float>(y2)[x2 * 3 + i];// at<Vec3f>(y2, x2)[i];
        float var = (br - bl - tr + tl);
        return var;
    }

    CascadeClassifier face_detector;
    JNIEXPORT void JNICALL Java_com_book_chapter_seven_DisplayModeActivity_initLoad(JNIEnv* env, jobject, jstring haarfilePath)
    {
        const char *nativeString = env->GetStringUTFChars(haarfilePath, 0);
        face_detector.load(nativeString);
        env->ReleaseStringUTFChars(haarfilePath, nativeString);
        LOGD( "Method Description: %s", "loaded haar files..." );
    }
    JNIEXPORT void JNICALL Java_com_book_chapter_seven_DisplayModeActivity_faceDetection(JNIEnv*, jobject, jlong addrRgba)
    {
        int flag = 1000;
        Mat& mRgb = *(Mat*)addrRgba;
        Mat gray;
        cvtColor(mRgb, gray, COLOR_BGR2GRAY);
        vector<Rect> faces;
        //LOGD( "This is a number from JNI: %d", flag*2);
        face_detector.detectMultiScale(gray, faces, 1.1, 1, 0, Size(50, 50), Size(300, 300));
        //LOGD( "This is a number from JNI: %d", flag*3);
        if(faces.empty()) return;
        for (int i = 0; i < faces.size(); i++) {
            rectangle(mRgb, faces[i], Scalar(255, 0, 0), 2, 8, 0);
            LOGD( "Face Detection : %s", "Found Face");
        }

    }

    JNIEXPORT void JNICALL Java_com_book_chapter_nine_BeautyFaceActivity_beautySkinFilter(JNIEnv*, jobject, jlong addrsrc, jlong addrdst, jfloat sigma, jboolean blur)
    {
        bool flag = (bool)blur;
        Mat& src = *(Mat*)addrsrc;
        Mat& dst = *(Mat*)addrdst;
        LOGD( "Face Beauty : %s", "Call In");
        // 计算积分图
        Mat sum, sqrsum, ycrcb;
        cvtColor(src, ycrcb, COLOR_BGR2YCrCb);
        integral(src, sum, sqrsum, CV_32S, CV_32F);
        LOGD( "Face Beauty : %s", "积分图计算");
        int w = src.cols;
        int h = src.rows;

        int x2 = 0, y2 = 0;
        int x1 = 0, y1 = 0;
        int ksize = 15;
        int radius = ksize / 2;
        int ch = src.channels();
        int cx = 0, cy = 0;
        float sigma2 = sigma*sigma;
        Mat mask = Mat::zeros(src.size(), CV_8UC1);
        int bgr[] = { 0, 0, 0 };
        for (int row = 0; row < h + radius; row++) {
            y2 = (row + 1)>h ? h : (row + 1);
            y1 = (row - ksize) < 0 ? 0 : (row - ksize);
            for (int col = 0; col < w + radius; col++) {
                x2 = (col + 1)>w ? w : (col + 1);
                x1 = (col - ksize) < 0 ? 0 : (col - ksize);
                cx = (col - radius) < 0 ? 0 : col - radius;
                cy = (row - radius) < 0 ? 0 : row - radius;
                int num = (x2 - x1)*(y2 - y1);
                for (int i = 0; i < ch; i++) {
                    int s = get_block_sum(sum, x1, y1, x2, y2, i);
                    float var = get_block_sqrt_sum(sqrsum, x1, y1, x2, y2, i);

                    // 计算系数K
                    float dr = (var - (s*s) / num) / num;
                    float mean = s / num;
                    float kr = dr / (dr + sigma2);

                    // 得到滤波后的像素值
                    int r = src.ptr<uchar>(cy)[cx * 3 + i];// at<Vec3b>(cy, cx)[i];
                    bgr[i] = ycrcb.ptr<uchar>(cy)[cx*3+i];
                    r = (int)((1 - kr)*mean + kr*r);
                    dst.ptr<uchar>(cy)[cx * 3 + i] = saturate_cast<uchar>(r);
                }
                if ((bgr[0] > 80) && (85 < bgr[2] && bgr[2] < 135) && (135 < bgr[1] && bgr[1] < 180)) {
                    mask.at<uchar>(cy, cx) = 255;
                }
            }
        }
        sum.release();
        ycrcb.release();
        sqrsum.release();
        LOGD( "Face Beauty : %s", "局部均方差滤波");

        Mat blur_mask, blur_mask_f;

        // 高斯模糊
        GaussianBlur(mask, blur_mask, Size(3, 3), 0.0);
        blur_mask.convertTo(blur_mask_f, CV_32F);
        normalize(blur_mask_f, blur_mask_f, 1.0, 0, NORM_MINMAX);

        // 高斯权重混合
        Mat clone = dst.clone();
        for (int row = 0; row<h; row++) {
            uchar* srcRow = src.ptr<uchar>(row);
            uchar* dstRow = dst.ptr<uchar>(row);
            uchar* cloneRow = clone.ptr<uchar>(row);
            float* mask_row = blur_mask_f.ptr<float>(row);
            for (int col = 0; col<w; col++) {
                int b1 = *srcRow++;
                int g1 = *srcRow++;
                int r1 = *srcRow++;

                int b2 = *cloneRow++;
                int g2 = *cloneRow++;
                int r2 = *cloneRow++;

                float w2 = *mask_row++;
                float w1 = 1.0f - w2;

                b2 = (int)(b2*w2 + w1*b1);
                g2 = (int)(g2*w2 + w1*g1);
                r2 = (int)(r2*w2 + w1*r1);

                *dstRow++ = saturate_cast<uchar>(b2);
                *dstRow++ = saturate_cast<uchar>(g2);
                *dstRow++ = saturate_cast<uchar>(r2);
            }
        }
        clone.release();
        blur_mask.release();
        blur_mask_f.release();
        LOGD( "Face Beauty : %s", "权重混合");

        // 边缘提升
        Canny(src, mask, 150, 300, 3, true);
        bitwise_and(src, src, dst, mask);

        // 亮度提升
        add(dst, Scalar(10, 10, 10), dst);
        if(flag){
            GaussianBlur(dst, dst, Size(3, 3), 0);
        }
        LOGD( "Face Beauty : %s", "End Call");
    }
}

