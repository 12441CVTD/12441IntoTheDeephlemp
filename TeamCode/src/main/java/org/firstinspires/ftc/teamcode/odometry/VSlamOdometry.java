package org.firstinspires.ftc.teamcode.odometry;

import android.graphics.Point;
import android.util.Log;

//import com.arcrobotics.ftclib.geometry.Pose2d;
//import com.arcrobotics.ftclib.geometry.Rotation2d;
//import com.arcrobotics.ftclib.geometry.Transform2d;
//import com.arcrobotics.ftclib.geometry.Translation2d;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ReadWriteFile;
//import com.spartronics4915.lib.T265Camera;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.autonomous.AutoRoute;
import org.firstinspires.ftc.teamcode.autonomous.AutoStep;
import org.firstinspires.ftc.teamcode.bots.BotMoveRequest;

import java.io.File;

/**
 * Odometry class to keep track of a robot using V-SLAM Camera Module
 *
 * Camera: Intel® RealSense™ Tracking Camera T265
 * Product page: https://www.intelrealsense.com/tracking-camera-t265/
 *
 * Sample code can be found at
 * https://github.com/pietroglyph/FtcRobotController/tree/ftc265-example
 * file: TeamCode/src/main/java/org/firstinspires/ftc/teamcode/TestCameraOpMode.java
 *
 * Instructions on adding the required libraries can be found at
 * https://github.com/pietroglyph/ftc265
 */
public class VSlamOdometry implements IBaseOdometry {

    public static final int THREAD_INTERVAL = 20;
    static double INCH_2_METER = 0.0254;

    private HardwareMap hwMap;

    private double encoderMeasurementCovariance;
    private int sleepTime;

//    private T265Camera slamra;
    private boolean isRunning = true;

    private double currentX;    // in Inches
    private double currentY;    // in Inches
    private double currentOrientation; // in Degrees
    private double initialOrientation; // in Degrees


    private double prevX;    // in Inches
    private double prevY;    // in Inches
    private double prevAdjOrientation; // in Degrees 0-360

    private double originalX;    // in Inches
    private double originalY;    // in Inches

    private static VSlamOdometry theInstance;

    private boolean persistPosition = false;
    private boolean trackingInitialized = false;

    private static final String TAG = "VSlamOdometry";


    private VSlamOdometry(HardwareMap hwMap, int threadDelay) {
        init(hwMap, threadDelay, 0.8);
    }

    public static VSlamOdometry getInstance(HardwareMap hwMap) {
        if (theInstance == null) {
            theInstance = new VSlamOdometry(hwMap, THREAD_INTERVAL);
            theInstance.setInitPosition(0, 0, 0);
        }
        return theInstance;
    }

    public static VSlamOdometry getInstance(HardwareMap hwMap, int threadDelay) {
        if (theInstance == null) {
            theInstance = new VSlamOdometry(hwMap, threadDelay);
            theInstance.setInitPosition(0, 0, 0);
        }
        return theInstance;
    }

    public static VSlamOdometry getInstance(HardwareMap hwMap, int threadDelay, int startXInches, int startYInches, int startHeadingDegrees) {
        if (theInstance == null) {
            theInstance = new VSlamOdometry(hwMap, threadDelay);
            theInstance.setInitPosition(startXInches, startYInches, startHeadingDegrees);
        }
        return theInstance;
    }

    private void init(HardwareMap hwMap, int threadDelay, double encoderMeasurementCovariance){
        this.hwMap = hwMap;
        this.sleepTime = threadDelay;
        // Increase this value to trust encoder odometry less when fusing encoder measurements with VSLAM
        this.encoderMeasurementCovariance = encoderMeasurementCovariance;

        // This is the transformation between the center of the camera and the center of the robot
        // Set these three values to match the location/orientation of the camera with respect to the robot
        double offsetXInches = -5;
        double offsetYInches = -5.5;
        double offsetHDegrees = 0;
        // to change offsets, place robot at 0 degrees. Measure from the center of the camera from the center of the robot. That is the value.
        // place the robot and spin in place. The values should vary by a maximum on 2
//
//        Translation2d offsetTranslation = new Translation2d(offsetXInches * INCH_2_METER, offsetYInches * INCH_2_METER);
//        Rotation2d offsetRotation = Rotation2d.fromDegrees(offsetHDegrees);
//        final Transform2d cameraToRobot = new Transform2d(offsetTranslation, offsetRotation);
//        this.slamra = new T265Camera(cameraToRobot, encoderMeasurementCovariance, this.hwMap.appContext);
//        slamra.start();
    }

    @Override
    public void setInitPosition(int startXInches, int startYInches, int startHeadingDegrees)  {

        this.originalX = startXInches;
        this.originalY = startYInches;
        this.currentX = startXInches;
        this.currentY = startYInches;
        this.currentOrientation = startHeadingDegrees;
        setInitialOrientation(startHeadingDegrees);

        double startX = startXInches * INCH_2_METER;
        double startY = startYInches * INCH_2_METER;
//        Rotation2d startHeading = Rotation2d.fromDegrees(startHeadingDegrees);
//        Pose2d startingPose = new Pose2d(startX, startY, startHeading);
//
//        slamra.setPose(startingPose);
    }

    @Override
    public void stop() {
        isRunning = false;
        trackingInitialized = false;
    }

    @Override
    public double getCurrentX() {
        return currentX;
    }

    @Override
    public double getCurrentY() {
        return currentY;
    }

    @Override
    public double getPreviousX() {
        return prevX;
    }

    @Override
    public double getPreviousY() {
        return prevY;
    }

    @Override
    public double getPreviousAdjHeading() {
        return prevAdjOrientation;
    }

    @Override
    public void setPreviousX(double x) {
        prevX = x;
    }

    @Override
    public void setPreviousY(double y) {
        prevY = y;
    }

    @Override
    public void setPreviousAdjHeading(double heading) {
        prevAdjOrientation = heading;
    }

//    protected double adjustXCoordinate(double rawX){
//
//        if (initialOrientation < 45) {
//            double delta = Math.abs(originalX - rawX);
//            if (rawX > originalX) {
//                rawX = originalX - delta;
//            } else {
//                rawX = originalX + delta;
//            }
//        }
//
//        return rawX;
//    }


    @Override
    public void reverseHorEncoder() {
        // does not apply
    }

    @Override
    public void setPersistPosition(boolean persistPosition) {
        this.persistPosition = persistPosition;
    }

    @Override
    public void init(Point startPos, double initialOrientation) {
        try {
            setInitPosition(startPos.x, startPos.y, (int) initialOrientation);
        }
        catch (Exception ex){
            Log.e(TAG, "Failed to start VSLAM camera", ex);
        }
    }

    @Override
    public double getInitialOrientation() {
        return this.initialOrientation;
    }

    @Override
    public double getOrientation() {
        return this.currentOrientation % 360;
    }

    @Override
    public double getAdjustedCurrentHeading() {
        double currentHead = this.getOrientation();

        boolean clockwise = currentHead >= 0;
        if (!clockwise){
            currentHead = 360 + currentHead;
        }
        return currentHead;
    }

    @Override
    public int getThreadSleepTime() {
        return this.sleepTime;
    }

    @Override
    public void setTarget(BotMoveRequest target) {

    }

    @Override
    public double getRealSpeedLeft() {
        return 0;
    }

    @Override
    public double getRealSpeedRight() {
        return 0;
    }

    @Override
    public boolean isLeftLong() {
        return false;
    }

    @Override
    public void run() {
        try {
            isRunning = true;
            while (isRunning) {
                updatePosition();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, "Error starting the camera", ex);
        }
    }

    private void updatePosition(){
        try {
//            T265Camera.CameraUpdate up = slamra.getLastReceivedCameraUpdate();
//
//            if (up != null) {
//                // ensure a value between 0 and 360
//                this.currentOrientation = up.pose.getRotation().getDegrees();
//
//                // get current position in inches
//                this.currentX = up.pose.getX() / INCH_2_METER;
//                this.currentY = up.pose.getY() / INCH_2_METER;
//
//                if (!trackingInitialized && Math.abs((int)this.currentX) > 0 && Math.abs((int)this.currentY) > 0) {
//                    trackingInitialized = true;
//                }
//
//                if (persistPosition) {
//                    saveLastPosition();
//                }
//            }
        }
        catch (Exception ex){
            Log.e(TAG, "Error in update position", ex);
        }
    }

    public void saveLastPosition(){
        BotPosition lastPos = new BotPosition();
        lastPos.setPosX((int)this.currentX);
        lastPos.setPosY((int)this.currentY);
        lastPos.setHeading(this.getOrientation());
        File file = AppUtil.getInstance().getSettingsFile(BotPosition.BOT_LAST_POSITION);
        ReadWriteFile.writeFile(file, lastPos.serialize());
    }

    public BotPosition getLastConfig() {
        BotPosition lastPos = null;

        File posFile = AppUtil.getInstance().getSettingsFile(BotPosition.BOT_LAST_POSITION);
        if (posFile.exists()) {
            String data = ReadWriteFile.readFile(posFile);
            lastPos = BotPosition.deserialize(data);
        }

        return lastPos;
    }

    public void setInitialOrientation(double initialOrientation) {
        this.initialOrientation = initialOrientation;
    }

    public boolean isTrackingInitialized() {
        return trackingInitialized;
    }

}

