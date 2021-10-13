package org.firstinspires.ftc.teamcode.src;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.teamcode.robotAttachments.CarouselSpinner;
import org.firstinspires.ftc.teamcode.robotAttachments.Grabber;
import org.firstinspires.ftc.teamcode.robotAttachments.LinearSlide;


//@Disabled

@TeleOp(name = "2022 Drive Program")
public class JavaDriveProgram extends LinearOpMode {

    private BNO055IMU imu;

    private DcMotor back_right;
    private DcMotor back_left;
    private DcMotor front_right;
    private DcMotor front_left;

    private Grabber grabber;
    private LinearSlide slide;
    private CarouselSpinner spinner;

    double Yaw_value;

    boolean grabberIsOpen;
    ElapsedTime buttonATimeEllapsed;


    /**
     * This function is executed when this Op Mode is selected from the Driver Station.
     */
    //Override
    public void runOpMode() throws InterruptedException {
        ElapsedTime time = new ElapsedTime();
        double DrivePowerMult = 1;

        boolean xDepressed = true;
        boolean yDepressed = true;
        boolean aDepressed = true;
        boolean bDepressed = true;

        /**
         * Drive-train, odometry and IMU initialization
         */
        front_right = hardwareMap.dcMotor.get("back_left");
        front_left = hardwareMap.dcMotor.get("back_right");
        back_right = hardwareMap.dcMotor.get("front_left");
        back_left = hardwareMap.dcMotor.get("front_right");

        front_right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        front_left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        back_left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        back_right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        front_right.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        front_left.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        back_right.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        back_left.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        front_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        front_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        back_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        back_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        back_left.setDirection(DcMotorSimple.Direction.REVERSE);
        front_left.setDirection(DcMotorSimple.Direction.REVERSE);


        //Front Left and Back Left may need to be changed in the drive functions

        //Init IMU
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu.initialize(parameters);


        grabber = new Grabber(hardwareMap,"freight grabber");
        grabber.open();
        grabberIsOpen = true;

        slide = new LinearSlide(hardwareMap,"linear slide arm");

        spinner = new CarouselSpinner(hardwareMap,"carousel wheel");


        telemetry.addData("Initialization Status", "Initialized");
        telemetry.update();
        buttonATimeEllapsed = new ElapsedTime();
        buttonATimeEllapsed.reset();
        waitForStart();
        time.reset();

        if (opModeIsActive()) {
            // Put run blocks here.
            xDepressed = true;
            // variables for power of robot's functions
            time.reset();

            while (opModeIsActive() && !isStopRequested()) {
                // The Y axis of a joystick ranges from -1 in its topmost position
                // to +1 in its bottom most position. We negate this value so that
                // the topmost position corresponds to maximum forward power.
                back_left.setPower(DrivePowerMult * ((gamepad1.left_stick_y - gamepad1.left_stick_x) + gamepad1.right_stick_x));
                front_left.setPower(DrivePowerMult * ((gamepad1.left_stick_y + gamepad1.left_stick_x) + gamepad1.right_stick_x));
                back_right.setPower(DrivePowerMult * (gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x));
                front_right.setPower(DrivePowerMult * ((gamepad1.left_stick_y - gamepad1.left_stick_x) - gamepad1.right_stick_x));




                //Toggles Shooter
                if (gamepad2.x == false) {
                    xDepressed = true;
                }
                if (gamepad2.x && xDepressed) {
                    xDepressed = false;
                }

                slide.setMotorPower(0.75*gamepad2.left_stick_y);
                //toggles Y button
                if (gamepad2.y == false) {
                    yDepressed = true;
                }
                if (gamepad2.y && yDepressed) {
                    yDepressed = false;
                }

                //Toggles Attachment A Button for Grabber, Needs some work. Sometimes doesn't toggle
                if (!gamepad2.a && buttonATimeEllapsed.milliseconds()>250) {
                    aDepressed = true;
                    buttonATimeEllapsed.reset();
                }
                if (gamepad2.a && grabberIsOpen && aDepressed) {
                    yDepressed = false;
                    grabberIsOpen = false;
                    grabber.close();
                }
                if (gamepad2.a && !grabberIsOpen && aDepressed) {
                    yDepressed = false;
                    grabberIsOpen = true;
                    grabber.open();
                }


                //Toggles B
                if (gamepad2.b == false) {
                    bDepressed = true;
                }
                if (gamepad2.b && bDepressed) {
                    stopBot();
                    spinner.spinOffDuck();
                    bDepressed = false;
                }


                if (gamepad2.dpad_down) {


                }
                if (!gamepad2.dpad_down) {

                }

                if (gamepad2.dpad_up) {

                }
                if (!gamepad2.dpad_up) {

                }

                if (gamepad1.b) {
                    DrivePowerMult = 0.3;
                }
                if (gamepad1.x) {
                    DrivePowerMult = 1;
                }
                if (gamepad1.a) {
                    DrivePowerMult = .6;
                }


            }

        }
    }


    private static double boundNumber(double num) {
        if (num > 1) {
            num = 1;
        }
        if (num < -1) {
            num = -1;
        }
        return num;
    }


    private void stopBot() {
        front_left.setPower(0);
        front_right.setPower(0);
        back_right.setPower(0);
        back_left.setPower(0);
    }

    private void turnRight(double power) {
        front_left.setPower(power);
        front_right.setPower(-power);
        back_right.setPower(power);
        back_left.setPower(power);
    }

    private void turnLeft(double power) {
        front_left.setPower(-power);
        front_right.setPower(power);
        back_right.setPower(-power);
        back_left.setPower(-power);
    }


    double getImu() {
        double returnVal = 0;
        if (imu.getAngularOrientation().firstAngle < 0) {
            returnVal = Math.abs(imu.getAngularOrientation().firstAngle);
        } else {
            returnVal = Math.abs(imu.getAngularOrientation().firstAngle - 360);
        }
        return returnVal % 360;

    }



    private void InitIMU() {
        BNO055IMU.Parameters imuParameters;

        // Create new IMU Parameters object.
        imuParameters = new BNO055IMU.Parameters();
        // Use degrees as angle unit.
        imuParameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        // Express acceleration as m/s^2.
        imuParameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        // Disable logging.
        imuParameters.loggingEnabled = false;
        // Calibrate automatically
        imuParameters.mode = BNO055IMU.SensorMode.IMU;
        // Initialize IMU.
        imu.initialize(imuParameters);
        // Prompt user to press start buton.
        //telemetry.addData("IMU initalized", "Press start to continue...");
        Yaw_value = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES).thirdAngle;
        //telemetry.addData("Yaw_value", Yaw_value);
        //telemetry.update();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            while (true) {
                telemetry.addData("Exception:", e.toString());
                telemetry.update();
            }
        }
    }

    private void stopDriveMotors() {
        back_right.setPower(0);
        back_left.setPower(0);
        front_right.setPower(0);
        front_left.setPower(0);
        sleep(0);
    }


    private void StrafeRight2(double DriveDistance, double DrivePower, double DegreeOfTurn, double Forward_drift_mult) {
        init_encoders();
        ElapsedTime time3 = new ElapsedTime();
        back_right.setTargetPosition((int) (1 * DriveDistance));
        back_left.setTargetPosition((int) (-1 * DriveDistance));
        front_right.setTargetPosition((int) (-1 * DriveDistance));
        front_left.setTargetPosition((int) (1 * DriveDistance));
        back_right.setPower(DrivePower);
        back_left.setPower(DrivePower);
        front_right.setPower(DrivePower);
        front_left.setPower(DrivePower);
        while (front_right.isBusy() && !isStopRequested() && time3.seconds() < 6) {
            Yaw_value = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES).thirdAngle;
            telemetry.addData("Yaw_value", Yaw_value);
            telemetry.update();
            back_right.setPower(0.02 * (Yaw_value - DegreeOfTurn) + DrivePower * Forward_drift_mult);
            back_left.setPower(0.02 * (Yaw_value - DegreeOfTurn) + DrivePower);
            front_right.setPower(-0.02 * (Yaw_value - DegreeOfTurn) + DrivePower);
            front_left.setPower(-0.02 * (Yaw_value - DegreeOfTurn) + DrivePower * Forward_drift_mult);
        }
        sleep(0);
    }

    private void init_encoders() {
        front_right.setDirection(DcMotorSimple.Direction.REVERSE);
        back_right.setDirection(DcMotorSimple.Direction.REVERSE);
        front_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        front_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        back_left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        back_right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        front_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        front_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        back_left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        back_right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        front_left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        front_right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        back_left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        back_right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        front_left.setTargetPosition(0);
        front_right.setTargetPosition(0);
        back_left.setTargetPosition(0);
        back_right.setTargetPosition(0);
        front_left.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        front_right.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        back_left.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        back_right.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }





    private double getVoltage() {
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            return sensor.getVoltage();
        }

        return 0;

    }


    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }



    private void print(String caption, Object value) {
        if (this.telemetry != null) {
            telemetry.addData(caption, value);
        }
    }

    private void printFlush() {
        if (this.telemetry != null) {
            telemetry.update();
        }
    }

    public static double getAngle(double rx, double ry, double x, double y, double robotRot) {
        double angle;
        x = x - rx;
        y = y - ry;
        angle = Math.toDegrees(Math.atan2(x, y));
        return ((angle - robotRot) % 360);
    }

    public void strafeAtAngle(double angle, double power) {
        power = boundNumber(power);
        double power1 = 0;
        double power2 = 0;
        angle = angle % 360;

        power1 = Math.cos(Math.toRadians(angle + 45.0));
        power2 = Math.cos(Math.toRadians(angle - 45));

        power1 = power * power1;
        power2 = power * power2;

        front_right.setPower(power1);
        back_left.setPower(power1);

        front_left.setPower(power2);
        back_right.setPower(-power2);

    }


}