package org.firstinspires.ftc.teamcode.Robots;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Components.Logger;
import org.firstinspires.ftc.teamcode.Components.Queuer;

public class BasicRobot{
    public static Logger logger;
    public static LinearOpMode op = null;
    protected Queuer queuer = null;
    public static boolean isTeleop;
    public static FtcDashboard dashboard;
    public BasicRobot(LinearOpMode opMode, boolean p_isTeleop){
        op = opMode;
        logger = new Logger();
        logger.createFile("/RobotLogs/GeneralRobot", "Runtime    Component               " +
                "Function                        Action");
        logger.createFile("gamepad", "Value Name Time");
        dashboard = FtcDashboard.getInstance();
        dashboard.setTelemetryTransmissionInterval(25);
        queuer = new Queuer();
        isTeleop = p_isTeleop;

    }
    public void resetQueuer() {
        queuer.resetQueuer();
    }
}
