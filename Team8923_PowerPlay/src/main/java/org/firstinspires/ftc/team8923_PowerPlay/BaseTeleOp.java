package org.firstinspires.ftc.team8923_PowerPlay;

abstract public class BaseTeleOp extends BaseOpMode {

    private boolean isSlowMode = false;

    private Toggle driveSpeedToggle = new Toggle();

    double driveSpeed = 1.0;

    public void driveRobot() {
        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x;
        double rotationalPower = gamepad1.right_stick_x;

        double angle = Math.toDegrees(Math.atan2(y, x)); // 0 degrees is forward
        double power = calculateDistance(x, y);

        driveMecanum(angle, power, rotationalPower);
    }

    public void driveRobotSpeed() {
        isSlowMode = driveSpeedToggle.toggle(gamepad1.left_bumper);
        if (isSlowMode) {
            driveSpeed = 0.50;
        } else {
            driveSpeed = 1.0;
        }
    }
}


