package org.firstinspires.ftc.team8923_PowerPlay;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.team8923_PowerPlay.BaseOpMode;


@Autonomous(name = "BaseAutonomous")

public abstract class BaseAutonomous extends BaseOpMode {

    private ElapsedTime runtime = new ElapsedTime();

    double robotX;
    double robotY;
    double robotAngle;
    double headingOffset = 0.0;

    int newTargetFL;
    int newTargetFR;
    int newTargetBL;
    int newTargetBR;

    int errorFR;
    int errorFL;
    int errorBR;
    int errorBL;

    double speedFL;
    double speedFR;
    double speedBL;
    double speedBR;

    double Kmove = 1.0f/1200.0f;


    int TOL = 100;

    boolean isDoneSettingUp = false;

    //Used to calculate distance traveled between loops
    int lastEncoderFL = 0;
    int lastEncoderFR = 0;
    int lastEncoderBL = 0;
    int lastEncoderBR = 0;

    boolean autoReverseDrive = false;

    Alliance alliance = Alliance.BLUE;
    Destinations destination = Destinations.SQUAREA;

    int rings;



    int delays = 0;
    int numOfSecondsDelay = 0;
    int delayTime = 0;

    double DRIVE_POWER_CONSTANT = 1.0/1000;
    double TURN_POWER_CONSTANT = 1.0/65;

    double MIN_DRIVE_POWER = 0.2;

    enum Alliance{
        BLUE,RED
    }


    enum Destinations{
        SQUAREA, SQUAREB, SQUAREC
    }

    public void initAuto() {
        telemetry.addData("Init State", "Init Started");
        telemetry.update();
        initHardware();
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        telemetry.addData("Init State", "Init Finished");
        telemetry.addData("Alliance", alliance.name());
        telemetry.addData("Delay Time", delayTime);



        //Set last know encoder values
        lastEncoderFR = motorFR.getCurrentPosition();
        lastEncoderFL = motorFL.getCurrentPosition();
        lastEncoderBL = motorBL.getCurrentPosition();
        lastEncoderBR = motorBR.getCurrentPosition();

        //set IMU heading offset
        headingOffset = imu.getAngularOrientation().firstAngle - robotAngle;

        telemetry.clear();
        telemetry.update();
        telemetry.addLine("Initialized. Ready to start!");




    }

    public void configureAutonomous(){
        while(!isDoneSettingUp){
            if(gamepad1.x){
                alliance = Alliance.BLUE;
            }else if(gamepad1.b){
                alliance = Alliance.RED;
            }
            if(gamepad1.dpad_up){
                delays++;
            }else if(gamepad1.dpad_down){
                delays--;
            }
            if(rings == 0){
                destination = Destinations.SQUAREA;

            }else if(rings == 1){
                destination = Destinations.SQUAREB;
            }else if(rings == 4){
                destination = Destinations.SQUAREC;
            }



            if(gamepad1.start){
                isDoneSettingUp = true;
            }
            // input information
            telemetry.addLine("Alliance Blue/Red: X/B");
            telemetry.addLine("Add a delay: D-Pad Up/Down");
            telemetry.addLine("toggle objective: a park/park and foundation");
            telemetry.addLine("After routine is complete and robot is on field, press Start");

            telemetry.addLine();
            telemetry.addData("alliance: ", alliance);
            telemetry.addData("delays:", delays);
            telemetry.addData("destination", destination);
            telemetry.update();



        }
    }

    public void moveAuto(double x, double y, double speed, double minSpeed, double timeout) throws InterruptedException
    {
        newTargetFL = motorFL.getCurrentPosition() + (int) Math.round(Constants.COUNTS_PER_MM * y) + (int) Math.round(Constants.COUNTS_PER_MM * x * 1.15);
        newTargetFR = motorFR.getCurrentPosition() + (int) Math.round(Constants.COUNTS_PER_MM * y) - (int) Math.round(Constants.COUNTS_PER_MM * x * 1.15);
        newTargetBL = motorBL.getCurrentPosition() + (int) Math.round(Constants.COUNTS_PER_MM * y) - (int) Math.round(Constants.COUNTS_PER_MM * x * 1.15);
        newTargetBR = motorBR.getCurrentPosition() + (int) Math.round(Constants.COUNTS_PER_MM * y) + (int) Math.round(Constants.COUNTS_PER_MM * x * 1.15);
        runtime.reset();
        do
        {
            errorFL = newTargetFL - motorFL.getCurrentPosition();
            speedFL = Math.abs(errorFL * Kmove);
            speedFL = Range.clip(speedFL, minSpeed, speed);
            speedFL = speedFL * Math.signum(errorFL);

            errorFR = newTargetFR - motorFR.getCurrentPosition();
            speedFR = Math.abs(errorFR * Kmove);
            speedFR = Range.clip(speedFR, minSpeed, speed);
            speedFR = speedFR * Math.signum(errorFR);

            errorBL = newTargetBL - motorBL.getCurrentPosition();
            speedBL = Math.abs(errorBL * Kmove);
            speedBL = Range.clip(speedBL, minSpeed, speed);
            speedBL = speedBL * Math.signum(errorBL);

            errorBR = newTargetBR - motorBR.getCurrentPosition();
            speedBR = Math.abs(errorBR * Kmove);
            speedBR = Range.clip(speedBR, minSpeed, speed);
            speedBR = speedBR * Math.signum(errorBR);

            motorFL.setPower(speedFL);
            motorFR.setPower(speedFR);
            motorBL.setPower(speedBL);
            motorBR.setPower(speedBR);
            idle();
        }
        while (opModeIsActive() &&
                (runtime.seconds() < timeout) &&
                (Math.abs(errorFL) > TOL || Math.abs(errorFR) > TOL || Math.abs(errorBL) > TOL || Math.abs(errorBR) > TOL));

        stopDriving();
    }

    //using imu
    public void imuPivot(double referenceAngle, double targetAngle, double maxSpeed, double kAngle, double timeout){
        runtime.reset();
        //counter-clockwise is positive
        double pivot;
        double currentRobotAngle;
        double angleError;

        targetAngle = referenceAngle + targetAngle;
        targetAngle = adjustAngles(targetAngle);
        do{
            currentRobotAngle = imu.getAngularOrientation().firstAngle;
            angleError =  currentRobotAngle - targetAngle;
            angleError = adjustAngles(angleError);
            pivot = angleError * kAngle;

            if (pivot >= 0.0){
                pivot = Range.clip(pivot, 0.15, maxSpeed);
            }else{
                pivot = Range.clip(pivot, -maxSpeed, -0.15);
            }

            speedFL = pivot;
            speedFR = pivot;
            speedBL = pivot;
            speedBR = pivot;

            motorFL.setPower(speedFL);
            motorFR.setPower(speedFR);
            motorBL.setPower(speedBL);
            motorBR.setPower(speedBR);
            idle();
        }
        while((opModeIsActive() && (Math.abs(angleError) > 3.0)) && (runtime.seconds() < timeout));
        stopDriving();




    }

    public void reverseImuPivot(double referenceAngle, double targetAngle, double maxSpeed, double kAngle, double timeout){
        runtime.reset();
        //counter-clockwise is positive
        double pivot;
        double currentRobotAngle;
        double angleError;

        targetAngle = referenceAngle + targetAngle;
        targetAngle = adjustAngles(targetAngle);
        do{
            currentRobotAngle = imu.getAngularOrientation().firstAngle;
            targetAngle = adjustAngles(targetAngle);
            angleError = currentRobotAngle - targetAngle;
            angleError = adjustAngles(angleError);
            pivot = angleError * kAngle;

            if(pivot >= 0.0){
                pivot = Range.clip(pivot, 0.15, maxSpeed);
            }else{
                pivot = Range.clip(pivot, -maxSpeed, -0.15);
            }

            speedFL = -pivot;
            speedFR = pivot;
            speedBL = -pivot;
            speedBR = pivot;

            motorFL.setPower(speedFL);
            motorFR.setPower(speedFR);
            motorBL.setPower(speedBL);
            motorBR.setPower(speedBR);
            idle();
        } while(opModeIsActive() && (Math.abs(angleError) > 3.0) && (runtime.seconds() < timeout));

        stopDriving();

    }

    public void sendTelemetry(){

        //Informs drivers of robot location
        telemetry.addData("X", robotX);
        telemetry.addData("Y", robotY);
        telemetry.addData("Robot Angle", imu.getAngularOrientation().firstAngle);
    }

    private void stopDriving(){

        motorFL.setPower(0.0);
        motorFR.setPower(0.0);
        motorBL.setPower(0.0);
        motorBR.setPower(0.0);

    }

    //normalizing the angle to be between -180 to 180
    private double adjustAngles(double angle){
        while(angle > 180)
            angle -= 360;
        while(angle < -180)
            angle += 360;
        return angle;

    }

    private double normalizeAngle(double rawAngle){
        while(Math.abs(rawAngle) > 180){
            rawAngle -= Math.signum(rawAngle) * 360;
        }
        return rawAngle;
    }

    private void updateRobotLocation(){
        // Update robot angle
        // subtraction here b/c imu returns a negative rotation when turned to the right
        robotAngle = headingOffset - imu.getAngularOrientation().firstAngle;

        // Calculate how far each motor has turned since last time
        int deltaFL = motorFL.getCurrentPosition() - lastEncoderFL;
        int deltaFR = motorFR.getCurrentPosition() - lastEncoderFR;
        int deltaBL = motorBL.getCurrentPosition() - lastEncoderBL;
        int deltaBR = motorBR.getCurrentPosition() - lastEncoderBR;

        // Take average of encoder ticks to find translational x and y components. FR and BL are
        // negative because of the direction at which they turn when going sideways
        double deltaX = (deltaFL - deltaFR - deltaBL + deltaBR) / 4.0;
        double deltaY = (deltaFL + deltaFR + deltaBL + deltaBR) / 4.0;

        telemetry.addData("deltaX", deltaX);
        telemetry.addData("deltaY", deltaY);

        // Convert to mm
        //TODO: maybe wrong proportions? something about 70/30 effectiveness maybe translation to MM is wrong
        deltaX *= Constants.TICKS_PER_INCH;
        deltaY *= Constants.TICKS_PER_INCH;

        /*
         * Delta x and y are intrinsic to the robot, so they need to be converted to extrinsic.
         * Each intrinsic component has 2 extrinsic components, which are added to find the
         * total extrinsic components of displacement. The extrinsic displacement components
         * are then added to the previous position to set the new coordinates
         */

        robotX += deltaX * Math.sin(Math.toRadians(robotAngle)) + deltaY * Math.cos(Math.toRadians(robotAngle));
        robotY += deltaX * -Math.cos(Math.toRadians(robotAngle)) + deltaY * Math.sin(Math.toRadians(robotAngle));

        // Set last encoder values for next loop
        lastEncoderFL = motorFL.getCurrentPosition();
        lastEncoderFR = motorFR.getCurrentPosition();
        lastEncoderBL = motorBL.getCurrentPosition();
        lastEncoderBR = motorBR.getCurrentPosition();


    }

}