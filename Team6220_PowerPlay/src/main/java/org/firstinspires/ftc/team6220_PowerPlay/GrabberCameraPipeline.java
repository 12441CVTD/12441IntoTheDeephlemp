package org.firstinspires.ftc.team6220_PowerPlay;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

public class GrabberCameraPipeline extends OpenCvPipeline {
    //Fields and stuff
    public double xPosition = Constants.CAMERA_CENTER_X;
    public double yPosition = Constants.CAMERA_CENTER_Y;
    public boolean detected = false;

    List<MatOfPoint> contours = new ArrayList<>();
    Mat hierarchy = new Mat();
    Mat mat = new Mat();

    @Override
    public Mat processFrame(Mat input) {
        // change color-space to hsv for ease of singling out colors
        Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV);
        // blur frame to remove imperfections
        Imgproc.GaussianBlur(mat, mat, Constants.BLUR_SIZE, 0);
        // masks blurred frame within black ranges to find junction top
        Core.inRange(mat, Constants.LOWER_BLACK, Constants.UPPER_BLACK, mat);
        // single out all areas of black
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // determine largest area of black
        if (contours.size() > 0) {
            detected = true;
            double maxVal = 0.0;
            int maxValIdx = 0;

            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
                double contourArea = Imgproc.contourArea(contours.get(contourIdx));
                if (maxVal < contourArea) {
                    maxVal = contourArea;
                    maxValIdx = contourIdx;
                }
            }
            // draws a box to visualize largest blob
            Rect boundingRect = Imgproc.boundingRect(contours.get(maxValIdx));
            // get center coords of blob/box
            xPosition = boundingRect.x + (boundingRect.width * 0.5);
            yPosition = boundingRect.y + (boundingRect.height * 0.5);
            // calculate distance of the center of the box from the center of the camera
            double distanceFromCenter = Math.sqrt(Math.pow(Math.abs((xPosition - Constants.CAMERA_CENTER_X)), 2) + Math.pow(Math.abs((yPosition - Constants.CAMERA_CENTER_Y)), 2));
            // determine if the detected area is close enough to the center
            // this is done to avoid detecting the wheels which are also black
            //TODO: add a check to determine that the box is relatively square, to prevent detecting other robots or black objects on the field other than the junctions
            if (distanceFromCenter < Constants.DISTANCE_FROM_CENTER) {
                Moments moments = Imgproc.moments(contours.get(maxValIdx), false);

                Imgproc.rectangle(input, boundingRect, new Scalar(0, 255, 0), 10);

                if (moments.get_m00() > 0) {
                    xPosition = boundingRect.x + (boundingRect.width * 0.5);
                    yPosition = boundingRect.y + (boundingRect.height * 0.5);
                }
            }
            // if not detected set back to defaults
        } else {
            detected = false;
            xPosition = Constants.CAMERA_CENTER_X;
            yPosition = Constants.CAMERA_CENTER_Y;
        }
        // clears the contours to prevent memory leak
        contours.clear();
        return input;
    }
}
