package org.firstinspires.ftc.teamcode;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.List;

/*
 * This OpMode illustrates the concept of driving a path based on encoder counts.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: RobotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forward, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backward for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This method assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@Autonomous(name = "Red Front", group = "CenterStage", preselectTeleOp = "Full")
public class CSAutoRedFront extends CSMethods {
    @Override
    public void runOpMode() {
        setup(true);

        // Main code
        //dropCarWash();
        drive(13.5);
        //drive(1);
        List<Recognition> pixels = detectProp();
        while (opModeIsActive()) {
            telemetry.addData("Team Prop Detection", pixels);
        }
        double pixel_distance = 3.0;
        //*
        if (pixels.size() > 0) {
            drive(pixel_distance);
            ejectPixel();
            drive(-pixel_distance);
            turn(90);
        } else {
            turn(-30);
            pixels = detectProp();
            if (pixels.size() > 0) {
                drive(pixel_distance);
                ejectPixel();
                drive(-pixel_distance);
                turn(30 + 90);
            } else {
                turn(60);
                drive(pixel_distance);
                ejectPixel();
                drive(-pixel_distance);
                turn(-30 + 90);
            }
        }
        //*/
        //ejectPixel();
        turn(60);
        drive(100);
        //ejectPixel();

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);  // Pause to display final telemetry message.
    }

}
