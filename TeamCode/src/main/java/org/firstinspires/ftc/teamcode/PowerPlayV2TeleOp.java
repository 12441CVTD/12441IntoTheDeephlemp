package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.powerplay.PowerPlayRobot;
import org.firstinspires.ftc.teamcode.powerplayV2.PowerPlayRobotV2;
import org.firstinspires.ftc.teamcode.robotbase.GamepadExEx;


@TeleOp(name = "PowerPlayV2", group = "TeleOP")
public class PowerPlayV2TeleOp extends CommandOpMode {
    private PowerPlayRobotV2 robot;

    @Override
    public void initialize() {
        GamepadExEx driverOp = new GamepadExEx(gamepad1);
        GamepadExEx toolOp = new GamepadExEx(gamepad2);

        robot = new PowerPlayRobotV2(hardwareMap, telemetry, driverOp, toolOp);
    }

    @Override
    public void run() {
        super.run();
        // TODO: Make telemetry subsystem/command and remove this function
        robot.telemetryUpdate();
        robot.dashboardTelemetryUpdate();
    }
}