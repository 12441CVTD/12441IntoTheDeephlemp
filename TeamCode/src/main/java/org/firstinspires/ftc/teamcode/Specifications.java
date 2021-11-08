package org.firstinspires.ftc.teamcode;

import com.bravenatorsrobotics.core.RobotSpecifications;
import com.bravenatorsrobotics.drive.FourWheelDrive;
import com.bravenatorsrobotics.drive.MecanumDrive;
import com.bravenatorsrobotics.drive.TwoWheelDrive;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Specifications extends RobotSpecifications {

    public Specifications() {
        super(MecanumDrive.GenerateMotors(
                "fl", true,
                "fr", false,
                "bl", true,
                "br", false
                ), MecanumDrive.class,
                1120, 1, 3.78, 11.9);

        this.useVelocity = true;
        this.maxVelocity = 2460;
        this.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT;
        this.debugModeEnabled = true;
    }

}