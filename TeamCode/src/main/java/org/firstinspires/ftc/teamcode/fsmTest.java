package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp
public class fsmTest extends OpMode
{
    public enum State
    {
        INTAKE,
        OUTLOW,
        OUTMID,
        OUTHIGH
    }

    State state = State.INTAKE;

    RevBlinkinLedDriver lights = hardwareMap.get(RevBlinkinLedDriver.class, "lights");
    DcMotor frontLeftMotor = hardwareMap.dcMotor.get("FLeft");
    DcMotor backLeftMotor = hardwareMap.dcMotor.get("BLeft");
    DcMotor frontRightMotor = hardwareMap.dcMotor.get("FRight");
    DcMotor backRightMotor = hardwareMap.dcMotor.get("BRight");
    DcMotor intakeMotor = hardwareMap.dcMotor.get("IntakeSpinner");
    DcMotor slideL = hardwareMap.dcMotor.get("slideL");

    Servo IntakeRaiser = hardwareMap.get(Servo.class, "IntakeRaiser");
    Servo ArmWrist = hardwareMap.get(Servo.class, "ArmWrist");
    Servo PixelGrabberWrist1 = hardwareMap.get(Servo.class, "PixelGrabberWrist1");
    Servo PixelGrabberWrist2 = hardwareMap.get(Servo.class, "PixelGrabberWrist2");
    Servo PixelGrabber = hardwareMap.get(Servo.class, "PixelGrabber");

    IMU imu = hardwareMap.get(IMU.class, "imu");
    IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
            RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD));

    @Override
    public void init()
    {
        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        PixelGrabberWrist2.setDirection(Servo.Direction.REVERSE);
        PixelGrabber.setDirection(Servo.Direction.REVERSE);

        IntakeRaiser.setPosition(0);
        ArmWrist.setPosition(0);
        PixelGrabberWrist1.setPosition(0.21);
        PixelGrabberWrist2.setPosition(0.21);
        PixelGrabber.setPosition(0);

        imu.initialize(parameters);
    }

    @Override
    public void loop()
    {
        switch (state)
        {
            case INTAKE:

            case OUTLOW:

            case OUTMID:

            case OUTHIGH:

            default:
                // should never be reached, as liftState should never be null
                state = State.INTAKE;
        }

        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x;
        double rx = gamepad1.right_stick_x;

        if (gamepad1.options)
        {
            imu.resetYaw();
        }

        double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
        double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);
        rotX = rotX * 1.1;
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);
        double frontLeftPower = (rotY + rotX + rx) / denominator;
        double backLeftPower = (rotY - rotX + rx) / denominator;
        double frontRightPower = (rotY - rotX - rx) / denominator;
        double backRightPower = (rotY + rotX - rx) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);
    }
}