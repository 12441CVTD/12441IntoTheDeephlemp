package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.aprilTags.AprilTagDetection;
import org.firstinspires.ftc.teamcode.tools.AutoDataStorage;
import org.firstinspires.ftc.teamcode.tools.SetDriveMotors;
import org.firstinspires.ftc.teamcode.tools.Robot;
import org.firstinspires.ftc.teamcode.tools.TelemetryManager;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "TeleOpDrive", group = "Testing")
public class TeleopDrive extends LinearOpMode {
    private SetDriveMotors setDriveMotorsObj;

    private boolean isLiftReset = false;

    Robot robot;
    AprilTagDetection aprilTagDetection;

    public void Setup(){
        TelemetryManager.setTelemetry(telemetry);
        setDriveMotorsObj = new SetDriveMotors(hardwareMap, gamepad1);

        robot = new Robot(hardwareMap, gamepad1, gamepad2, false);

        aprilTagDetection = new AprilTagDetection();
        aprilTagDetection.Setup(hardwareMap, telemetry);

        Robot.clawPitch.setPosition(Robot.clawPitchIntake);
        Robot.clawYaw.setPosition(Robot.clawYawIntake);
        Robot.clawGrip.setPosition(Robot.clawOpen);
        sleep(1000);

        while(!isStarted() && !isStopRequested()){
            if(!isLiftReset){
                Robot.lift.liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                Robot.lift.liftMotor.setPower(-1);
                if(Robot.liftTouchDown.isPressed()){
                    Robot.lift.liftMotor.setPower(0);
                    Robot.lift.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    Robot.lift.liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    isLiftReset = true;
                }
        }
        }
        AutoDataStorage.comingFromAutonomous = false;

    }

    public boolean atRest(){
        return(
                Math.abs(gamepad1.left_stick_y) < setDriveMotorsObj.DEADZONE_MIN_Y &&
                        Math.abs(gamepad1.right_stick_y) < setDriveMotorsObj.DEADZONE_MIN_Y &&
                        Math.abs(gamepad1.left_stick_x) < setDriveMotorsObj.DEADZONE_MIN_X &&
                        Math.abs(gamepad1.right_stick_x) < setDriveMotorsObj.DEADZONE_MIN_X);

    }

    @Override
    public void runOpMode() throws InterruptedException {
        Setup();
        waitForStart();
        while(opModeIsActive()){

            if(!isLiftReset) {
                Robot.lift.liftMotor.setPower(-1);
                if (Robot.liftTouchDown.isPressed()) {
                    Robot.lift.liftMotor.setPower(0);
                    isLiftReset = true;
                }
            }
//            if(drive.isBusy()&& atRest()){
//                drive.update();
//            }
            telemetry.update();
            double horizontal = gamepad1.left_stick_x;
            double vertical = -gamepad1.left_stick_y;
            double turn = gamepad1.right_stick_x;
            boolean goFast = gamepad1.left_bumper;
            boolean emergencyBrakeOverride = gamepad1.right_bumper;
            boolean switchDriveMode = gamepad1.b;
//            if (!drive.isBusy() || !atRest()) {
//                setMotorsObj.driveCommands(hardwareMap, horizontal, vertical, turn, goFast);
//            }

            double distanceToWall = aprilTagDetection.GetDistanceAwayFromTheBackdrop();
            if (emergencyBrakeOverride){
                // A little bit of a monkey patch, but it works to override, because distance 0 corresponds to "too far to detect"
                distanceToWall = 0;
            }
            setDriveMotorsObj.driveCommands(horizontal, vertical, turn, goFast, distanceToWall, switchDriveMode);


            robot.update();

            if(robot.currentState()== robot.outTakingPixels){
                if(Robot.handlerDPad_Left.Pressed()){
                    Robot.clawYaw.setPosition(Robot.clawYawLeft);
                }
                if(Robot.handlerDPad_Down.Pressed()){
                    Robot.clawYaw.setPosition(Robot.clawYawIntake);
                }
                if(Robot.handlerDPad_Right.Pressed()){
                    Robot.clawYaw.setPosition(Robot.clawYawRight);
                }
                if(Robot.handlerRightBumper.Pressed()){
                    Robot.clawGrip.setPosition(Robot.clawOpen);
                }
            }

        }
    }

}