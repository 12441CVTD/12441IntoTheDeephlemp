package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.renderscript.Sampler;
import android.view.View;

import androidx.collection.CircularArray;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Locale;

import javax.lang.model.element.VariableElement;

@Autonomous(name="WaitAutoBlue2", group="Autonomous")
//@Disabled
public class Test_Subject_4 {private ElapsedTime runtime = new ElapsedTime();
    private DcMotor Ben_S = null;  //left front wheel
    private DcMotor Titus = null;  //right front wheel
    private DcMotor Ben_K = null;  //left back wheel
    private DcMotor Dimensia = null;  //right back wheel
    private DcMotor tower1 = null; //arm motor 1
    private DcMotor tower2 = null; //arm motor 2
    private Servo clawservo = null; //clawservo
    private DcMotor duckspinnerL = null; // the duck spinny thingy
    private DcMotor duckspinnerR = null; // the other duck spinny thingy


    @Override
    public void runOpMode() {
        TelemetryMessage telemetry;
        telemetry.addData("Status", "Ready to run");
        telemetry.addData(Ben_S);
        CircularArray hardwareMap;
        Ben_S = hardwareMap.get(DcMotor.class, "lf");
        Titus = hardwareMap.get(DcMotor.class, "rf");
        Ben_K = hardwareMap.get(DcMotor.class, "lb");
        Dimensia = hardwareMap.get(DcMotor.class, "rb");
        tower1 = hardwareMap.get(DcMotor.class, "tower1");
        tower2 = hardwareMap.get(DcMotor.class, "tower2");
        clawservo = hardwareMap.get(Servo.class, "clawservo");
        duckspinnerL = hardwareMap.get(DcMotor.class, "duckspinnerL");
        duckspinnerR = hardwareMap.get(DcMotor.class, "duckspinnerR");
        double sidemult = -1.0; // Red = 1.0 Blue = -1.0

        Ben_S.setDirection(DcMotor.Direction.FORWARD);
        Titus.setDirection(DcMotor.Direction.REVERSE);
        Ben_K.setDirection(DcMotor.Direction.FORWARD);
        Dimensia.setDirection(DcMotor.Direction.REVERSE);
        tower1.setDirection(DcMotor.Direction.FORWARD);
        tower2.setDirection(DcMotor.Direction.FORWARD);
        wait();
        runtime.reset();


        boolean opModeIsActive;
        while (opModeIsActive) & (runtime.seconds() < 30.0)) {

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);

            sleep(20000); //quick stop

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0.5);
            rf.setPower(0.5);
            lb.setPower(0.5);
            rb.setPower(0.5);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(500); //Go forwards to side of goal

            tower1.setPower(0.5);
            tower2.setPower(0.5);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(1000); //Lower arm

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(500); //quick stop

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0.75);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(500); //Open claw

            tower1.setPower(-0.5);
            tower2.setPower(-0.5);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(800); //Raise arm and close claw

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(500); //quick stop

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(-0.5);
            rf.setPower(-0.5);
            lb.setPower(-0.5);
            rb.setPower(-0.5);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(350); //Go backwards

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(500); //quick stop

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(-0.75);
            rf.setPower(0.75);
            lb.setPower(-1.0);
            rb.setPower(1.0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(675); //turn left

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(500); //quick stop

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0.8);
            rf.setPower(0.8);
            lb.setPower(0.8);
            rb.setPower(0.8);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(950); //Go forward

            tower1.setPower(0);
            tower2.setPower(0);
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
            clawservo.setPosition(0);
            duckspinnerL.setPower(0);
            duckspinnerR.setPower(0);
            sleep(25000); //quick stop


        }
    }

    private void sleep(int i) {
    }

    private class Ben_S {
    }
}
