package org.firstinspires.ftc.teamcode;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;
import static org.firstinspires.ftc.teamcode.Bot.BotState.STORAGE_NOT_FULL;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.autonomous.AprilTagsDetection;
import org.firstinspires.ftc.teamcode.autonomous.AprilTagsPipeline;
import org.firstinspires.ftc.teamcode.subsystems.Box;
import org.firstinspires.ftc.teamcode.subsystems.Drone;
import org.firstinspires.ftc.teamcode.subsystems.Fourbar;
import org.firstinspires.ftc.teamcode.subsystems.Noodles;
import org.firstinspires.ftc.teamcode.subsystems.Slides;
import org.openftc.easyopencv.OpenCvCamera;


public class Bot {

    public enum BotState {
        INTAKE, // surgical tubing ready to pick up pixel
        STORAGE_FULL, // 2 pixels in storage
        STORAGE_NOT_FULL, //1 or 0 pixels in storage
        OUTTAKE, //is in outtake position
    }

    public OpMode opMode;
    public BotState currentState = STORAGE_NOT_FULL;
    public static Bot instance;
    public OpenCvCamera camera;
    public AprilTagsPipeline aprilTagsPipeline;

    public static org.firstinspires.ftc.teamcode.autonomous.AprilTagsDetection detections;

    public Slides slides;
    public Noodles noodles;
    public Drone drone;
    public Fourbar fourbar;
    public Box box;

    public static DistanceSensor distanceSensor;

    private final DcMotorEx FL, FR, BL, BR, susMotor, slidesMotor;
    private final Servo droneServo, fourBarServo;



    public BNO055IMU imu;
    public boolean fieldCentricRunMode = false;

    private double imuOffset = 0;
    private double distanceFromBackdrop;
    private final double optimalDistanceFromBackdrop = 10;
    //arbitrary number for now

    public static Bot getInstance() {
        if (instance == null) {
            throw new IllegalStateException("tried to getInstance of Bot when uninitialized");
        }
        return instance;
    }

    public static Bot getInstance(OpMode opMode) {
        if (instance == null) {
            return instance = new Bot(opMode);
        }
        instance.opMode = opMode;
        return instance;
    }

    private Bot(OpMode opMode) {
        this.opMode = opMode;
        enableAutoBulkRead();
        //what is this
        try {
            this.initializeImus();
            fieldCentricRunMode = false;
        } catch (Exception e) {
            imu = null;
            fieldCentricRunMode = false;

        }

        FL = opMode.hardwareMap.get(DcMotorEx.class, "FL");
        FR = opMode.hardwareMap.get(DcMotorEx.class, "FR");
        BL = opMode.hardwareMap.get(DcMotorEx.class, "BL");
        BR = opMode.hardwareMap.get(DcMotorEx.class, "BR");
        droneServo= opMode.hardwareMap.get(Servo.class, "droneServo");;
        fourBarServo= opMode.hardwareMap.get(Servo.class, "fourBarServo");
        susMotor = opMode.hardwareMap.get(DcMotorEx.class, "susMotor");
        slidesMotor = opMode.hardwareMap.get(DcMotorEx.class, "slidesMotor");
        distanceSensor = opMode.hardwareMap.get(DistanceSensor.class, "distanceSensor");


        FL.setMode(RUN_USING_ENCODER);
        FR.setMode(RUN_USING_ENCODER);
        BL.setMode(RUN_USING_ENCODER);
        BR.setMode(RUN_USING_ENCODER);


        slides = new Slides(opMode);
        noodles = new Noodles(opMode);
        drone= new Drone(opMode);
        fourbar = new Fourbar(opMode);
        box= new Box(opMode);


    }



    public void prepForOuttake() {
        currentState = BotState.STORAGE_FULL;
        resetOuttake();
    }

    // must be combined with bot.slide.run___() in MainTeleOp
    public void outtake(int stage, boolean pixelTwo) {
        currentState = BotState.OUTTAKE;
        aprilTagTuning();
        slides.runTo(stage);
        fourbar.outtake();
        box.depositFirstPixel();
        if(pixelTwo){
            box.depositSecondPixel();
            resetOuttake();
        }
    }

    public void outtake(boolean pixelTwo, int stage){
        currentState = BotState.OUTTAKE;
        slides.runTo(stage);
        fourbar.outtake();
        box.depositFirstPixel();
        if(pixelTwo){
            box.depositSecondPixel();
            resetOuttake();
        }
    }


    public void initializeImus() {
        imu = opMode.hardwareMap.get(BNO055IMU.class, "imu");
        final BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;

        imu.initialize(parameters);
        resetIMU();
    }

    public void storageSlides(){
        slides.runTo(1);
    }

    public void resetOuttake(){
        box.resetBox();
        storageSlides();
        fourbar.storage();
    }


    public void fixMotors(double velocity) {
        FL.setDirection(DcMotorEx.Direction.REVERSE); //invert
        FR.setVelocity(velocity);
        BL.setDirection(DcMotorEx.Direction.REVERSE); // invert
        BR.setVelocity(velocity);

        FL.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        FR.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        BL.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        BR.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    }

    public void driveRobotCentric(double strafeSpeed, double forwardBackSpeed, double turnSpeed) {
        double[] speeds = {
                forwardBackSpeed - strafeSpeed - turnSpeed,
                forwardBackSpeed + strafeSpeed + turnSpeed,
                forwardBackSpeed + strafeSpeed - turnSpeed,
                forwardBackSpeed - strafeSpeed + turnSpeed
        };
        double maxSpeed = 0;
        for (int i = 0; i < 4; i++) {
            maxSpeed = Math.max(maxSpeed, speeds[i]);
        }
        if (maxSpeed > 1) {
            for (int i = 0; i < 4; i++) {
                speeds[i] /= maxSpeed;
            }
        }
        FL.setPower(speeds[0]);
        FR.setPower(speeds[1]);
        BL.setPower(speeds[2]);
        BR.setPower(speeds[3]);
    }

    //cope no one uses field centric
    public void driveFieldCentric(double strafeSpeed, double forwardBackSpeed, double turnSpeed, double heading) {
        double magnitude = Math.sqrt(strafeSpeed * strafeSpeed + forwardBackSpeed * forwardBackSpeed);
        double theta = (Math.atan2(forwardBackSpeed, strafeSpeed) - heading) % (2 * Math.PI);
        double[] speeds = {
                magnitude * Math.sin(theta + Math.PI / 4) + turnSpeed,
                magnitude * Math.sin(theta - Math.PI / 4) - turnSpeed,
                magnitude * Math.sin(theta - Math.PI / 4) + turnSpeed,
                magnitude * Math.sin(theta + Math.PI / 4) - turnSpeed
        };

        double maxSpeed = 0;

        for (int i = 0; i < 4; i++) {
            maxSpeed = Math.max(maxSpeed, speeds[i]);
        }

        if (maxSpeed > 1) {
            for (int i = 0; i < 4; i++) {
                speeds[i] /= maxSpeed;
            }
        }

        //        for (int i = 0; i < 4; i++) {
        //            driveTrainMotors[i].set(speeds[i]);
        //        }
        // manually invert the left side

        FL.setPower(speeds[0]);
        FR.setPower(speeds[1]);
        BL.setPower(speeds[2]);
        BR.setPower(speeds[3]);
    }

    private void enableAutoBulkRead() {
        for (LynxModule mod : opMode.hardwareMap.getAll(LynxModule.class)) {
            mod.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
    }

    public void intake(double power){
        currentState = BotState.INTAKE;
        noodles.intake(power);
        if(power<=0.1){
            noodles.stop();
        }
    }

    public void outtakeBox(){
        currentState = BotState.OUTTAKE;
        if(box.getNumPixelsDeposited() == 0){
            box.depositFirstPixel();
        }else if(box.getNumPixelsDeposited()==1){
            box.depositSecondPixel();
            box.resetBox();
        }
    }


    public void outtakeSlides(double target){
        currentState = BotState.OUTTAKE;
        slides.runTo(target);
    }

    public void outtakeFourbar(double input){
        if(!fourbar.getIsOuttakePos()){
            fourbar.runManualOuttake(input);
        }
    }



    public void resetEncoder() {
        FL.setMode(STOP_AND_RESET_ENCODER);
        FR.setMode(STOP_AND_RESET_ENCODER);
        BR.setMode(STOP_AND_RESET_ENCODER);
        BL.setMode(STOP_AND_RESET_ENCODER);
        //slides.resetEncoder(); code this in slides subsystems
        //reset encoder in slides

    }

    public void setImuOffset(double offset) {
        imuOffset += offset;
    }

    public void resetIMU() {
        imuOffset += getIMU();
    }

    public double getIMU() {
        double angle = (imu.getAngularOrientation().toAngleUnit(AngleUnit.DEGREES).firstAngle - imuOffset) % 360;
        if (angle > 180) {
            angle = angle - 360;
        }
        return angle;
    }

    public void resetProfiler() {
        //slides.resetProfiler(); code in slides subsystem
        //figure this out

    }
    public void turn(double power){
        if(power>0) {
            //turn right
            FL.setPower(power);
        }
        if(power<0){
            //turn left
            FR.setPower(-power);
        }
    }
    public void strafeRight(){
        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BR.setDirection(DcMotorSimple.Direction.REVERSE);
        FL.setPower(0.1);
        FR.setPower(0.1);
        BR.setPower(0.1);
        BL.setPower(0.1);
        FL.setDirection(DcMotorSimple.Direction.FORWARD);
        BR.setDirection(DcMotorSimple.Direction.FORWARD);
        FL.setPower(0);
        FR.setPower(0);
        BR.setPower(0);
        BL.setPower(0);
    }
    public void strafeLeft(){
        BL.setDirection(DcMotorSimple.Direction.REVERSE);
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
        FL.setPower(0.1);
        FR.setPower(0.1);
        BR.setPower(0.1);
        BL.setPower(0.1);
        BL.setDirection(DcMotorSimple.Direction.FORWARD);
        FR.setDirection(DcMotorSimple.Direction.FORWARD);
        FL.setPower(0);
        FR.setPower(0);
        BR.setPower(0);
        BL.setPower(0);
    }
    public void back(){
        BL.setDirection(DcMotorSimple.Direction.REVERSE);
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
        FL.setDirection(DcMotorSimple.Direction.REVERSE);
        BR.setDirection(DcMotorSimple.Direction.REVERSE);
        FL.setPower(0.1);
        FR.setPower(0.1);
        BR.setPower(0.1);
        BL.setPower(0.1);
        BL.setDirection(DcMotorSimple.Direction.FORWARD);
        FR.setDirection(DcMotorSimple.Direction.FORWARD);
        FL.setDirection(DcMotorSimple.Direction.FORWARD);
        BR.setDirection(DcMotorSimple.Direction.FORWARD);
        FL.setPower(0);
        FR.setPower(0);
        BR.setPower(0);
        BL.setPower(0);

    }
    public void forward(){
        FL.setPower(0.1);
        FR.setPower(0.1);
        BR.setPower(0.1);
        BL.setPower(0.1);
    }

    public void distanceTuning(DistanceSensor sensor){
        double diffy = this.distanceFromBackdrop - optimalDistanceFromBackdrop;
        boolean inRange = Math.abs(diffy) <= 5;
        if(inRange){
            return;
        }
        while(!inRange){
            if(diffy<0){
                back();
            }else{
                forward();
            }
            distanceFromBackdrop = sensor.getDistance(DistanceUnit.CM);
            diffy = distanceFromBackdrop - optimalDistanceFromBackdrop;
            inRange = Math.abs(diffy) <= 5;
            distanceTuning(sensor);
        }
    }

    public void aprilTagTuning(){
        AprilTagsDetection.detectTag();
        distanceFromBackdrop = detections.calcDistToTag();
        double diffy = this.distanceFromBackdrop - optimalDistanceFromBackdrop;
        boolean inRange = Math.abs(diffy) <= 5;
        while(!inRange){
            if(diffy<0){
                back();
            }else{
                forward();
            }
            distanceFromBackdrop = detections.calcDistToTag();
            diffy = distanceFromBackdrop - optimalDistanceFromBackdrop;
            inRange = Math.abs(diffy) <= 5;
        }
    }
}