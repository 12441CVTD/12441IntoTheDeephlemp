package org.firstinspires.ftc.teamcode.driver;

import static android.os.SystemClock.sleep;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.shared.MotionHardware;

@TeleOp(name = "Threaded TeleOp", group = "TeleOp Driver")
public class Driver extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();
    static final double INCREMENT = 0.01;     // amount to ramp motor each CYCLE_MS cycle
    static final int CYCLE_MS = 50;     // period of each cycle
    static final double MAX_FWD = 1.0;     // Maximum FWD power applied to motor
    static final double MAX_REV = -1.0;     // Maximum REV power applied to motor

    static final double ARM_SPEED = 1.0;
    static final int ARM_DROP_POS = -4530;
    static final int ARM_DRIVE_POS = -800;
    static final int ARM_INTAKE_POS = 0;
    static final double LEFT_GRIPPER_OPEN = 0.95;

    //was 1.5
    static final double LEFT_GRIPPER_CLOSE = 2;
    static final double RIGHT_GRIPPER_OPEN = 0.05;

    //was -0.5
    static final double RIGHT_GRIPPER_CLOSE = -0.9;
    static final double WRIST_DROP_POS = 0.7;
    static final double WRIST_INTAKE_POS = 0.3;
    private Servo leftGripper;
    private Servo rightGripper;
    private Servo wristServo;
    private Servo launcherServo = null;
    private Servo DroneCoverServo = null;
    private DcMotor armMotor = null;

    private Thread launcherThread;
    private Thread armThread;
    MotionHardware robot = new MotionHardware(this);


    @Override
    public void runOpMode() {

        // Define class members
        DcMotor motor;
        double power = 0;
        boolean rampUp = true;
        //Define variables for arm, wrist, and gripper
        //DcMotor armMotor;
        //CRServo contServo;
        float leftY, rightY;
        double armPosition, gripPosition, contPower;
        double MIN_POSITION = 0, MAX_POSITION = 1;
        // called when init button is  pressed.


        armMotor = hardwareMap.get(DcMotor.class, "armMotor");
        //armMotor = hardwareMap.dcMotor.get("armMotor");
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        wristServo = hardwareMap.servo.get("wristServo");
        launcherServo = hardwareMap.get(Servo.class, "launcherServo");
        DroneCoverServo = hardwareMap.get(Servo.class, "DroneCoverServo");
        leftGripper = hardwareMap.get(Servo.class, "leftGripper");
        rightGripper = hardwareMap.get(Servo.class, "rightGripper");
        DcMotor frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        DcMotor backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        DcMotor frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        DcMotor backRightMotor = hardwareMap.dcMotor.get("backRightMotor");
        //frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        //backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        //robot.init();

        initializeLauncherThread();
        initializeArmThread();

        waitForStart();

        launcherThread.start();
        armThread.start();




        telemetry.addData("Mode", "waiting");

        while (opModeIsActive()) {
            telemetry.addData("Mode", "running");
            // check to see if we need to move the servo.

            telemetry.addData("Servo Position", launcherServo.getPosition());
            telemetry.addData("Status", "Running");
            telemetry.update();

            telemetry.addData("Mode", "running");
            // check to see if we need to move the servo.

            telemetry.addData("Servo Position", DroneCoverServo.getPosition());
            telemetry.addData("Status", "Running");
            telemetry.update();

            // check arm position
            telemetry.addData("Arm Position",armMotor.getCurrentPosition());
            telemetry.update();

            // Declare Motors

            double y = gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = -gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = -gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator; //
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            frontLeftMotor.setPower(frontLeftPower);
            backLeftMotor.setPower(backLeftPower);
            frontRightMotor.setPower(frontRightPower);
            backRightMotor.setPower(backRightPower);



            if (gamepad2.right_bumper) {
                // Move servos in opposite directions when "y" is pressed
                leftGripper.setPosition(LEFT_GRIPPER_CLOSE);
                rightGripper.setPosition(RIGHT_GRIPPER_CLOSE);// Adjust the position value as needed

            } else if (gamepad2.left_bumper) {
                // Return servos to the center position when "x" is pressed
                leftGripper.setPosition(LEFT_GRIPPER_OPEN);
                rightGripper.setPosition(RIGHT_GRIPPER_OPEN); // Adjust the position value for the center position
            }
            telemetry.update();
            }

        // Stop the threads when the op mode is no longer active
        if (launcherThread != null) launcherThread.interrupt();
        if (armThread != null) armThread.interrupt();
    }




    private void initializeLauncherThread() {
        launcherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    if (gamepad1.dpad_left) {
                        // move to 0 degrees.
                        DroneCoverServo.setPosition(0.3);
                    } else if (gamepad1.dpad_right) {
                        // move to 90 degrees.
                        DroneCoverServo.setPosition(.7);
                    }

                    if (gamepad1.left_bumper) {
                        // move to 0 degrees.
                        launcherServo.setPosition(0.3);
                    } else if (gamepad1.right_bumper || gamepad1.left_bumper) {
                        // move to 90 degrees.
                        launcherServo.setPosition(1);
                    }

                    try {
                        Thread.sleep(10); // Small delay to prevent looping too fast
                    } catch (InterruptedException e) {
                        break; // Exit the loop if the thread is interrupted
                    }
                }
            }
        });
    }


    private void initializeArmThread() {
        ElapsedTime runtime = new ElapsedTime();

        armThread = new Thread(new Runnable() {
            @Override
            public void run() {
                float leftY, rightY;
                while (!Thread.interrupted()) {
                    //leftY = gamepad2.left_stick_y * -1;
                    //rightY = gamepad2.right_stick_y * -1;
                    //armMotor.setPower(Range.clip(leftY, -1.0, 1.0));
                    //Between here..


                    if (gamepad2.y) {
                        wristServo.setPosition(WRIST_DROP_POS);
                        moveArmMotorToPosition(ARM_DROP_POS, 2);
                    }
                    else if (gamepad2.x) {
                        wristServo.setPosition(WRIST_INTAKE_POS);
                        moveArmMotorToPosition(ARM_INTAKE_POS, 2.6);
                    } else if (gamepad2.dpad_up) {
                        moveArmMotorToPosition(ARM_DRIVE_POS, 2.6);
                        wristServo.setPosition(WRIST_INTAKE_POS);
                    }

                    //and here put your logic to move the arm up and down
                    try {
                        Thread.sleep(10); // Small delay to prevent looping too fast
                    } catch (InterruptedException e) {
                        break; // Exit the loop if the thread is interrupted
                    }
                }
            }
        });
    }

    private void moveArmMotorToPosition(int position, double timeoutS) {
        ElapsedTime runtime = new ElapsedTime();

        runtime.reset();
        armMotor.setTargetPosition(position);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(ARM_SPEED); // Set your desired power
        while ((armMotor.isBusy()) && (runtime.seconds() < timeoutS)) {

            telemetry.addData("Running to", "%7d", position);
            telemetry.addData("Currently at", "%7d", armMotor.getCurrentPosition());
            telemetry.update();
        }
        armMotor.setPower(0); // Stop the motor once the position is reached
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
}



