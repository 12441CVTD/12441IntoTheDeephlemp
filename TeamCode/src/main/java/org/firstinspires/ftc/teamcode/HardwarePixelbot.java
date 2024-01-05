package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.MILLIAMPS;
import static java.lang.Thread.sleep;

import android.graphics.Color;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;
import org.firstinspires.ftc.teamcode.HardwareDrivers.MaxSonarI2CXL;

/*
 * Hardware class for goBilda robot (12"x15" chassis with 96mm/3.8" goBilda mecanum wheels)
 */
public class HardwarePixelbot
{
    //====== REV CONTROL/EXPANSION HUBS =====
    LynxModule controlHub;
    LynxModule expansionHub;
    public double controlHubV   = 0.0; // Voltage supply of the control hub
    public double expansionHubV = 0.0; // Voltage supply of the expansion hub

    //====== INERTIAL MEASUREMENT UNIT (IMU) =====
    protected BNO055IMU imu    = null;
    public double headingAngle = 0.0;
    public double tiltAngle    = 0.0;

    //====== MECANUM DRIVETRAIN MOTORS (RUN_USING_ENCODER) =====
    protected DcMotorEx frontLeftMotor     = null;
    public int          frontLeftMotorTgt  = 0;       // RUN_TO_POSITION target encoder count
    public int          frontLeftMotorPos  = 0;       // current encoder count
    public double       frontLeftMotorVel  = 0.0;     // encoder counts per second
    public double       frontLeftMotorAmps = 0.0;     // current power draw (Amps)

    protected DcMotorEx frontRightMotor    = null;
    public int          frontRightMotorTgt = 0;       // RUN_TO_POSITION target encoder count
    public int          frontRightMotorPos = 0;       // current encoder count
    public double       frontRightMotorVel = 0.0;     // encoder counts per second
    public double       frontRightMotorAmps= 0.0;     // current power draw (Amps)

    protected DcMotorEx rearLeftMotor      = null;
    public int          rearLeftMotorTgt   = 0;       // RUN_TO_POSITION target encoder count
    public int          rearLeftMotorPos   = 0;       // current encoder count
    public double       rearLeftMotorVel   = 0.0;     // encoder counts per second
    public double       rearLeftMotorAmps  = 0.0;     // current power draw (Amps)

    protected DcMotorEx rearRightMotor     = null;
    public int          rearRightMotorTgt  = 0;       // RUN_TO_POSITION target encoder count
    public int          rearRightMotorPos  = 0;       // current encoder count
    public double       rearRightMotorVel  = 0.0;     // encoder counts per second
    public double       rearRightMotorAmps = 0.0;     // current power draw (Amps)

    public final static double MIN_DRIVE_POW      = 0.03;    // Minimum speed to move the robot
    public final static double MIN_TURN_POW       = 0.03;    // Minimum speed to turn the robot
    public final static double MIN_STRAFE_POW     = 0.04;    // Minimum speed to strafe the robot
    protected double COUNTS_PER_MOTOR_REV  = 28.0;    // goBilda Yellow Jacket Planetary Gear Motor Encoders
//  protected double DRIVE_GEAR_REDUCTION  = 26.851;  // goBilda 26.9:1 (223rpm) gear ratio with 1:1 HDT5 pully/belt
    protected double DRIVE_GEAR_REDUCTION  = 19.203;  // goBilda 19.2:1 (312rpm) gear ratio with 1:1 HDT5 pully/belt
    protected double MECANUM_SLIPPAGE      = 1.01;    // one wheel revolution doesn't achieve 6" x 3.1415 of travel.
    protected double WHEEL_DIAMETER_INCHES = 3.77953; // (96mm) -- for computing circumference
    protected double COUNTS_PER_INCH       = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION * MECANUM_SLIPPAGE) / (WHEEL_DIAMETER_INCHES * 3.1415);
    // The math above assumes motor encoders.  For REV odometry pods, the counts per inch is different
//  protected double COUNTS_PER_INCH2      = 1738.4;  // REV ENCODERS = 8192 counts-per-rev / (1.5" omni wheel * PI)
    protected double COUNTS_PER_INCH2      = 336.878; // goBilda Odometry Pod = 2000 counts-per-rev / (1.8897" omni wheel * PI)

    // Pixel collector/transporter motor
    protected DcMotorEx collectorMotor     = null;

    public double  COLLECTOR_MOTOR_POWER = 1.00;  // Speed of the collector motor when we run
    public double  COLLECTOR_EJECT_POWER = 0.60;  // Speed of the collector motor for autonomous

    // Viper slide motors (Y power cable to drive both motors from one port; single encoder cable on left motor
    protected DcMotorEx viperMotors = null;
    public int          viperMotorsPos  = 0;       // current encoder count
    public double       viperMotorsVel  = 0.0;     // encoder counts per second
    public double       viperMotorsPwr  = 0.0;     // current power setting
    public double       viperMotorsAmps = 0.0;     // current power draw (Amps)

    public boolean      viperMotorAutoMove = false;  // have we commanded an automatic lift movement?
    public double  VIPER_RAISE_POWER =  1.000; // Motor power used to RAISE viper slide
    public double  VIPER_HOLD_POWER  =  0.001; // Motor power used to HOLD viper slide at current height
    public double  VIPER_LOWER_POWER = -0.250; // Motor power used to LOWER viper slide
    public int     VIPER_EXTEND_ZERO = 0;    // Encoder count when fully retracted (may need to be adjustable??)
    public int     VIPER_EXTEND_BIN  = 140;  // Encoder count when raised to just above the bin (safe to rotate)
    public int     VIPER_EXTEND_LOW  = 250;  // Encoder count when raised to lowest possible scoring position (200)
    public int     VIPER_EXTEND_MID  = 325;  // Encoder count when raised to medium scoring height (350)
    public int     VIPER_EXTEND_HIGH = 400;  // Encoder count when raised to upper scoring height (500)
    public int     VIPER_EXTEND_FULL = 580;  // Encoder count when fully extended (never exceed this count!)

    //====== SERVO FOR COLLECTOR ARM ====================================================================
    public Servo  collectorServo       = null;

    public double COLLECTOR_SERVO_GROUND = 0.830;
    public double COLLECTOR_SERVO_STACK2 = 0.780;
    public double COLLECTOR_SERVO_STACK3 = 0.750;
    public double COLLECTOR_SERVO_STACK4 = 0.710;
    public double COLLECTOR_SERVO_STACK5 = 0.680;
    public double COLLECTOR_SERVO_RAISED = 0.300;  // almost vertical
    public double COLLECTOR_SERVO_STORED = 0.230;  // past this hits the collector motor

    public double collectorServoSetPoint = COLLECTOR_SERVO_STORED;

    public int collectorServoIndex = 1;

    public boolean collectorServoChanged = false;

    //====== COLOR/DISTANCE SENSORS FOR PIXEL BIN ========================================================
    private NormalizedColorSensor pixel1ColorSensor = null;  // lower
    private NormalizedColorSensor pixel2ColorSensor = null;  // upper

    private NormalizedRGBA  pixelRGBA;
    private float[]         pixelHSV = new float[3];

    public double pixel1Hue; // lower
    public double pixel2Hue; // upper
    enum PixelColorsEnum {
        EMPTY,
        WHITE,
        YELLOW,
        GREEN,
        PURPLE,
        UNKNOWN
    }
    public PixelColorsEnum pixel1Color = PixelColorsEnum.EMPTY;  // lower
    public PixelColorsEnum pixel2Color = PixelColorsEnum.EMPTY;  // upper
    private DistanceSensor pixe1DistanceSensor = null;        // lower
    private DistanceSensor pixe2DistanceSensor = null;        // upper

    public double pixel1Distance; // lower
    public double pixel2Distance; // upper

    //====== SERVOS FOR PIXEL FINGERS ====================================================================
    public Servo  pushServo = null;
    public double PUSH_SERVO_INIT = 0.470;
    public double PUSH_SERVO_SAFE = 0.470;   // Safe to raise/lower the lift
    public double PUSH_SERVO_GRAB = 0.540;   // Retract linkage servo back behind the pixel bin
    public double PUSH_SERVO_DROP = 0.890;   // Extend linkage servo and finger assembly toward the Backdrop

    public Servo  wristServo = null;
    public double WRIST_SERVO_INIT = 0.450;   // higher is counter-clockwise
    public double WRIST_SERVO_GRAB = 0.450;
    public double WRIST_SERVO_DROP = 0.810;

	public Servo  fingerServo1 = null;  // TOP (bin) or RIGHT (backdrop)
    public double FINGER1_SERVO_DROP = 0.500;
    public double FINGER1_SERVO_GRAB = FINGER1_SERVO_DROP + 0.242; // 0.742

	public Servo  fingerServo2 = null;  // BOTTOM (bin) or LEFT (backdrop)
    public double FINGER2_SERVO_DROP = 0.480;
    public double FINGER2_SERVO_GRAB = FINGER2_SERVO_DROP + 0.262;  // 0.742

    //====== ODOMETRY ENCODERS (encoder values only!) =====
    protected DcMotorEx rightOdometer      = null;
    public int          rightOdometerCount = 0;       // current encoder count
    public int          rightOdometerPrev  = 0;       // previous encoder count

    protected DcMotorEx leftOdometer       = null;
    public int          leftOdometerCount  = 0;       // current encoder count
    public int          leftOdometerPrev   = 0;       // previous encoder count

    protected DcMotorEx strafeOdometer      = null;
    public int          strafeOdometerCount = 0;      // current encoder count
    public int          strafeOdometerPrev  = 0;      // previous encoder count

    //Ultrasonic sensors
    private MaxSonarI2CXL sonarRangeF = null;

    /* local OpMode members. */
    protected HardwareMap hwMap = null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    public HardwarePixelbot(){
    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap, boolean isAutonomous ) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Configure REV control/expansion hubs for bulk reads (faster!)
        for (LynxModule module : hwMap.getAll(LynxModule.class)) {
            if(module.isParent()) {
                controlHub = module;
            } else {
                expansionHub = module;
            }
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        // Define and Initialize drivetrain motors
        frontRightMotor = hwMap.get(DcMotorEx.class,"FrontRight"); // Control Hub  port 0 (forward)
        rearRightMotor  = hwMap.get(DcMotorEx.class,"RearRight");  // Control Hub  port 1 (forward)
        frontLeftMotor  = hwMap.get(DcMotorEx.class,"FrontLeft");  // Expansion Hub port 0 (REVERSE)
        rearLeftMotor   = hwMap.get(DcMotorEx.class,"RearLeft");   // Expansion Hub port 1 (REVERSE)

        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        rearRightMotor.setDirection(DcMotor.Direction.FORWARD);
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);  // goBilda fwd/rev opposite of Matrix motors!
        rearLeftMotor.setDirection(DcMotor.Direction.REVERSE);

        // Set all drivetrain motors to zero power
        driveTrainMotorsZero();

        // Set all drivetrain motors to run with encoders.
        frontLeftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //Set all drivetrain motors to brake when at zero power
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        collectorMotor  = hwMap.get(DcMotorEx.class,"CollectorMotor");  // Expansion Hub port 2 (REVERSE)
        collectorMotor.setDirection(DcMotor.Direction.REVERSE);
        collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        collectorMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        collectorMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        collectorMotor.setPower( 0.0 );

        collectorServo = hwMap.servo.get("collectorServo");      // servo port 4 (Expansion Hub)
        collectorServo.setPosition(collectorServoSetPoint);

        viperMotors = hwMap.get(DcMotorEx.class,"ViperMotors");  // Control Hub port 2
        viperMotors.setDirection(DcMotor.Direction.FORWARD);
        viperMotors.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        viperMotors.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        viperMotors.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        viperMotors.setPower( 0.0 );

        rightOdometer  = hwMap.get(DcMotorEx.class,"OdomRight");   // Control Hub port 3
        rightOdometer.setDirection(DcMotor.Direction.FORWARD);
        rightOdometer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightOdometer.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightOdometer.setPower( 0.0 );

        // "OdomLeft" = overloaded onto CollectorMotor on Expansion Hub port 2

        strafeOdometer = hwMap.get(DcMotorEx.class,"OdomStrafe");  // Expansion Hub port 3
        strafeOdometer.setDirection(DcMotor.Direction.FORWARD);
        strafeOdometer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        strafeOdometer.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        strafeOdometer.setPower( 0.0 );

        /*--------------------------------------------------------------------------------------------*/
        pixel1ColorSensor = hwMap.get(NormalizedColorSensor.class, "BinLowerSensor");  // ControlHub I2C Bus1
        pixel1ColorSensor.setGain(3.0f);
        pixe1DistanceSensor = (DistanceSensor) pixel1ColorSensor;

        pixel2ColorSensor = hwMap.get(NormalizedColorSensor.class, "BinUpperSensor");  // ControlHub I2C Bus2
        pixel2ColorSensor.setGain(3.0f);
        pixe2DistanceSensor = (DistanceSensor) pixel2ColorSensor;

        /*--------------------------------------------------------------------------------------------*/
        pushServo = hwMap.servo.get("ElbowServo");           // servo port 0 (Expansion Hub)
        pushServo.setPosition(PUSH_SERVO_INIT);

        wristServo = hwMap.servo.get("WristServo");           // servo port 1 (Expansion Hub)
        wristServo.setPosition(WRIST_SERVO_INIT);

        fingerServo1 = hwMap.servo.get("Finger1Servo");       // servo port 2 (Expansion Hub)
        fingerServo1.setPosition(FINGER1_SERVO_DROP);

        fingerServo2 = hwMap.servo.get("Finger2Servo");       // servo port 3 (Expansion Hub)
        fingerServo2.setPosition(FINGER2_SERVO_DROP);

        // Initialize REV Control Hub IMU
        initIMU();

//      sonarRangeF = hwMap.get( MaxSonarI2CXL.class, "distance" );

    } /* init */

    /*--------------------------------------------------------------------------------------------*/
    public void initIMU()
    {
        // Define and initialize REV Expansion Hub IMU
        BNO055IMU.Parameters imu_params = new BNO055IMU.Parameters();
        imu_params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu_params.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imu_params.calibrationDataFile = "BNO055IMUCalibration.json"; // located in FIRST/settings folder
        imu_params.loggingEnabled = false;
        imu_params.loggingTag = "IMU";
        imu_params.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hwMap.get(BNO055IMU.class, "imu");
        imu.initialize( imu_params );
    } // initIMU()

    /*--------------------------------------------------------------------------------------------*/
    public double headingIMU()
    {
        Orientation angles = imu.getAngularOrientation( AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES );
        headingAngle = angles.firstAngle;
        tiltAngle = angles.secondAngle;
        return -headingAngle;  // degrees (+90 is CW; -90 is CCW)
    } // headingIMU

    /*--------------------------------------------------------------------------------------------*/
    public double headingIMUradians()
    {
        Orientation angles = imu.getAngularOrientation( AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS );
        double heading = -(double)angles.firstAngle;
        return heading;  // radians (+pi is CW; -pi is CCW)
    } // headingIMUradians

    /*--------------------------------------------------------------------------------------------*/
    public boolean isPwrRampingDown( double priorPwr, double thisPwr ) {
        // Check for POSITIVE power ramp-down (0.30 to 0.20)
        boolean positiveRampDown = ((priorPwr >= 0.0) && (thisPwr >= 0.0) && (thisPwr < priorPwr));
        // Check for NEGATIVE power ramp-down (-0.30 to -0.20)
        boolean negativeRampDown = ((priorPwr <= 0.0) && (thisPwr <= 0.0) && (thisPwr > priorPwr));
        // If either is true, we're ramping down power toward zero
        return positiveRampDown || negativeRampDown;
    } // isPwrRampingDown

    /*--------------------------------------------------------------------------------------------*/
    /* NOTE: The absolute magnetic encoders may not be installed with 0deg rotated to the "right" */
    /* rotational angle to put ZERO DEGREES where we want it.  By defining a starting offset, and */
    /* using this function to account for that offset, we can place zero where we want it in s/w. */
    /* Having DEGREES_PER_ROTATION as a variable lets us adjust for the 3.3V vs. 5.0V difference. */
    /*--------------------------------------------------------------------------------------------*/
    public double computeAbsoluteAngle( double measuredVoltage, double zeroAngleOffset )
    {
        final double DEGREES_PER_ROTATION = 360.0;  // One full rotation measures 360 degrees
        final double MAX_MA3_ANALOG_VOLTAGE = 3.3;  // 3.3V maximum analog output
        double measuredAngle = (measuredVoltage / MAX_MA3_ANALOG_VOLTAGE) * DEGREES_PER_ROTATION;
        // Correct for the offset angle (see note above)
        double correctedAngle = measuredAngle - zeroAngleOffset;
        // Enforce that any wrap-around remains in the range of -180 to +180 degrees
        while( correctedAngle < -180.0 ) correctedAngle += 360.0;
        while( correctedAngle > +180.0 ) correctedAngle -= 360.0;
        return correctedAngle;
    } // computeAbsoluteAngle

    /*--------------------------------------------------------------------------------------------*/
    public void readBulkData() {
        // For MANUAL mode, we must clear the BulkCache once per control cycle
        expansionHub.clearBulkCache();
        controlHub.clearBulkCache();
        // Get a fresh set of values for this cycle
        //   getCurrentPosition() / getTargetPosition() / getTargetPositionTolerance()
        //   getPower() / getVelocity() / getCurrent()
        //===== CONTROL HUB VALUES =====
        frontLeftMotorPos  = frontLeftMotor.getCurrentPosition();
        frontLeftMotorVel  = frontLeftMotor.getVelocity();
        frontRightMotorPos = frontRightMotor.getCurrentPosition();
        frontRightMotorVel = frontRightMotor.getVelocity();
        rearRightMotorPos  = rearRightMotor.getCurrentPosition();
        rearRightMotorVel  = rearRightMotor.getVelocity();
        rearLeftMotorPos   = rearLeftMotor.getCurrentPosition();
        rearLeftMotorVel   = rearLeftMotor.getVelocity();
        //===== EXPANSION HUB VALUES =====
        viperMotorsPos     = viperMotors.getCurrentPosition();
        viperMotorsVel     = viperMotors.getVelocity();
        viperMotorsPwr     = viperMotors.getPower();
        // Parse right odometry encoder
        rightOdometerPrev  = rightOdometerCount;
        rightOdometerCount = -rightOdometer.getCurrentPosition(); // Must be POSITIVE when bot moves FORWARD
        // Parse left odometry encoder
        leftOdometerPrev   = leftOdometerCount;
        leftOdometerCount  = -collectorMotor.getCurrentPosition();   // Must be POSITIVE when bot moves FORWARD
        // Parse rear odometry encoder
        strafeOdometerPrev  = strafeOdometerCount;
        strafeOdometerCount = -strafeOdometer.getCurrentPosition();  // Must be POSITIVE when bot moves RIGHT

        // NOTE: motor mA data is NOT part of the bulk-read, so increase cycle time!
//      frontLeftMotorAmps  = frontLeftMotor.getCurrent(MILLIAMPS);
//      frontRightMotorAmps = frontRightMotor.getCurrent(MILLIAMPS);
//      rearRightMotorAmps  = rearRightMotor.getCurrent(MILLIAMPS);
//      rearLeftMotorAmps   = rearLeftMotor.getCurrent(MILLIAMPS);
        viperMotorsAmps     = viperMotors.getCurrent(MILLIAMPS);
//      liftMotorAmps       = liftMotorF.getCurrent(MILLIAMPS) + liftMotorB.getCurrent(MILLIAMPS);

    } // readBulkData

    /*--------------------------------------------------------------------------------------------*/
    // This is a slow operation (involves an I2C reading) so only do it as needed
    public double readBatteryControlHub() {
        // Update local variable and then return that value
        controlHubV = controlHub.getInputVoltage( VoltageUnit.MILLIVOLTS );
        return controlHubV;
    } // readBatteryControlHub

    /*--------------------------------------------------------------------------------------------*/
    // This is a slow operation (involves an I2C reading) so only do it as needed
    public double readBatteryExpansionHub() {
        // Update local variable and then return that value
        expansionHubV = expansionHub.getInputVoltage( VoltageUnit.MILLIVOLTS );
        return expansionHubV;
    } // readBatteryExpansionHub

    /*--------------------------------------------------------------------------------------------*/
    public void driveTrainMotors( double frontLeft, double frontRight, double rearLeft, double rearRight )
    {
        frontLeftMotor.setPower( frontLeft );
        frontRightMotor.setPower( frontRight );
        rearLeftMotor.setPower( rearLeft );
        rearRightMotor.setPower( rearRight );
    } // driveTrainMotors

    /*--------------------------------------------------------------------------------------------*/
    /* Set all 4 motor powers to drive straight FORWARD (Ex: +0.10) or REVERSE (Ex: -0.10)        */
    public void driveTrainFwdRev( double motorPower )
    {
        frontLeftMotor.setPower(  motorPower );
        frontRightMotor.setPower( motorPower );
        rearLeftMotor.setPower(   motorPower );
        rearRightMotor.setPower(  motorPower );
    } // driveTrainFwdRev

    /*--------------------------------------------------------------------------------------------*/
    /* Set all 4 motor powers to strafe RIGHT (Ex: +0.10) or LEFT (Ex: -0.10)                     */
    public void driveTrainRightLeft( double motorPower )
    {
        frontLeftMotor.setPower(   motorPower );
        frontRightMotor.setPower( -motorPower );
        rearLeftMotor.setPower(   -motorPower );
        rearRightMotor.setPower(   motorPower );
    } // driveTrainRightLeft

    /*--------------------------------------------------------------------------------------------*/
    /* Set all 4 motor powers to turn clockwise (Ex: +0.10) or counterclockwise (Ex: -0.10)       */
    public void driveTrainTurn( double motorPower )
    {
        frontLeftMotor.setPower( -motorPower );
        frontRightMotor.setPower( motorPower );
        rearLeftMotor.setPower(  -motorPower );
        rearRightMotor.setPower(  motorPower );
    } // driveTrainTurn

    /*--------------------------------------------------------------------------------------------*/
    public void driveTrainMotorsZero()
    {
        frontLeftMotor.setPower( 0.0 );
        frontRightMotor.setPower( 0.0 );
        rearLeftMotor.setPower( 0.0 );
        rearRightMotor.setPower( 0.0 );
    } // driveTrainMotorsZero

    /*--------------------------------------------------------------------------------------------*/
    public void stopMotion() {
        // Stop all motion;
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        rearLeftMotor.setPower(0);
        rearRightMotor.setPower(0);
    }

    /*--------------------------------------------------------------------------------------------*/
    public double restrictDeltaPower( double powerNext, double powerNow, double powerStepMax ) {
        // Don't stress our motors (or draw too much current) by going straight from 0% to 100% power!
        double powerDelta = powerNext - powerNow;
        // Is the delta outside the allowed limit?
        // (if so, only step in that direction the allowed amount)
        if( Math.abs(powerDelta) > powerStepMax )
            powerNext = powerNow + ((powerNext > powerNow)? +powerStepMax : -powerStepMax);
        // Return the verified power setting
        return powerNext;
    } // restrictDeltaPower

    /*--------------------------------------------------------------------------------------------*/
    public int pixelBinUpdateStatus() {
        final double MAX_DIST_OCCUPIED = 60.0; // minimum distance sensor can report is 54.52mm
        int binPixelCount = 0;
        //========== LOWER POSITION IN PIXEL BIN ==========
        // 1) does the proximity detector detect a pixel?
        pixel1Distance = pixe1DistanceSensor.getDistance(DistanceUnit.MM);
        boolean binPosition1Empty = Double.isNaN(pixel1Distance) || (pixel1Distance > MAX_DIST_OCCUPIED);
        if( binPosition1Empty ) {
            pixel1Hue = 0.0;
            pixel1Color = PixelColorsEnum.EMPTY;
        } // binPositionIsEmpty
        else {
            binPixelCount++;
            // 2) read the color sensor in RGBA mode (red/green/blue/alpha)
            pixelRGBA = pixel1ColorSensor.getNormalizedColors();
            // 3) Convert RGBA to a color integer, and then to HSV (hue/sat/value)
            Color.colorToHSV( pixelRGBA.toColor(), pixelHSV );
            // 4) Store the HUE
            pixel1Hue = pixelHSV[0];
            // 5) Convert to an enumerated pixel color using hue & saturation
            pixel1Color = pixelHueSatToColor( pixelHSV[0], pixelHSV[1] );
        } // !binPositionIsEmpty
        //========== UPPER POSITION IN PIXEL BIN ==========
        // 1) does the proximity detector detect a pixel?
        pixel2Distance = pixe2DistanceSensor.getDistance(DistanceUnit.MM);
        boolean binPosition2Empty = Double.isNaN(pixel2Distance) || (pixel2Distance > MAX_DIST_OCCUPIED);
        if( binPosition2Empty ) {
            pixel2Hue = 0.0;
            pixel2Color = PixelColorsEnum.EMPTY;
        } // binPositionIsEmpty
        else {
            binPixelCount++;
            // 2) read the color sensor in RGBA mode (red/green/blue/alpha)
            pixelRGBA = pixel2ColorSensor.getNormalizedColors();
            // 3) Convert RGBA to a color integer, and then to HSV (hue/sat/value)
            Color.colorToHSV( pixelRGBA.toColor(), pixelHSV );
            // 4) Store the HUE
            pixel2Hue = pixelHSV[0];
            // 5) Convert to an enumerated pixel color using hue & saturation
            pixel2Color = pixelHueSatToColor( pixelHSV[0], pixelHSV[1] );
        }
        return binPixelCount;
    } // pixelBinUpdateStatus

    /*--------------------------------------------------------------------------------------------*/
    private PixelColorsEnum pixelHueSatToColor( double hue, double sat ) {
        PixelColorsEnum pixelColor;
        if((hue > 20.0) && (hue <= 65.0))
            pixelColor = PixelColorsEnum.YELLOW; // hue typically 45-48
        else if((hue > 70.0) && (hue <= 180.0)) {
            if( sat > 0.350 )
                pixelColor = PixelColorsEnum.GREEN;  // hue typically 109-117, saturation 0.560
            else
                pixelColor = PixelColorsEnum.WHITE;  // hue typically 100-142, saturation 0.160
        }
        else if((hue > 200.0) && (hue <= 250.0))
            pixelColor = PixelColorsEnum.PURPLE;  // hue typically 216-225
        else
            pixelColor = PixelColorsEnum.UNKNOWN;
        return pixelColor;
    } // pixelHueSatToColor

    /*--------------------------------------------------------------------------------------------*/
    /* setRunToPosition()                                                                         */
    /* - driveY -   true = Drive forward/back; false = Strafe right/left                          */
    /* - distance - how far to move (inches).  Positive is FWD/RIGHT                              */
    public void setRunToPosition( boolean driveY, double distance )
    {
        // Compute how many encoder counts achieves the specified distance
        int moveCounts = (int)(distance * COUNTS_PER_INCH);

        // These motors move the same for front/back or right/left driving
        frontLeftMotorTgt  = frontLeftMotorPos  +  moveCounts;
        frontRightMotorTgt = frontRightMotorPos + (moveCounts * ((driveY)? 1:-1));
        rearLeftMotorTgt   = rearLeftMotorPos   + (moveCounts * ((driveY)? 1:-1));
        rearRightMotorTgt  = rearRightMotorPos  +  moveCounts;

        // Configure target encoder count
        frontLeftMotor.setTargetPosition(  frontLeftMotorTgt  );
        frontRightMotor.setTargetPosition( frontRightMotorTgt );
        rearLeftMotor.setTargetPosition(   rearLeftMotorTgt   );
        rearRightMotor.setTargetPosition(  rearRightMotorTgt  );

        // Enable RUN_TO_POSITION mode
        frontLeftMotor.setMode(  DcMotor.RunMode.RUN_TO_POSITION );
        frontRightMotor.setMode( DcMotor.RunMode.RUN_TO_POSITION );
        rearLeftMotor.setMode(   DcMotor.RunMode.RUN_TO_POSITION );
        rearRightMotor.setMode(  DcMotor.RunMode.RUN_TO_POSITION );
    } // setRunToPosition

    /*--------------------------------------------------------------------------------------------*/
    /* viperSlideExtension()                                                                      */
    public void startViperSlideExtension(int targetEncoderCount )
    {
        // Range-check the target
        if( targetEncoderCount < VIPER_EXTEND_ZERO ) targetEncoderCount = VIPER_EXTEND_ZERO;
        if( targetEncoderCount > VIPER_EXTEND_FULL ) targetEncoderCount = VIPER_EXTEND_FULL;
        // Are we raising or lowering the lift?
        boolean directionUpward = (targetEncoderCount > viperMotorsPos)? true : false;
        // Set the power used to get there
        double motorPower = (directionUpward)? VIPER_RAISE_POWER : VIPER_LOWER_POWER;
        viperMotors.setPower( motorPower );
        // Configure target encoder count
        viperMotors.setTargetPosition( targetEncoderCount );
        // Enable RUN_TO_POSITION mode
        viperMotors.setMode(  DcMotor.RunMode.RUN_TO_POSITION );
        // Note that we've started a RUN_TO_POSITION and need to reset to RUN_USING_ENCODER
        viperMotorAutoMove = true;
    } // viperSlideExtension

    public void checkViperSlideExtension()
    {
        // Have we commanded an automatic lift movement that we need to terminate?
        if( viperMotorAutoMove ) {
           // turn off the auto-movement power, but don't go to ZERO POWER or
           // the weight of the lift will immediately drop it back down.
           viperMotors.setPower( VIPER_HOLD_POWER );
           viperMotors.setMode(  DcMotor.RunMode.RUN_USING_ENCODER );
           viperMotorAutoMove = false;
        }
    } // checkViperSlideExtension

    public int singleSonarRangeF() {
        //Query the current range sensor reading and wait for a response
        return sonarRangeF.getDistanceSync();
    } // singleSonarRangeF

    enum UltrasonicsInstances
    {
        SONIC_RANGE_LEFT,
        SONIC_RANGE_RIGHT,
        SONIC_RANGE_FRONT,
        SONIC_RANGE_BACK;
    }

    enum UltrasonicsModes
    {
        SONIC_FIRST_PING,
        SONIC_MOST_RECENT;
    }
    public int slowSonarRange( UltrasonicsInstances sensorInstance ) {
        // This is the SLOW version that sends an ultrasonic ping, waits 50 msec for the result,
        // and returns a value.  The returned valued is based on SINGLE reading (no averaging).
        // This version is intended for 1-time readings where the 50msec is okay (not control loops).
        int cm = 0;
        switch( sensorInstance ) {
            case SONIC_RANGE_FRONT : cm = sonarRangeF.getDistanceSync(); break;
            default                : cm = 0;
        } // switch()
        return cm;
    } // slowSonarRange

    public int fastSonarRange( UltrasonicsInstances sensorInstance, UltrasonicsModes mode ) {
        // This is the FAST version that assumes there's a continuous sequence of pings being
        // triggered so it simply returns the "most recent" answer (no waiting!). This function is
        // intended for control loops that can't afford to incur a 50msec delay in the loop time.
        // The first call should pass SONIC_FIRST_PING and ignore the result; All subsequent calls
        // (assuming at least 50 msec has elapsed) should pass SONIC_MOST_RECENT and use the distance
        // returned.
        int cm = 0;
        switch( sensorInstance ) {
            case SONIC_RANGE_FRONT : cm = sonarRangeF.getDistanceAsync(); break;
            default                : cm = 0;
        } // switch()
        // Do we need to zero-out the value returned (likely from another time/place)?
        if( mode == HardwarePixelbot.UltrasonicsModes.SONIC_FIRST_PING ) {
            cm = 0;
        }
        // Return
        return cm;
    } // fastSonarRange

    /*--------------------------------------------------------------------------------------------*/

    /***
     *
     * waitForTick implements a periodic delay. However, this acts like a metronome with a regular
     * periodic tick.  This is used to compensate for varying processing times for each cycle.
     * The function looks at the elapsed cycle time, and sleeps for the remaining time interval.
     *
     * @param periodMs  Length of wait cycle in mSec.
     */
    public void waitForTick(long periodMs) {

        long  remaining = periodMs - (long)period.milliseconds();

        // sleep for the remaining portion of the regular cycle period.
        if (remaining > 0) {
            try {
                sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Reset the cycle clock for the next pass.
        period.reset();
    } /* waitForTick() */

} /* HardwarePixelbot */
