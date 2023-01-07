package org.firstinspires.ftc.teamcode.Robots;

import static org.firstinspires.ftc.teamcode.Components.Claw.ClawStates.CLAW_CLOSED;
import static org.firstinspires.ftc.teamcode.Components.Claw.ClawStates.CLAW_CLOSING;
import static org.firstinspires.ftc.teamcode.Components.Claw.ClawStates.CLAW_OPEN;
import static org.firstinspires.ftc.teamcode.Components.Lift.LiftConstants.LIFT_HIGH_JUNCTION;
import static org.firstinspires.ftc.teamcode.Components.Lift.LiftConstants.LIFT_MED_JUNCTION;
import static org.firstinspires.ftc.teamcode.Components.LiftArm.liftArmStates.ARM_INTAKE;
import static org.firstinspires.ftc.teamcode.Components.LiftArm.liftArmStates.ARM_OUTTAKE;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Components.Aligner;
import org.firstinspires.ftc.teamcode.Components.CV.CVMaster;
import org.firstinspires.ftc.teamcode.Components.Claw;
import org.firstinspires.ftc.teamcode.Components.ClawExtension;
import org.firstinspires.ftc.teamcode.Components.Field;
import org.firstinspires.ftc.teamcode.Components.Lift;
import org.firstinspires.ftc.teamcode.Components.LiftArm;
import org.firstinspires.ftc.teamcode.Components.RFModules.Devices.RFGamepad;
import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.roadrunner.util.IMU;

import java.util.ArrayList;

public class PwPRobot extends BasicRobot {
    private Aligner aligner = null;
    private Claw claw = null;
    private LiftArm liftArm = null;
    private ClawExtension clawExtension = null;
    private Lift lift = null;
    private RFGamepad gp = null;
    public Field field = null;
    public CVMaster cv = null;
    public SampleMecanumDrive roadrun = null;
    private IMU imu = null;
    private ArrayList<Integer> seq;
    private boolean regularDrive = true;
//    private LEDStrip leds = null;


    public PwPRobot(LinearOpMode opMode, boolean p_isTeleop) {
        super(opMode, p_isTeleop);
        roadrun = new SampleMecanumDrive(opMode.hardwareMap);
        cv = new CVMaster();
        gp = new RFGamepad();
//        imu = new IMU();
        field = new Field(roadrun, cv, imu, gp);
//        aligner = new Aligner();
        claw = new Claw();
        liftArm = new LiftArm();
//        clawExtension = new ClawExtension();
        lift = new Lift();
//        leds = new LEDStrip();
    }
//    com.qualcomm.ftcrobotcontroller I/art: Waiting for a blocking GC Alloc
//2023-01-05 14:19:08.807 9944-10985/com.qualcomm.ftcrobotcontroller I/art: Alloc sticky concurrent mark sweep GC freed 340391(7MB) AllocSpace objects, 0(0B) LOS objects, 20% free, 43MB/54MB, paused 2.675ms total 197.819ms
//2023-01-05 14:19:08.807 9944-12811/com.qualcomm.ftcrobotcontroller I/art: WaitForGcToComplete blocked for 689.441ms for cause Alloc
//2023-01-05 14:19:08.807 9944-12811/com.qualcomm.ftcrobotcontroller I/art: Starting a blocking GC Alloc

    public void stop() {
        lift.setLiftPower(0.0);
        logger.log("/RobotLogs/GeneralRobot", "program stoped");
    }

    public void delay(double p_delay) {
        queuer.addDelay(p_delay);
    }

    public void waitForFinish(int condition) {
        queuer.waitForFinish(condition);
    }

    public void waitForFinish() {
        queuer.waitForFinish();
    }

    public void autoAim() {
        if (queuer.queue(false, !roadrun.isBusy())) {
            if (!roadrun.isBusy() && field.lookingAtPole()) {
                Trajectory trajectory = roadrun.trajectoryBuilder(roadrun.getPoseEstimate()).lineToLinearHeading(
                        field.getDropPosition()).build();
                roadrun.followTrajectoryAsync(trajectory);
            }
        }
    }

    public void updateLiftArmStates() {
        liftArm.updateLiftArmStates();
    }

    public void teleAutoAim(Trajectory trajectory) {
        roadrun.followTrajectoryAsync(trajectory);
    }

    public void followTrajectorySequenceAsync(TrajectorySequence trajectorySequence) {
        if (queuer.queue(false, !roadrun.isBusy())) {
            if (!roadrun.isBusy()) {
                roadrun.followTrajectorySequenceAsync(trajectorySequence);
            }
        }
    }

    public void followTrajectoryAsync(Trajectory trajectory) {
        if (queuer.queue(false, !roadrun.isBusy())) {
            if (!roadrun.isBusy()) {
                roadrun.followTrajectoryAsync(trajectory);
            }
        }
    }

    public void followTrajectorySequenceAsync(TrajectorySequence trajectory, boolean clawClosed) {
        if (queuer.queue(false, !roadrun.isBusy() || CLAW_CLOSING.getStatus())) {
            if (!roadrun.isBusy()) {
                roadrun.followTrajectorySequenceAsync(trajectory);
            }
        }
    }

    public void setFirstLoop(boolean value) {
        queuer.setFirstLoop(value);
    }

    public void openClaw() {
        openClaw(true);
    }

    public void openClaw(boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, CLAW_OPEN.getStatus())) {
            claw.updateClawStates();
            claw.openClaw();
        }
    }

    public void closeClaw() {
        closeClaw(true);
    }

    public void closeClaw(boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, CLAW_CLOSED.getStatus())) {
            claw.updateClawStates();
            claw.closeClawRaw();
        }
    }

    public void toggleClawPosition() {
        toggleClawPosition(true);
    }

    public void toggleClawPosition(boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, op.getRuntime() > claw.clawServoLastSwitchTime +
                claw.CLAW_SERVO_SWITCH_TIME)) {
            claw.toggleClawPosition();
        }
    }

    public boolean isConeReady() {
        return claw.isConeReady();
    }

    public void updateClawStates() {
        claw.updateClawStates();
    }

    public void liftToPosition(Lift.LiftConstants targetJunction) {
        if (queuer.queue(true, lift.isDone() && abs(lift.getLiftPosition() - targetJunction.getValue()) < 20)) {
            lift.liftToPosition(targetJunction);
        }
    }

    public void wideClaw() {
        if (queuer.queue(true, claw.isClawWide())) {
            claw.wideClaw();
        }
    }

    public void liftToTargetAuto() {
        lift.liftToTargetAuto();
    }

    public void liftToPosition(int tickTarget) {
        if (queuer.queue(true, lift.isDone() && abs(lift.getLiftPosition() - tickTarget) < 20)) {
            lift.liftToPosition(tickTarget);
        }
    }

    public void liftToPosition(int tickTarget, boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, lift.isDone() && abs(lift.getLiftPosition() - tickTarget) < 20)) {
            lift.liftToPosition(tickTarget);
        }
    }

    public void setLiftPower(double p_power) {
        lift.setLiftPower(p_power);
    }

    public void lowerLiftArmToIntake() {
        lowerLiftArmToIntake(true);
    }

    public void lowerLiftArmToIntake(boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, ARM_INTAKE.getStatus())) {

            liftArm.updateLiftArmStates();
            liftArm.lowerLiftArmToIntake();
        }
    }

    public void cycleLiftArmToCycle(boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, liftArm.isCylce())) {

            liftArm.updateLiftArmStates();
            liftArm.cycleLiftArmToCylce();
        }
    }

    public void raiseLiftArmToOuttake() {
        raiseLiftArmToOuttake(true);
    }

    public void raiseLiftArmToOuttake(boolean p_asynchronous) {
        if (queuer.queue(p_asynchronous, ARM_OUTTAKE.getStatus())) {

            liftArm.updateLiftArmStates();
            liftArm.raiseLiftArmToOuttake();
        }
    }

    public void toggleArmPosition() {
        if (queuer.queue(true, op.getRuntime() > liftArm.liftArmServoLastSwitchTime +
                liftArm.LIFT_ARM_SERVO_SWITCH_TIME)) {

            liftArm.liftArmServoLastSwitchTime = op.getRuntime();
            liftArm.toggleArmPosition();
        }
    }

    //    public void spinAlignerIntake() {
//        if (queuer.queue(true, alignerSensor.getSensorDistance() <
//                aligner.CONE_IN_ALIGNER_DISTANCE)) {
//
//            aligner.spinAlignerIntake();
//        }
//    }
//
//    public void stopAlignerIntake() {
//        if (queuer.queue(true, op.getRuntime() > aligner.alignerMotorLastStopTime +
//                aligner.ALIGNER_MOTOR_STOP_TIME)) {
//
//            aligner.stopAlignerIntake();
//        }
//    }
//
//    public void reverseAlignerIntake() {
//        if (queuer.queue(true, alignerSensor.getSensorDistance() >
//                aligner.CONE_OUT_OF_ALIGNER_DISTANCE)) {
//
//            aligner.reverseAlignerIntake();
//        }
//    }
//    public void reverseAlignerIntake() {
//        if (queuer.queue(true, !aligner.isConeInAligner())){
//
//            aligner.reverseAlignerIntake();
//        }
//    }
    public void liftToTarget() {
        lift.liftToTarget();
    }

    public void setLiftTarget(double p_target) {
        lift.setLiftTarget(p_target);
    }

    public void setLiftVelocity(double velocity) {
        lift.setLiftVelocity(velocity);
    }


    private boolean mecZeroLogged = false;
    private boolean progNameLogged = false;

    public void setPoseEstimate(Pose2d newPose) {
        roadrun.setPoseEstimate(newPose);
        imu.setAngle(newPose.getHeading());
    }

    public void teleOp() {
//        if (progNameLogged == false) {
//            logger.log("/RobotLogs/GeneralRobot", "PROGRAM RUN: PwPTeleOp", false);
//            progNameLogged = true;
//        }
        gp.readGamepad(op.gamepad2.y, "gamepad2_y", "Status");
        gp.readGamepad(op.gamepad1.x, "gamepad1_x", "Status");
        boolean isY = gp.readGamepad(op.gamepad1.y, "gamepad1_y", "Status");
                gp.readGamepad(op.gamepad2.a, "gamepad1_a", "Status");
        gp.readGamepad(op.gamepad2.b, "gamepad1_b", "Status");
        gp.readGamepad(op.gamepad1.left_stick_y, "gamepad1_left_stick_y", "Value");
        gp.readGamepad(op.gamepad1.left_stick_x, "gamepad1_left_stick_x", "Value");
        gp.readGamepad(op.gamepad1.right_stick_x, "gamepad1_right_stick_x", "Value");
        gp.readGamepad(op.gamepad2.left_trigger, "gamepad2_left_trigger", "Value");
        gp.readGamepad(op.gamepad2.right_trigger, "gamepad2_right_trigger", "Value");
        gp.readGamepad(op.gamepad2.right_bumper, "gamepad2_right_bumper", "Status");
        if (isY) {
            regularDrive = !regularDrive;
        }

        //omnidirectional movement + turning

        if (op.gamepad2.y) {
            lift.setLiftTarget(LIFT_HIGH_JUNCTION.getValue());
            liftArm.raiseLiftArmToOuttake();
        }
        if (op.gamepad2.b) {
            lift.setLiftTarget(LIFT_MED_JUNCTION.getValue());
            liftArm.raiseLiftArmToOuttake();
        }
        if (op.gamepad2.a) {
            liftArm.lowerLiftArmToIntake();
            lift.setLiftTarget(0);
        }

        if (op.gamepad1.dpad_left && op.gamepad2.dpad_left) {
            lift.resetEncoder();
        }
        //manual lift up/down
        if (op.gamepad1.dpad_down && op.gamepad2.dpad_down) {
            lift.setLiftRawPower((op.gamepad2.right_trigger - op.gamepad2.left_trigger) / 3);
        } else if (op.gamepad2.right_trigger > 0.1 || op.gamepad2.left_trigger > 0.1) {
            lift.setLiftPower((op.gamepad2.right_trigger - op.gamepad2.left_trigger));
            lift.updateLastManualTime();
        } else {
//            lift.setLiftPower(0);
            lift.liftToTarget();
        }

        if (op.gamepad2.dpad_down) {
            lift.iterateConeStackDown();
            liftArm.cycleLiftArmToCylce();
            logger.log("/RobotLogs/GeneralRobot", "Lift,iterateConeStackDown(),Cone Stack Lowered by 1", true);
        }
        if (op.gamepad2.dpad_up) {
            lift.iterateConeStackUp();
            liftArm.cycleLiftArmToCylce();
            logger.log("/RobotLogs/GeneralRobot", "Lift,iterateConeStackUp(),Cone Stack Raised by 1", true);
        }
        //when not manual lifting, automate lifting

//        if (field.lookingAtPole() && op.gamepad1.dpad_up && !roadrun.isBusy()) {
//            field.updateTrajectory();
//            teleAutoAim(field.getTrajectory());
//        } else if (roadrun.isBusy()) {
//            //nothin
//        } else {
        double[] vals = {op.gamepad1.left_stick_x, op.gamepad1.left_stick_y, op.gamepad1.right_stick_x};
        double[] minBoost = {0.1, 0.1, 0.05};
        boolean boosted = false;
        if (abs(op.gamepad1.left_stick_x) < 0.15) {
            minBoost[0] = 0;
        } else {
            boosted = true;
        }
        if (op.gamepad1.left_stick_x == 0) {
            vals[0] = 0.0001;
        }
        if (abs(op.gamepad1.left_stick_y) < 0.04) {
            minBoost[1] = 0;
        } else {
            boosted = true;
        }
        if (op.gamepad1.left_stick_y == 0) {
            vals[1] = 0.0001;

        }
        if (abs(op.gamepad1.right_stick_x) < 0.03) {
            minBoost[2] = 0;

            //48.8,36.6,
        } else {
            boosted = true;
        }
        if (op.gamepad1.right_stick_x == 0) {
            vals[2] = 0.0001;

        }
        if (!roadrun.isBusy() || boosted) {
            if (roadrun.isBusy()) {
                field.breakAutoTele();
            }
            if (regularDrive) {
                roadrun.setWeightedDrivePower(
                        new Pose2d(
                                abs(vals[1] - 0.0001) / -vals[1] * (minBoost[1] + 0.5 * abs(vals[1]) + 0.15 * pow(abs(vals[1]), 3)),
                                abs(vals[0] - 0.0001) / -vals[0] * (minBoost[0] + 0.5 * abs(vals[0]) + 0.15 * pow(abs(vals[0]), 3)),
                                abs(vals[2] - 0.0001) / -vals[2] * (minBoost[2] + 0.5 * abs(vals[2]) + 0.15 * pow(abs(vals[2]), 3))
                        )
                );
            } else {
                Vector2d input = new Vector2d(abs(vals[1] - 0.0001) / -vals[1] * (minBoost[1] + 0.5 * abs(vals[1]) + 0.15 * pow(abs(vals[1]), 3)),
                        abs(vals[0] - 0.0001) / -vals[0] * (minBoost[0] + 0.5 * abs(vals[0]) + 0.15 * pow(abs(vals[0]), 3)));
                input = input.rotated(-roadrun.getPoseEstimate().getHeading()-Math.toRadians(90));
                roadrun.setWeightedDrivePower(
                        new Pose2d(input.getX(),
                                input.getY(),
                                abs(vals[2] - 0.0001) / -vals[2] * (minBoost[2] + 0.5 * abs(vals[2]) + 0.15 * pow(abs(vals[2]), 3)))
                );
            }
        }
        if ((-op.gamepad1.left_stick_y * 0.7 == -0) && (-op.gamepad1.left_stick_x == -0) && (-op.gamepad1.right_stick_x * 0.8 == -0) && (mecZeroLogged == false)) {
            logger.log("/RobotLogs/GeneralRobot", "Mecanum,setWeightedDriverPower(Pose2d),Mec = 0 | 0 | 0", true);
            mecZeroLogged = true;
        } else if ((-op.gamepad1.left_stick_y * 0.7 == -0) && (-op.gamepad1.left_stick_x == -0) && (-op.gamepad1.right_stick_x * 0.8 == -0) && (mecZeroLogged == true)) {
            //nutting
        } else {
            logger.log("/RobotLogs/GeneralRobot", "Mecanum,setWeightedDriverPower(Pose2d),Mec =  " + -op.gamepad1.left_stick_y * 0.7 + " | " + -op.gamepad1.left_stick_x + " | " + -op.gamepad1.right_stick_x * 0.8, true);
            mecZeroLogged = false;
        }
        //toggle automate lift target to higher junc
        if (op.gamepad2.dpad_up) {
            lift.toggleLiftPosition(1);
        }
        //toggle automate lift target to lower junc
        if (op.gamepad2.dpad_down) {
            lift.toggleLiftPosition(-1);
        }
        //toggle liftArm position
        if (op.gamepad2.right_bumper) {
            if (ARM_OUTTAKE.getStatus()) {
                liftArm.lowerLiftArmToIntake();

            } else {
                liftArm.raiseLiftArmToOuttake();
            }
        }
//        field.closestDropPosition(false);
        if (op.gamepad1.right_bumper) {
            if (CLAW_CLOSED.getStatus()) {
                claw.setLastOpenTime(op.getRuntime());
                claw.openClaw();
            } else {
                claw.closeClawRaw();
            }
        }
        claw.closeClaw();
        if (op.getRuntime() - claw.getLastTime() > .4 && op.getRuntime() - claw.getLastTime() < .7 && CLAW_CLOSED.getStatus()) {
            liftArm.raiseLiftArmToOuttake();
        }


        //will only close when detect cone
        //claw.closeClaw
        op.telemetry.addData("stacklevel", lift.getStackLevel());
        if (gp.updateSequence()) {
            field.autoMovement();
        }
        roadrun.update();
        field.updateMoves(false);
        liftArm.updateLiftArmStates();
        claw.updateClawStates();
        lift.updateLiftStates();
//            logger.log("/RobotLogs/GeneralRobot", seq.toString(), false);
        //USE THE RFGAMEPAD FUNCTION CALLED getSequence(), WILL RETURN ARRAYLIST OF INTS:
        //1 = down, 2 = right, 3 = up, 4 = left
//        }
    }
}