package org.firstinspires.ftc.teamcode.Base;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class MainBase {

    // internal instance of opMode
    public OpMode opMode = null;

    // Total Motors: 7
    // Total Servos: 0
    public DcMotor frontleft = null;
    public DcMotor frontright = null;
    public DcMotor backleft = null;
    public DcMotor backright = null;
    public DcMotor lift = null;
    public DcMotor rightgrabber = null;
    public DcMotor leftgrabber = null;


    public ColorSensor color = null;
//    ModernRoboticsI2cRangeSensor rangebrothers;
//    Rev2mDistanceSensor rangebrothers2;
    public ModernRoboticsI2cGyro gyro = null;



    //28 * 20 / (2ppi * 4.125)
    public Integer cpr = 13; //counts per rotation
    public Integer gearratio = 40;
    public Double diameter = 4.125;
    public Double cpi = (cpr * gearratio) / (Math.PI * diameter); //counts per inch, 28cpr * gear ratio / (2 * pi * diameter (in inches, in the center))
    public Double bias = 1.0;//default 0.8
    public Double meccyBias = 0.9;//change to adjust only strafing movement
    public double amountError = 2;

    public static final double HEADING_THRESHOLD = 1;      // As tight as we can make it with an integer gyro
    public static final double P_TURN_COEFF = 0.1;     // Larger is more responsive, but also less stable
    public static final double P_DRIVE_COEFF = 0.07;     // Larger is more responsive, but also less stable

    public double DRIVE_SPEED08 = 0.88;
    public double DRIVE_SPEED07 = 0.4;
    public double DRIVE_SPEED06 = 0.3;


    public static double   MAX_ACCEPTABLE_ERROR = 2;

    public double TURN_SPEED = 0.65;
    public double TURN_SPEED_FIX = 0.3;
    public double GYRO_HOLD_TIME = 0.4;
    public double FIND_LINE_TIME = 0.4;





    public Double conversion = cpi * bias;
    public Boolean exit = false;




    private ElapsedTime runtime = new ElapsedTime();

    /**
     *  initialize
     * @param ahwMap
     * @param gievnOpMode
     */
    public void init(HardwareMap ahwMap, OpMode gievnOpMode) {

        opMode = gievnOpMode;

        HardwareMap hwMap = ahwMap;

        frontleft    = hwMap.get(DcMotor.class, "frontleft");
        frontright   = hwMap.get(DcMotor.class, "frontright");
        backleft = hwMap.get(DcMotor.class, "backleft");
        backright = hwMap.get(DcMotor.class, "backright");
        lift      = hwMap.get(DcMotor.class, "lift");
        rightgrabber    = hwMap.get(DcMotor.class,"rightgrabber");
        leftgrabber    = hwMap.get(DcMotor.class,"leftgrabber");

        gyro = hwMap.get(ModernRoboticsI2cGyro.class,"Gyro");
        color = hwMap.get(ColorSensor.class,"sensor_color");


        backleft.setDirection(DcMotor.Direction.FORWARD);
        frontleft.setDirection(DcMotor.Direction.FORWARD);
        backright.setDirection(DcMotor.Direction.REVERSE);
        frontright.setDirection(DcMotor.Direction.REVERSE);
        lift.setDirection(DcMotor.Direction.FORWARD);
        leftgrabber.setDirection(DcMotor.Direction.FORWARD);
        rightgrabber.setDirection(DcMotor.Direction.FORWARD);



        backleft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontleft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backright.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontright.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftgrabber.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightgrabber.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        backleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftgrabber.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightgrabber.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        backleft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontleft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backright.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontright.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftgrabber.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightgrabber.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        backleft.setPower(0);
        frontleft.setPower(0);
        backright.setPower(0);
        frontright.setPower(0);
        lift.setPower(0);
        leftgrabber.setPower(0);
        rightgrabber.setPower(0);

        opMode.telemetry.addLine("Start Gyro");
        opMode.telemetry.update();
        gyro.calibrate();
        while (gyro.isCalibrating()) ;
        opMode.telemetry.addLine("Gyro Calibrated");
        opMode.telemetry.addData("Angle: ", gyro.getIntegratedZValue());
        opMode.telemetry.update();
    }

    // ***************************************************************
    // Public functions
    // ***************************************************************



    public void gyroStrafe(double inches,double angle, double speed, LinearOpMode opMode) {
//
        int move = (int) (Math.round(inches * cpi * meccyBias));
//\
        int newFrontLeftTarget = frontleft.getCurrentPosition() + move;
        int newFrontRightTarget = frontright.getCurrentPosition() - move;
        int newBackLeftTarget = backleft.getCurrentPosition() - move;
        int newBackRightTarget = backright.getCurrentPosition() + move;
        backleft.setTargetPosition(newBackLeftTarget);
        frontleft.setTargetPosition(newFrontLeftTarget);
        backright.setTargetPosition(newBackRightTarget);
        frontright.setTargetPosition(newFrontRightTarget);


        double HalfMaxOne;
        double HalfMaxTwo;

        double max;

        double error;
        double steer;
        double frontLeftSpeed;
        double frontRightSpeed;
        double backLeftSpeed;
        double backRightSpeed;

        double ErrorAmount;
        boolean goodEnough = false;
//
        frontleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//
        frontleft.setPower(speed);
        backleft.setPower(speed);
        frontright.setPower(speed);
        backright.setPower(speed);
//
        while ((frontleft.isBusy() && frontright.isBusy() && backleft.isBusy() && backright.isBusy()) && !goodEnough) {
            error = getError(angle);
            steer = getSteer(error, P_DRIVE_COEFF);

            // if driving in reverse, the motor correction also needs to be reversed
            if (inches < 0)
                steer *= -1.0;

            frontLeftSpeed = speed - steer;
            backLeftSpeed = speed + steer;
            backRightSpeed = speed + steer;
            frontRightSpeed = speed - steer;

            // Normalize speeds if either one exceeds +/- 1.0;
            HalfMaxOne = Math.max(Math.abs(frontLeftSpeed), Math.abs(backLeftSpeed));
            HalfMaxTwo = Math.max(Math.abs(frontRightSpeed), Math.abs(backRightSpeed));
            max = Math.max(Math.abs(HalfMaxOne), Math.abs(HalfMaxTwo));
            if (max > 1.0) {
                frontLeftSpeed /= max; //frontLeftSpeed = frontLeftSpeed / max += -= *= %=
                frontRightSpeed /= max;
                backLeftSpeed /= max;
                backRightSpeed /= max;
            }


            frontleft.setPower(frontLeftSpeed);
            frontright.setPower(frontRightSpeed);
            backleft.setPower(backLeftSpeed);
            backright.setPower(backRightSpeed);

            // Display drive status for the driver.
            opMode.telemetry.addData("Err/St", "%5.1f/%5.1f", error, steer);
            opMode.telemetry.addData("Target", "%7d:%7d", newBackLeftTarget, newBackRightTarget, newFrontLeftTarget, newFrontRightTarget);
            opMode.telemetry.addData("Actual", "%7d:%7d", backleft.getCurrentPosition(), backright.getCurrentPosition(), frontleft.getCurrentPosition(), frontright.getCurrentPosition());
            opMode.telemetry.addData("Speed", "%5.2f:%5.2f", backLeftSpeed, backRightSpeed, frontLeftSpeed, frontRightSpeed);
            opMode.telemetry.update();

            ErrorAmount = ((Math.abs(((newBackLeftTarget) - (backleft.getCurrentPosition())))
                    + (Math.abs(((newFrontLeftTarget) - (frontleft.getCurrentPosition()))))
                    + (Math.abs((newBackRightTarget) - (backright.getCurrentPosition())))
                    + (Math.abs(((newFrontRightTarget) - (frontright.getCurrentPosition()))))) / cpi);
            if (ErrorAmount < amountError) {
                goodEnough = true;
            }


        }
        frontright.setPower(0);
        frontleft.setPower(0);
        backright.setPower(0);
        backleft.setPower(0);
    }
    /**
     * drives towards the backleft and frontright of the robot
     * @param inches
     * @param speed
     */
    public void BLandFR(double inches, double speed) {


        int move = (int) (Math.round(inches * cpi * meccyBias));

        frontleft.setTargetPosition(frontleft.getCurrentPosition() + move);
        backright.setTargetPosition(backright.getCurrentPosition() + move);

        frontleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backright.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        frontleft.setPower(speed);
        backright.setPower(speed);

        while ( frontleft.isBusy() && backright.isBusy()) {
        }

        frontleft.setPower(0);
        backright.setPower(0);
        return;

    }

    /**
     * drives towards the backright and frontleft of the robot
     * @param inches
     * @param speed
     */
    public void BRandFL(double inches, double speed) {


        int move = (int) (Math.round(inches * cpi * meccyBias));

        backleft.setTargetPosition(backleft.getCurrentPosition() - move);
        frontright.setTargetPosition(frontright.getCurrentPosition() - move);


        frontright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);


        backleft.setPower(speed);
        frontright.setPower(speed);


        while ( frontright.isBusy() && backleft.isBusy()) {
        }
        frontright.setPower(0);
        backleft.setPower(0);
        return;

    }


    /**
     * Find tape on field using color sensor
     * Change FIND_LINE_TIME to tell it how long it has to find the line before exiting program
     */
    public void findLine(){


        ElapsedTime holdTimer = new ElapsedTime();

        // keep looping while we have time remaining.
        holdTimer.reset();
        while (color.blue() <= 270 && (holdTimer.time() < FIND_LINE_TIME)) {
            backright.setPower(-.05);
            backleft.setPower(.05);
            frontright.setPower(.05);
            frontleft.setPower(-.05);

        }

        backright.setPower(0);
        backleft.setPower(0);
        frontright.setPower(0);
        frontleft.setPower(0);
    }


    /**
     * Lift operations
     * @param inches up is +, down is -
     * @param speed
     */
    public void goLift(double inches, double speed){

        opMode.telemetry.addData("Encodercount", lift.getCurrentPosition() );
        opMode.telemetry.update();
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        int move =  -(int)(Math.round(inches*93.3));

        lift.setTargetPosition(lift.getCurrentPosition() + move);
        lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift.setPower(speed);
    }

    /**
     * Grab cone
     */
    public void getItGirl(LinearOpMode opMode){
        while(lift.isBusy()){
        }
        leftgrabber.setPower(.7);
        rightgrabber.setPower(-.7);

        opMode.sleep(1000);

        rightgrabber.setPower(0);
        leftgrabber.setPower(0);
    }

    /**
     * Let go of cone
     */
    public void letGoGirl(LinearOpMode opMode){
        goLift(-6,.7);
        while(lift.isBusy()){
        }
        leftgrabber.setPower(-1);
        rightgrabber.setPower(1);

        opMode.sleep(700);

        rightgrabber.setPower(0);
        leftgrabber.setPower(0);
        goLift(6,.9);
    }


    /**
     *  Driving using Modern Robotics Gyro
     * @param speed
     * @param frontLeftInches
     * @param frontRightInches
     * @param backLeftInches
     * @param backRightInches
     * @param angle
     */
    public void MRgyroDrive(double speed,
                          double frontLeftInches, double frontRightInches, double backLeftInches,
                          double backRightInches,
                          double angle,
                            LinearOpMode opMode) {

        int newFrontLeftTarget;
        int newFrontRightTarget;
        int newBackLeftTarget;
        int newBackRightTarget;

        double HalfMaxOne;
        double HalfMaxTwo;

        double max;

        double error;
        double steer;
        double frontLeftSpeed;
        double frontRightSpeed;
        double backLeftSpeed;
        double backRightSpeed;

        double ErrorAmount;
        boolean goodEnough = false;

        // Ensure that the opmode is still active

        if (opMode.opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newFrontLeftTarget = frontleft.getCurrentPosition() + (int) (frontLeftInches * cpi);
            newFrontRightTarget = frontright.getCurrentPosition() + (int) (frontRightInches * cpi);
            newBackLeftTarget = backleft.getCurrentPosition() + (int) (backLeftInches * cpi);
            newBackRightTarget = backright.getCurrentPosition() + (int) (backRightInches * cpi);


            // Set Target and Turn On RUN_TO_POSITION
            frontleft.setTargetPosition(newFrontLeftTarget);
            frontright.setTargetPosition(newFrontRightTarget);
            backleft.setTargetPosition(newBackLeftTarget);
            backright.setTargetPosition(newBackRightTarget);

            frontleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            frontright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            backleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            backright.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // start motion.
            speed = Range.clip(Math.abs(speed), 0.0, 1.0);
            frontleft.setPower(Math.abs(speed));
            frontright.setPower(Math.abs(speed));
            backleft.setPower(Math.abs(speed));
            backright.setPower(Math.abs(speed));
            // keep looping while we are still active, and BOTH motors are running.
            while (opMode.opModeIsActive() &&
                    ((frontleft.isBusy() && frontright.isBusy()) && (backleft.isBusy() && backright.isBusy())) && !goodEnough) {


                // adjust relative speed based on heading error.
                error = getError(angle);
                steer = getSteer(error, P_DRIVE_COEFF);

                // if driving in reverse, the motor correction also needs to be reversed
                if (frontLeftInches < 0 && frontRightInches < 0 && backLeftInches < 0 && backRightInches < 0)
                    steer *= -1.0;

                frontLeftSpeed = speed - steer;
                backLeftSpeed = speed - steer;
                backRightSpeed = speed + steer;
                frontRightSpeed = speed + steer;

                // Normalize speeds if either one exceeds +/- 1.0;
                HalfMaxOne = Math.max(Math.abs(frontLeftSpeed), Math.abs(backLeftSpeed));
                HalfMaxTwo = Math.max(Math.abs(frontRightSpeed), Math.abs(backRightSpeed));
                max = Math.max(Math.abs(HalfMaxOne), Math.abs(HalfMaxTwo));
                if (max > 1.0) {
                    frontLeftSpeed /= max;
                    frontRightSpeed /= max;
                    backLeftSpeed /= max;
                    backRightSpeed /= max;
                }

                frontleft.setPower(frontLeftSpeed);
                frontright.setPower(frontRightSpeed);
                backleft.setPower(backLeftSpeed);
                backright.setPower(backRightSpeed);

                // Display drive status for the driver.
//                telemetry.addData("Err/St", "%5.1f/%5.1f", error, steer);
//                telemetry.addData("Target", "%7d:%7d", newBackLeftTarget, newBackRightTarget, newFrontLeftTarget, newFrontRightTarget);
//                telemetry.addData("Actual", "%7d:%7d", backleft.getCurrentPosition(), backright.getCurrentPosition(), frontleft.getCurrentPosition(), frontright.getCurrentPosition());
//                telemetry.addData("Speed", "%5.2f:%5.2f", backLeftSpeed, backRightSpeed, frontLeftSpeed, frontRightSpeed);
//                telemetry.update();

                ErrorAmount = ((Math.abs(((newBackLeftTarget) - (backleft.getCurrentPosition())))
                        + (Math.abs(((newFrontLeftTarget) - (frontleft.getCurrentPosition()))))
                        + (Math.abs((newBackRightTarget) - (backright.getCurrentPosition())))
                        + (Math.abs(((newFrontRightTarget) - (frontright.getCurrentPosition()))))) / cpi);
                if (ErrorAmount < amountError) {
                    goodEnough = true;
                }
            }

            // Stop all motion;
            frontleft.setPower(0);
            frontright.setPower(0);
            backleft.setPower(0);
            backright.setPower(0);

            // Turn off RUN_TO_POSITION

            frontleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            frontright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            backleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            backright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    /**
     * Turning using Modern Robotics Gyro
     *
     * @param speed
     * @param angle
     */

    public void MRgyroTurn(double speed, double angle , LinearOpMode opmode) {
        frontleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // keep looping while we are still active, and not on heading.
        while (opmode.opModeIsActive() && !onHeading(speed, angle, P_TURN_COEFF)) {
            // Update telemetry & Allow time for other processes to run.
            opMode.telemetry.update();
        }
        frontleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backright.setMode(DcMotor.RunMode.RUN_TO_POSITION);

    }

    /**
     * Using Modern Robotics Gyro
     * Method to obtain & hold a heading for a finite amount of time
     * Move will stop once the requested time has elapsed
     *
     * @param speed    Desired speed of turn.
     * @param angle    Absolute Angle (in Degrees) relative to last gyro reset.
     *                 0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                 If a relative angle is required, add/subtract from current heading.
     * @param holdTime Length of time (in seconds) to hold the specified heading.
     */


    public void gyroHold(double speed, double angle, double holdTime, LinearOpMode opMode) {

        ElapsedTime holdTimer = new ElapsedTime();

        // keep looping while we have time remaining.
        holdTimer.reset();
        while (opMode.opModeIsActive() && (holdTimer.time() < holdTime)) {
            // Update telemetry & Allow time for other processes to run.
            onHeading(speed, angle, P_TURN_COEFF);
            opMode.telemetry.update();


        }

        // Stop all motion;
        frontleft.setPower(0);
        backleft.setPower(0);
        backright.setPower(0);
        frontright.setPower(0);
    }


    /**
     * Using Modern Robotics Gyro
     * Perform one cycle of closed loop heading control.
     *
     * @param speed  Desired speed of turn.
     * @param angle  Absolute Angle (in Degrees) relative to last gyro reset.
     *               0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *               If a relative angle is required, add/subtract from current heading.
     * @param PCoeff Proportional Gain coefficient
     * @return
     */
    boolean onHeading(double speed, double angle, double PCoeff) {
        double error;
        double steer;
        boolean onTarget = false;
        double leftSpeed;
        double rightSpeed;

        // determine turn power based on +/- error
        error = getError(angle);

        if (Math.abs(error) <= HEADING_THRESHOLD) {
            steer = 0.0;
            leftSpeed = 0.0;
            rightSpeed = 0.0;
            onTarget = true;
        } else {
            steer = getSteer(error, PCoeff);
            rightSpeed = speed * steer;
            leftSpeed = -rightSpeed;
        }

        // Send desired speeds to motors.
        frontleft.setPower(leftSpeed);
        backleft.setPower(leftSpeed);
        backright.setPower(rightSpeed);
        frontright.setPower(rightSpeed);

        // Display it for the driver.
//        telemetry.addData("Target", "%5.2f", angle);
//        telemetry.addData("Err/St", "%5.2f/%5.2f", error, steer);
//        telemetry.addData("Speed.", "%5.2f:%5.2f", leftSpeed, rightSpeed);

        return onTarget;
    }

    /**
     * Using Modern Robotics Gyro
     * getError determines the error between the target angle and the robot's current heading
     *
     * @param targetAngle Desired angle (relative to global reference established at last Gyro Reset).
     * @return error angle: Degrees in the range +/- 180. Centered on the robot's frame of reference
     * +ve error means the robot should turn LEFT (CCW) to reduce error.
     */
    public double getError(double targetAngle) {

        double robotError;

        // calculate error in -179 to +180 range  (
        robotError = targetAngle - gyro.getHeading();
        while (robotError > 180) robotError -= 360;
        while (robotError <= -180) robotError += 360;
        return -robotError;
    }

    /**
     * Using Modern Robotics Gyro
     * returns desired steering force.  +/- 1 range.  +ve = steer left
     *
     * @param error  Error angle in robot relative degrees
     * @param PCoeff Proportional Gain Coefficient
     * @return
     */
    public double getSteer(double error, double PCoeff) {
        return Range.clip(error * PCoeff, -DRIVE_SPEED07, 1);
    }
    // ***************************************************************
    // Private functions
    // ***************************************************************

}
