package com.example.eda.distancemeasure;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.calib3d.Calib3d.RANSAC;
import static org.opencv.calib3d.Calib3d.calibrateCamera;
import static org.opencv.calib3d.Calib3d.drawChessboardCorners;
import static org.opencv.calib3d.Calib3d.findEssentialMat;
import static org.opencv.calib3d.Calib3d.getOptimalNewCameraMatrix;
import static org.opencv.calib3d.Calib3d.recoverPose;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.features2d.Features2d.drawMatches;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class CalibrationMode extends Fragment {

    static {
        System.loadLibrary("opencv_java3");
    }

    private Bitmap bitmap;

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        ImageView imageview = (ImageView)view.findViewById(R.id.calibrateView);
        imageview.setImageBitmap(onImage(bitmap));
    }


    public Bitmap onImage(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        Mat gray = Mat.zeros(mat.width(),mat.height(),CV_8U);

        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Core.normalize(gray, gray, 0, 255, Core.NORM_MINMAX);

        Mat outputFrame = onCalibrate(bitmap);

        Bitmap mbitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(outputFrame,mbitmap);
        return mbitmap;
    }

    public Mat onEssential(Mat right,Mat left) {

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        MatOfKeyPoint key1 = new MatOfKeyPoint();
        MatOfKeyPoint key2 = new MatOfKeyPoint();
        Scalar color = new Scalar(0,0,255);
        Mat description1 = new Mat(right.rows(),right.cols(),right.type());
        Mat description2 = new Mat(left.rows(),left.cols(),left.type());

        detector.detect(right,key1);
        detector.detect(left,key2);
        extractor.compute(right,key1,description1);
        extractor.compute(left,key2,description2);

        MatOfDMatch matche = new MatOfDMatch();
        MatOfDMatch dmatche12 = new MatOfDMatch();
        MatOfDMatch dmatche21 = new MatOfDMatch();
        List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        List<DMatch> dmatch = new ArrayList<DMatch>();

        matcher.match(description1,description2,dmatche12);
        matcher.match(description2,description1,dmatche21);
        List<DMatch> ldm_srcToBase = dmatche12.toList();
        List<DMatch> ldm_baseToSrc = dmatche21.toList();

        for(int i=0; i<ldm_srcToBase.size(); i++) {

            DMatch forward = ldm_srcToBase.get(i);
            DMatch backward = ldm_baseToSrc.get(forward.trainIdx);

            if (backward.trainIdx == forward.queryIdx) {
                dmatch.add(forward);
            }
        }
        matche.fromList(dmatch);

        Mat output = new Mat(right.rows()*2,right.cols()*2,right.type());
        drawMatches(right, key1, left, key2, matche, output);


        List<KeyPoint> keypoints1_l = key1.toList();
        List<KeyPoint> keypoints2_l = key2.toList();
        List<Point> plist1 = new ArrayList<Point>();
        List<Point> plist2 = new ArrayList<Point>();

        MatOfPoint2f pP1 = new MatOfPoint2f();
        MatOfPoint2f pP2 = new MatOfPoint2f();
        Point p = new Point();
        Point pP = new Point(0,0);
        Mat ip =  new Mat(3,1, CV_64FC1);
        Mat non = Mat.zeros(3, 1, CV_64FC1);

        if(dmatch.size()>100) {
            for (int i = 0; i < dmatch.size(); i++) {
                DMatch forward = dmatch.get(i);

                p.x = keypoints1_l.get(forward.queryIdx).pt.x;
                p.y = keypoints1_l.get(forward.queryIdx).pt.y;
                plist1.add(p);

                p.x = keypoints2_l.get(forward.trainIdx).pt.x;
                p.y = keypoints2_l.get(forward.trainIdx).pt.y;
                plist2.add(p);

            }
            pP1.fromList(plist1);
            pP2.fromList(plist2);

            Mat mask = new Mat();
            Mat essentialMat = findEssentialMat(pP1, pP2, 1.0, pP, RANSAC, 0.9999, 0.003, mask);
            Mat rotation = new Mat();
            Mat translation = new Mat();
            recoverPose(essentialMat, pP1, pP2, rotation, translation);
        }
        return output;
    }

    public Mat onCalibrate(Bitmap inputFrame) {

        int horizonalCrossCount = 7;
        int verticalCrossCount = 10;

        List<Mat> image_point = new ArrayList<Mat>();
        MatOfPoint3f objects = new MatOfPoint3f();
        List<Mat> objcts_point = new ArrayList<Mat>();
        for (int j = 0; j < horizonalCrossCount * verticalCrossCount; j++) {
            objects.push_back(new MatOfPoint3f(new Point3(j / horizonalCrossCount, j % verticalCrossCount, 0.0f)));
        }

        MatOfPoint2f corners = new MatOfPoint2f();
        TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, 30, 0.1);
        Size sizea = new Size(horizonalCrossCount, verticalCrossCount);
        Size sizeb = new Size(11, 11);
        Size sizec = new Size(-1, -1);
        Mat savedImage = new Mat();
        Mat distcofes = Mat.zeros(4, 1, CV_32FC1);
        Mat cameraMatrix = Mat.zeros(3, 3, CV_32FC1);
        List<Mat> t = new ArrayList<>();
        List<Mat> r = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
            Mat mcheck = new Mat();
            Utils.bitmapToMat(inputFrame, mcheck);
            if (i != 1)Imgproc.resize(mcheck, mcheck, savedImage.size());
            Mat gry = Mat.zeros(mcheck.width(), mcheck.height(), CV_8U);
            Imgproc.cvtColor(mcheck, gry, COLOR_BGR2GRAY);
            if (i == 1) mcheck.copyTo(savedImage);

            boolean found = Calib3d.findChessboardCorners(gry, sizea, corners,
                    Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);

            if (found == true) {
                Imgproc.cornerSubPix(gry, corners, sizeb, sizec, criteria);
                image_point.add(corners);
                objcts_point.add(objects);
                drawChessboardCorners(gry, sizea, corners, found);
                System.out.println("success");
            } else {
                System.out.println("not found.");
            }
        }
        float[][] dataA = {{320, 0, 160}, {0, 320, 120}, {0, 0, 1} };
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cameraMatrix.put(i,j,dataA[i][j]);
            }
        }
        calibrateCamera(objcts_point, image_point, savedImage.size(), cameraMatrix, distcofes, r, t);
        Mat undisort = new Mat();
        Mat roi = getOptimalNewCameraMatrix(cameraMatrix, distcofes, savedImage.size(), 1);
       Imgproc.undistort(savedImage, undisort, roi, distcofes, roi);

        return undisort;
    }

}
