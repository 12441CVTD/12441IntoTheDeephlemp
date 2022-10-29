package org.firstinspires.ftc.teamcode.robots.taubot;

import static org.firstinspires.ftc.teamcode.robots.taubot.util.Constants.Position;
import static org.firstinspires.ftc.teamcode.robots.taubot.util.Utils.wrapAngle;

import org.firstinspires.ftc.teamcode.robots.taubot.subsystem.Robot;
import org.firstinspires.ftc.teamcode.robots.taubot.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.robots.taubot.util.Utils;
import org.firstinspires.ftc.teamcode.robots.taubot.vision.VisionProvider;
import org.firstinspires.ftc.teamcode.robots.taubot.vision.VisionProviders;
import org.firstinspires.ftc.teamcode.statemachine.Stage;
import org.firstinspires.ftc.teamcode.statemachine.StateMachine;

public class Autonomous {
    public VisionProvider visionProvider;
    private Robot robot;

    enum Mode {
        LINEAR, SPLINE, NO_RR, SIMPLE;
    }

    // autonomous routines
    private StateMachine left, right, blueDown, redDown,
            blueUpLinear, redUpLinear, blueDownLinear, redDownLinear,
            leftNoRR, rightNoRR, blueDownNoRR, redDownNoRR,
            blueUpSimple, redUpSimple, blueDownSimple, redDownSimple;
    // misc. routines
    public StateMachine backAndForth, square, turn, lengthTest, diagonalTest, squareNoRR;

    public Autonomous(Robot robot) {
        this.robot = robot;
    }

    public StateMachine getStateMachine(Position startingPosition, Mode mode) {
        switch(mode) {
            case LINEAR:
                switch(startingPosition) {
                    case START_LEFT:
                        return left;
                    case START_RIGHT:
                        return right;
                }
                break;
            case SPLINE:
/*
                switch(startingPosition) {
                    case START_BLUE_UP:
                        return blueUpLinear;
                    case START_RED_UP:
                        return redUpLinear;
                    case START_BLUE_DOWN:
                        return blueDownLinear;
                    case START_RED_DOWN:
                        return redDownLinear;
                }
*/
                break;
            case NO_RR:
                switch(startingPosition) {
                    case START_LEFT:
                        return leftNoRR;
                    case START_RIGHT:
                        return rightNoRR;
                }
                break;
            case SIMPLE:
                /*
                switch(startingPosition) {
                    case START_BLUE_UP:
                        return blueUpSimple;
                    case START_RED_UP:
                        return redUpSimple;
                    case START_BLUE_DOWN:
                        return blueDownSimple;
                    case START_RED_DOWN:
                        return redDownSimple;
                }
                */

                break;
        }

        return null;
    }

    private StateMachine trajectorySequenceToStateMachine(TrajectorySequence trajectorySequence) {
        return Utils.getStateMachine(new Stage())
                .addSingleState(() -> {
                    robot.driveTrain.followTrajectorySequenceAsync(
                            trajectorySequence
                    );
                })
                .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                .build();
    }

    public void build(Position startingPosition) {
        //----------------------------------------------------------------------------------------------
        // Misc. Routines
        //----------------------------------------------------------------------------------------------
        
        TrajectorySequence backAndForthSequence =
                robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(48)
                        .forward(48)
                        .build();
        backAndForth = trajectorySequenceToStateMachine(backAndForthSequence);

        TrajectorySequence squareSequence =
                robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(24)
                        .turn(Math.toRadians(-90))
                        .back(24)
                        .turn(Math.toRadians(-90))
                        .back(24)
                        .turn(Math.toRadians(-90))
                        .back(24)
                        .turn(Math.toRadians(-90))
                        .build();
        square = trajectorySequenceToStateMachine(squareSequence);

        squareNoRR = Utils.getStateMachine(new Stage())
                .addState(() -> robot.driveTrain.driveUntilDegrees(24, 90,20))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.turnUntilDegrees(0))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.driveUntilDegrees(24, 0,20))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.turnUntilDegrees(270))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.driveUntilDegrees(24, 270,20))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.turnUntilDegrees(180))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.driveUntilDegrees(24, 180, 20))
                .addTimedState(1f, () -> {}, () -> {})
                .addState(() -> robot.driveTrain.turnUntilDegrees(90))
                .addTimedState(1f, () -> {}, () -> {})
                .build();

        TrajectorySequence turnSequence =
                robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .turn(Math.toRadians(90))
                        .turn(Math.toRadians(90))
                        .turn(Math.toRadians(90))
                        .turn(Math.toRadians(90))
                        .build();
        turn = trajectorySequenceToStateMachine(turnSequence);



        //----------------------------------------------------------------------------------------------
        // Spline Routines
        //----------------------------------------------------------------------------------------------

        switch(startingPosition) {
            case START_LEFT:
            /* sample code from Reach
                left = Utils.getStateMachine(new Stage())

                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addMineralState(
                                () -> visionProvider.getMostFrequentPosition().getIndex(),
                                () -> { robot.crane.articulate(Crane.Articulation.LOWEST_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.MIDDLE_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.HIGH_TIER); return true; }
                        )
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addTimedState(3f, () -> robot.crane.turret.setTargetHeading(90), () -> {})
                        .addTimedState(1.5f, () -> robot.crane.dump(), () -> robot.crane.articulate(Crane.Articulation.HOME))
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        blueUp2
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.TRANSFER))
                        .build();
*/

                leftNoRR = Utils.getStateMachine(new Stage())
                        .addTimedState(1, () -> {
                        }, () -> {
                        }) //wait

                        //drive until we approach conestack
                        .addState(() -> robot.driveTrain.driveUntil(-20, 20))
                        //turn to warehouse
                        .addState(() -> robot.driveTrain.turnUntilDegrees(-90))
                        //enter warehouse
                        .addState(() -> robot.driveTrain.driveUntilDegrees(16, wrapAngle(-45), 20))
                        //.addTimedState(3,()->{}, ()->{}) //wait
                        .addState(() -> robot.driveTrain.driveUntilDegrees(26, wrapAngle(45), 20))

                        .build();
                break;
            case START_RIGHT:
                rightNoRR = Utils.getStateMachine(new Stage())

                        //start moving the arm so subsequent movements aren't as large
                        .addTimedState(1, () -> {
                        }, () -> {
                        }) //wait so arm movement doesn't build on kinetic energy of chassis

//                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.HIGH_TIER))
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)

                        .addTimedState(1, () -> {
                        }, () -> {
                        }) //wait

                        .addState(() -> robot.driveTrain.driveUntil(1.5 * 23.5, 20))
                        //turn to warehouse
                        .addState(() -> robot.driveTrain.turnUntilDegrees(45))
                        //enter warehouse
                        .addState(() -> robot.driveTrain.driveUntilDegrees(16, wrapAngle(45), 20))
                        //.addTimedState(3,()->{}, ()->{}) //wait
                        .addState(() -> robot.driveTrain.driveUntilDegrees(26, wrapAngle(-45), 20))

                        .build();
/* samples from Reach
                TrajectorySequence redUp1Simple = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(20)
                        .build();
                TrajectorySequence redUp2Simple = robot.driveTrain.trajectorySequenceBuilder(redUp1Simple.end())
                        .turn(Math.toRadians(-90))
                        .back(70)
                        .build();
                redUpSimple = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    redUp1Simple
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        redUp2Simple
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.TRANSFER))
                        .build();
                break;


            case START_BLUE_DOWN:
                TrajectorySequence blueDown1 = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(29.67)
                        .build();
                TrajectorySequence blueDown2 = robot.driveTrain.trajectorySequenceBuilder(blueDown1.end())
                        .splineTo(new Vector2d(-68, 68), Math.toRadians(135))
                        .build();
                TrajectorySequence blueDown3 = robot.driveTrain.trajectorySequenceBuilder(blueDown2.end())
                        .setReversed(true)
                        .splineTo(new Vector2d(-24, 0), Math.toRadians(0))
                        .splineTo(new Vector2d(12, 12), Math.toRadians(45))
                        .back(50)
                        .turn(Math.toRadians(180))
                        .build();
                blueDown = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    blueDown1
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addMineralState(
                                () -> visionProvider.getMostFrequentPosition().getIndex(),
                                () -> { robot.crane.articulate(Crane.Articulation.LOWEST_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.MIDDLE_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.HIGH_TIER); return true; }
                        )
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addSingleState(() -> robot.crane.turret.setTargetHeading(-90))
                        .addState(() -> robot.turret.isTurretNearTarget())
                        .addTimedState(1.5f, () -> robot.crane.dump(), () -> robot.crane.articulate(Crane.Articulation.HOME))
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                    blueDown2
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addTimedState(3f,
                                () -> robot.driveTrain.toggleDuckSpinner(Alliance.BLUE.getMod()),
                                () -> robot.driveTrain.toggleDuckSpinner(Alliance.BLUE.getMod())
                        )
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                    blueDown3
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.TRANSFER))
                        .build();

                TrajectorySequence blueDownLinear1 = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(31.67)
                        .build();
                TrajectorySequence blueDownLinear2 = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(38.33)
                        .turn(-Math.toRadians(90))
                        .forward(48)
                        .turn(Math.toRadians(90))
                        .forward(36)
                        .turn(-Math.toRadians(90))
                        .forward(36)
                        .build();
                blueDownLinear = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    blueDownLinear1
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addMineralState(
                                () -> visionProvider.getMostFrequentPosition().getIndex(),
                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_HIGH_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_MIDDLE_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_HIGH_TIER); return true; }
                        )
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addTimedState(3f, () -> robot.crane.turret.setTargetHeading(-90), () -> {})
                        .addTimedState(1.5f, () -> robot.crane.dump(), () -> robot.crane.articulate(Crane.Articulation.HOME))
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        blueDownLinear2
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .build();

                blueDownNoRR = Utils.getStateMachine(new Stage())
                        .build();

                TrajectorySequence blueDown1Simple = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(20)
                        .build();
                TrajectorySequence blueDown2Simple = robot.driveTrain.trajectorySequenceBuilder(blueDown1Simple.end())
                        .turn(-Math.toRadians(90))
                        .forward(98)
                        .build();
                blueDownSimple = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    blueDown1Simple
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        blueDown2Simple
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.TRANSFER))
                        .build();
                break;
            case START_RED_DOWN:
                TrajectorySequence redDown1 = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(29.67)
                        .build();
                TrajectorySequence redDown2 = robot.driveTrain.trajectorySequenceBuilder(redDown1.end())
                        .splineTo(new Vector2d(-68, -68), Math.toRadians(215))
                        .build();
                TrajectorySequence redDown3 = robot.driveTrain.trajectorySequenceBuilder(redDown2.end())
                        .setReversed(true)
                        .splineTo(new Vector2d(-24, 0), Math.toRadians(0))
                        .splineTo(new Vector2d(12, -12), Math.toRadians(315))
                        .back(50)
                        .turn(Math.toRadians(180))
                        .build();
                redDown = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    redDown1
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addMineralState(
                                () -> visionProvider.getMostFrequentPosition().getIndex(),
                                () -> { robot.crane.articulate(Crane.Articulation.LOWEST_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.MIDDLE_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.HIGH_TIER); return true; }
                        )
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addSingleState(() -> robot.crane.turret.setTargetHeading(90))
                        .addState(() -> robot.turret.isTurretNearTarget())
                        .addTimedState(1.5f, () -> robot.crane.dump(), () -> robot.crane.articulate(Crane.Articulation.HOME))
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        redDown2
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addTimedState(3f,
                                () -> robot.driveTrain.toggleDuckSpinner(Alliance.RED.getMod()),
                                () -> robot.driveTrain.toggleDuckSpinner(Alliance.RED.getMod())
                        )
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        redDown3
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.TRANSFER))
                        .build();

                TrajectorySequence redDownLinear1 = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(31.67)
                        .build();
                TrajectorySequence redDownLinear2 = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(38.33)
                        .turn(Math.toRadians(90))
                        .forward(48)
                        .turn(-Math.toRadians(90))
                        .forward(36)
                        .turn(Math.toRadians(90))
                        .forward(36)
                        .build();

                redDownLinear = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    redDownLinear1
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addMineralState(
                                () -> visionProvider.getMostFrequentPosition().getIndex(),
                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_HIGH_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_MIDDLE_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_HIGH_TIER); return true; }
                        )
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addTimedState(3f, () -> robot.crane.turret.setTargetHeading(90), () -> {})
                        .addTimedState(1.5f, () -> robot.crane.dump(), () -> robot.crane.articulate(Crane.Articulation.HOME))
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        redDownLinear2
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .build();

                redDownNoRR = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {robot.driveTrain.setChassisLengthMode(DriveTrain.ChassisLengthMode.BOTH);})
                        .addSingleState(() -> {
                            robot.crane.setToHomeEnabled(false);
                            //robot.driveTrain.setUseAutonChassisLengthPID(true);
                        })
                        //hack pre-align swerve steer by driving into wall
                        .addState(() -> robot.driveTrain.driveUntil(1,robot.driveTrain.getPoseEstimate().getHeading() ,.1))
                        .addTimedState(1,()->{}, ()->{}) //wait

                        //extend swerve
                        .addSingleState(() -> robot.driveTrain.setChassisLength(MIN_CHASSIS_LENGTH +9))
                        //start moving the arm so subsequent movenments aren't as large
                        .addTimedState(1,()->{}, ()->{}) //wait so arm movement doesn't build on kinetic energy of chassis

                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.HIGH_TIER))
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)

                        .addTimedState(1,()->{}, ()->{}) //wait

                        //preload dump section
                        .addMineralState(
                                () -> visionProvider.getMostFrequentPosition().getIndex(),
                                () -> { robot.crane.articulate(Crane.Articulation.LOWEST_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.MIDDLE_TIER); return true; },
                                () -> { robot.crane.articulate(Crane.Articulation.HIGH_TIER); return true; }
                        )
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)

                        //turn turret to shared hub, then dump and return
                        .addTimedState(2f, () -> robot.crane.turret.setTargetHeading(-45), () -> {})
                        .addTimedState(1.5f, () -> robot.crane.dump(), () -> {})
                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        .addTimedState(1f, () -> robot.crane.turret.setTargetHeading(0), () -> {})
                        //.addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
                        //preload dump section end

//                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.AUTON_FFUTSE_PREP))
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)

                        //retract swerve
                        .addSingleState(() -> {robot.driveTrain.setChassisLengthMode(DriveTrain.ChassisLengthMode.SWERVE);})
                        .addSingleState(() -> robot.driveTrain.setChassisLength(MIN_CHASSIS_LENGTH))

                        //FFUTSE Retrieval
//                        .addMineralState(
//                                () -> visionProvider.getMostFrequentPosition().getIndex(),
//                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_FFUTSE_LEFT); return true; },
//                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_FFUTSE_MIDDLE); return true; },
//                                () -> { robot.crane.articulate(Crane.Articulation.AUTON_FFUTSE_RIGHT); return true; }
//                        )
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
//                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.AUTON_FFUTSE_UP))
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
//                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.AUTON_FFUTSE_HOME))
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
//                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.STOW_FFUTSE))
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)
//                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.RELEASE_FFUTSE))
//                        .addState(() -> robot.crane.getArticulation() == Crane.Articulation.MANUAL)


                        .addSingleState(() -> {
                            robot.crane.setToHomeEnabled(true);
                            //robot.driveTrain.setUseAutonChassisLengthPID(true);
                        })
                        //end pickup FFUTSE sequence

                        //turn toward warehouse
                        .addSingleState(() -> robot.driveTrain.setChassisLength(MIN_CHASSIS_LENGTH))
                        .addState(() -> robot.driveTrain.driveUntil(-20,20))
                        //turn to warehouse
                        .addState(() -> robot.driveTrain.turnUntilDegrees(-45))
                        //enter warehouse
                        .addState(() -> robot.driveTrain.driveUntilDegrees(16, wrapAngle(-45),20))
                        //.addTimedState(3,()->{}, ()->{}) //wait
                        .addState(() -> robot.driveTrain.driveUntilDegrees(26, wrapAngle(45),20))

                        .build();

                TrajectorySequence redDown1Simple = robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(20)
                        .build();
                TrajectorySequence redDown2Simple = robot.driveTrain.trajectorySequenceBuilder(redDown1Simple.end())
                        .turn(Math.toRadians(90))
                        .forward(98)
                        .build();
                redDownSimple = Utils.getStateMachine(new Stage())
                        .addSingleState(() -> {
                            robot.driveTrain.followTrajectorySequenceAsync(
                                    redDown1Simple
                            );
                        })
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() ->
                                robot.driveTrain.followTrajectorySequenceAsync(
                                        redDown2Simple
                                )
                        )
                        .addState(() -> !robot.driveTrain.trajectorySequenceRunner.isBusy())
                        .addSingleState(() -> robot.crane.articulate(Crane.Articulation.TRANSFER))
                        .build();
                break;
        }
       */
        }
        TrajectorySequence diagonalTestTrajectory =
                robot.driveTrain.trajectorySequenceBuilder(robot.driveTrain.getPoseEstimate())
                        .back(72)
                        .turn(Math.toRadians(180))
                        .back(48)
                        .turn(Math.toRadians(-45))
                        .back(100)
                        .build();
        diagonalTest = trajectorySequenceToStateMachine(diagonalTestTrajectory);
    }


    public void createVisionProvider(int visionProviderIndex) {
        try {
            visionProvider = VisionProviders.VISION_PROVIDERS[visionProviderIndex].newInstance();
        } catch(IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Error while instantiating vision provider");
        }
    }
}
