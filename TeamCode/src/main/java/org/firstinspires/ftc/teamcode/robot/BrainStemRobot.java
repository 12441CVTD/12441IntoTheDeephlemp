package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import static java.lang.Thread.sleep;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.util.Encoder;

public class BrainStemRobot {

    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor.RunMode currentDrivetrainMode;

    private Telemetry telemetry;
    private OpMode opMode;

    // declare robot components
    public Turret turret;
    public Lift lift;
    public Extension arm;
    public SampleMecanumDrive drive;

    public BrainStemRobot(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        // this.opMode = opMode;

        // instantiate components turret, lift, arm, grabber
        turret  = new Turret(hwMap, telemetry);
        lift    = new Lift(hwMap, telemetry);
        arm     = new Extension(hwMap, telemetry);
        drive   = new SampleMecanumDrive(hwMap);

        telemetry.addData("Robot", " Is Ready");
        telemetry.update();
    }

    public void initializeRobotPosition(){
        lift.initializePosition();
        lift.getToClear();  // Raise lift to clear side panels. This does not clear the arm holding cone.
        arm.getToClear();   // Extend the arm so it clears corner of the robot when swinging
        turret.initializePosition();
        lift.raiseHeightTo(0);


    }
    public void moveTurret(int targetDegrees){
        lift.getToClear();
        arm.getToClear();
        turret.moveTurret(targetDegrees);
    }




}
