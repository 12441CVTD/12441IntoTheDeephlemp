package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/** Stores the Robot's hardware and position.
 *  Also has a "desired state" for mechanism driving.
 */
public class Robot {
    // Robot desired states.

    public enum SlidesState {RETRACTED, LOW, MEDIUM, HIGH, UNREADY, MOVE_UP, MOVE_DOWN, STOPPED}
    public enum ParkingPosition {INSIDE, MIDDLE, OUTSIDE}
    public enum ClawRotatorState {FRONT, SIDE, REAR}
    public enum ClawState {OPEN, CLOSED}

    public static SlidesState desiredSlidesState = SlidesState.UNREADY;
    public ClawRotatorState desiredClawRotatorState;
    public ClawState desiredClawState;

    enum BarcodeScanState {CHECK_SCAN, SCAN}

    public BarcodeScanState barcodeScanState;
    public enum BarcodeScanResult {LEFT, CENTER, RIGHT};

    static final int MAX_BARCODE_ATTEMPTS = 40;                           // How many times to try scanning the barcode before giving up
    static final int MIN_BARCODE_REPEAT = MAX_BARCODE_ATTEMPTS / 2 + 1;

    int numBarcodeAttempts;                                               // Amount of current attempts to scan the barcode
    Map<BarcodeScanResult, Integer> barcodeScanResultMap;                 // An array representing a histogram of the scan results.
    BarcodeScanResult barcodeScanResult;                                  // Represents the final decided barcode state

    public void resetBarcodeScanMap() {
        barcodeScanResultMap = new HashMap<BarcodeScanResult, Integer>() {{
            put(BarcodeScanResult.LEFT, 0);
            put(BarcodeScanResult.CENTER, 0);
            put(BarcodeScanResult.RIGHT, 0);
        }};
    }

    enum MovementMode {NORMAL, FINE, ULTRA_FINE}
    MovementMode movementMode = MovementMode.NORMAL;
    boolean wheelSpeedAdjustment = false;

    HashMap<RobotConfig.DriveMotors, DcMotor> driveMotors = new HashMap<RobotConfig.DriveMotors, DcMotor>();

    // Hardware
    public DcMotor slidesMotor1, slidesMotor2;
    public Servo clawRotator, claw, clawIndicator;
    public DigitalChannel slidesLimitSwitch;

    // Other
    public Telemetry telemetry;
    public ElapsedTime elapsedTime;

    // Positioning
    public PositionManager positionManager;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry, ElapsedTime elapsedTime) {
        this.telemetry = telemetry;
        this.elapsedTime = elapsedTime;
        positionManager = new PositionManager(hardwareMap, telemetry);

        numBarcodeAttempts = 0;
        resetBarcodeScanMap();

        desiredClawRotatorState = ClawRotatorState.FRONT;
        desiredClawState = ClawState.CLOSED;

        slidesMotor1 = hardwareMap.get(DcMotor.class, RobotConfig.MotorNames.get(RobotConfig.Motors.SLIDES_MOTOR_1));
        slidesMotor2 = hardwareMap.get(DcMotor.class, RobotConfig.MotorNames.get(RobotConfig.Motors.SLIDES_MOTOR_2));
        clawRotator = hardwareMap.get(Servo.class, RobotConfig.ServoNames.get(RobotConfig.Servos.CLAW_ROTATOR));
        claw = hardwareMap.get(Servo.class, RobotConfig.ServoNames.get(RobotConfig.Servos.CLAW));
        clawIndicator = hardwareMap.get(Servo.class, RobotConfig.ServoNames.get(RobotConfig.Servos.CLAW_INDICATOR));

        slidesLimitSwitch = hardwareMap.get(DigitalChannel.class, RobotConfig.SwitchNames.get(RobotConfig.Switches.SLIDES_LIMIT));

        for (RobotConfig.DriveMotors motor : RobotConfig.DriveMotors.values()) {
            driveMotors.put(motor, hardwareMap.get(DcMotor.class, RobotConfig.DriveMotorNames.get(motor)));
            Objects.requireNonNull(driveMotors.get(motor)).setDirection(RobotConfig.DriveMotorsDirections.get(motor));
            // TODO: figure out these settings
            Objects.requireNonNull(driveMotors.get(motor)).setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            Objects.requireNonNull(driveMotors.get(motor)).setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//            Objects.requireNonNull(driveMotors.get(motor)).setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            Objects.requireNonNull(driveMotors.get(motor)).setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

//            Objects.requireNonNull(driveMotors.get(motor)).setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        slidesMotor1.setDirection(DcMotor.Direction.FORWARD);
        slidesMotor2.setDirection(DcMotor.Direction.REVERSE);

        if (desiredSlidesState == SlidesState.UNREADY) {//if the slides have yet to be initialised then reset the encoders for the slides and set the slide state to retracted
            this.telemetry.addData("desired string state", desiredSlidesState.toString());
            slidesMotor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            slidesMotor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            slidesMotor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            slidesMotor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            desiredSlidesState = SlidesState.RETRACTED;
        }
        slidesMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slidesMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /** Returns the position of the robot.
     */
    public Position getPosition() {
        return positionManager.position;
    }
}


/** Maps the robot's hardware to their names in the OpMode configuration, and contains any other necessary constants
 *  pertaining to the robot's state.
 */
class RobotConfig {
    enum Switches {SLIDES_LIMIT}
    enum Motors {SLIDES_MOTOR_1, SLIDES_MOTOR_2}
    public enum DriveMotors {REAR_LEFT, REAR_RIGHT, FRONT_LEFT, FRONT_RIGHT};
    enum Servos {CLAW_ROTATOR, CLAW, CLAW_INDICATOR}

    public static final Map<Switches, String> SwitchNames = new HashMap<Switches, String>() {{
        put(Switches.SLIDES_LIMIT, "slides_limit");
    }};
    public static final Map<Motors, String> MotorNames = new HashMap<Motors, String>() {{
        put(Motors.SLIDES_MOTOR_1, "slides_motor_1");
        put(Motors.SLIDES_MOTOR_2, "slides_motor_2");
    }};

    public static final Map<DriveMotors, String> DriveMotorNames = new HashMap<DriveMotors, String>() {{
        put(DriveMotors.REAR_LEFT, "rear_left");
        put(DriveMotors.REAR_RIGHT, "rear_right");
        put(DriveMotors.FRONT_LEFT, "front_left");
        put(DriveMotors.FRONT_RIGHT, "front_right");
    }};

    public static final Map<DriveMotors, DcMotor.Direction> DriveMotorsDirections = new HashMap<DriveMotors, DcMotor.Direction>() {{
        put(DriveMotors.FRONT_LEFT, DcMotor.Direction.REVERSE);
        put(DriveMotors.REAR_LEFT, DcMotor.Direction.REVERSE);
        put(DriveMotors.FRONT_RIGHT, DcMotor.Direction.REVERSE);
        put(DriveMotors.REAR_RIGHT, DcMotor.Direction.REVERSE);
    }};

    public static final Map<Servos, String> ServoNames = new HashMap<Servos, String>() {{
        put(Servos.CLAW_ROTATOR, "claw_rotator");
        put(Servos.CLAW, "claw");
        put(Servos.CLAW_INDICATOR, "claw_indicator");
    }};
}
