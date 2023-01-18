package org.firstinspires.ftc.team6220_PowerPlay;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

public class RobotCameraPipeline extends OpenCvPipeline {
    public double xPosition = Constants.CAMERA_CENTER_X;
    public double yPosition = Constants.CAMERA_CENTER_Y;
    public double width = 0.0;

    private Scalar lowerRange;
    private Scalar upperRange;

    List<MatOfPoint> contours = new ArrayList<>();
    Mat hierarchy = new Mat();
    Mat mat = new Mat();

    // method used to set the ranges for the pipeline
    public void setRanges(Scalar lowerRange, Scalar upperRange) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
    }

    @Override
    public Mat processFrame(Mat input) {
        // transform the RGB frame into a HSV frame
        Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV);

        // blur the HSV frame
        Imgproc.GaussianBlur(mat, mat, Constants.BLUR_SIZE, 0);

        // mask the blurred frame
        Core.inRange(mat, lowerRange, upperRange, mat);

        // find the contours in the masked frame
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // find the largest contour if there is one
        if (contours.size() > 0) {
            double maxVal = 0.0;
            int maxValIdx = 0;

            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
                double contourArea = Imgproc.contourArea(contours.get(contourIdx));

                if (maxVal < contourArea) {
                    maxVal = contourArea;
                    maxValIdx = contourIdx;
                }
            }

            // get the bounding rectangle around the largest contour
            Rect boundingRect = Imgproc.boundingRect(contours.get(maxValIdx));

            // get moments
            Moments moments = Imgproc.moments(contours.get(maxValIdx), false);

            // draw the bounding rectangle on the frame
            Imgproc.rectangle(input, boundingRect, new Scalar(0, 255, 0), 10);

            if (moments.get_m00() > 0) {
                xPosition = boundingRect.x + (boundingRect.width * 0.5);
                yPosition = boundingRect.y + (boundingRect.height * 0.5);
                width = boundingRect.width;
            }
        } else {
            width = 0.0;
            xPosition = Constants.CAMERA_CENTER_X;
            yPosition = Constants.CAMERA_CENTER_Y;
        }

        contours.clear();
        return input;
    }
}
