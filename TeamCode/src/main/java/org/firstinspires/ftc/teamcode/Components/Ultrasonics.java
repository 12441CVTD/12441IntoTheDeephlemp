package org.firstinspires.ftc.teamcode.Components;

import static org.firstinspires.ftc.teamcode.BasicRobot.logger;
import static org.firstinspires.ftc.teamcode.BasicRobot.op;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.String.valueOf;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.LED;

import java.util.ArrayList;

public class Ultrasonics {
    private AnalogInput ultrasonicFront, ultrasonicBack, ultrasonicRight, ultrasonicLeft;
    private LED ultraFront, ultraBack, ultraRight, ultraLeft;
    public double ultraRange = 35, lastUltraUpdate = -10, lastSetPos = -10, time = 0.0, robotWidth = 13, robotLength = 15;
    //x,y,a
    private double[] pos = {0, 0, 0}, error = {0, 0};
    public ArrayList<double[]> errorLog = new ArrayList<>();
    private boolean high = false;
    public int updated = 0, updatedto = 0;

    public double[] dist = {0, 0, 0, 0};

    public Ultrasonics() {
        ultrasonicFront = op.hardwareMap.get(AnalogInput.class, "ultrasonicFront");
//        ultrasonicBack = op.hardwareMap.get(AnalogInput.class, "ultrasonicBack");
        ultrasonicLeft = op.hardwareMap.get(AnalogInput.class, "ultrasonicLeft");
        ultrasonicRight = op.hardwareMap.get(AnalogInput.class, "ultrasonicRight");
        ultraFront = op.hardwareMap.get(LED.class, "ultraFront");
//        ultraBack = op.hardwareMap.get(LED.class, "ultraBack");
        ultraRight = op.hardwareMap.get(LED.class, "ultraRight");
        ultraLeft = op.hardwareMap.get(LED.class, "ultraLeft");
//        ultraBack.enable(true);
        ultraRight.enable(true);
        ultraFront.enable(true);
        ultraLeft.enable(true);
        logger.createFile("Ultrasonics","error0, error1");
    }

    public void logError() {
        double[] potential_log = {0,0};
        if(abs(error[0])<5){
            potential_log[0] = error[0];
        }else{
            //data is bad
        }
        if(abs(error[1])<5){
            potential_log[1] = error[1];
        }else{
            //data is bad
        }
        if(potential_log[0]!=0||potential_log[1]!=0) {
            errorLog.add(potential_log);
        }
    }

    public void clearError() {
        errorLog.clear();
    }

    public boolean sufficientData() {
        if (errorLog.size() < 13) {
            return false;
        } else {
            return true;
        }
    }

    public double[] averageError() {
        double[] aberage = {0, 0};
        for (int i = 0; i < errorLog.size(); i++) {
            aberage[0] += errorLog.get(i)[0];
            aberage[1] += errorLog.get(i)[1];
        }
        aberage[0] /= errorLog.size();
        aberage[1] /= errorLog.size();
        return aberage;
    }

    public boolean updateUltra(double xpos, double ypos, double angle) {
        updatedto=0;
        pos[0] = xpos;
        pos[1] = ypos;
        pos[2] = angle;
        updated = 0;
        angle *= 180 / PI;
        time = op.getRuntime();
        if(angle>180){
            angle-=360;
        }
        if(angle<-180){
            angle+=360;
        }
        if (time - lastUltraUpdate > 0.05 && !high) {
//            ultraBack.enable(false);
            ultraRight.enable(false);
            ultraFront.enable(false);
            ultraLeft.enable(false);
            high = true;
        }
        if (time - lastUltraUpdate > 0.1 & high) {
            error[0]=0;
            error[1]=0;
            getDistance();
            double distance = dist[0] + robotWidth / 2;
            if (distance < 20 + robotWidth/2 && distance > 0) {
                if (abs(angle) < 5 || abs(angle-360) < 5) {
                    error[1] = -70.5 + distance - pos[1];
                    updated = 5;
                } else if (abs(180 - angle) < 5 || abs(-180 - angle)<5) {
                    error[1] = 70.5 - distance - pos[1];
                    updated = 5;

                } else if (abs(-90 - angle) < 5) {
                    error[0] = -70.5 + distance - pos[0];
                    updated = 5;

                } else if (abs(90 - angle) < 5) {
                    error[0] = 70.5 - distance - pos[0];
                    updated = 5;

                } else {
                    //do nothing
                }
            }
            distance = dist[1] + robotWidth / 2;
            if (distance < 20+ robotWidth/2 && distance > 0) {
            if (abs(180 - angle) < 5 || abs(-180 - angle)<5) {
                    error[1] = -70.5 + distance - pos[1];
                    updated = 5;

                } else if (abs(angle) < 5 || abs(angle-360) < 5) {
                    error[1] = 70.5 - distance - pos[1];
                    updated = 5;

                } else if (abs(90 - angle) < 5) {
                    error[0] = -70.5 + distance - pos[0];
                    updated = 5;

                } else if (abs(-90 - angle) < 5) {
                    error[0] = 70.5 - distance - pos[0];
                    updated = 5;

                } else {
                    //do nothing
                }
            }
            distance = dist[2] + robotLength / 2;
            if (distance < 20+ robotLength/2 && distance > 0) {
            if (abs(180 - angle) < 5 || abs(-180 - angle)<5) {
                    error[0] = -70.5 + distance - pos[0];
                    updated = 5;

                } else if (abs(angle) < 5 || abs(angle-360) < 5) {
                    error[0] = 70.5 - distance - pos[0];
                    updated = 5;

                } else if (abs(90 - angle) < 5) {
                    error[1] = 70.5 - distance - pos[1];
                    updated = 5;

                } else if (abs(-90 - angle) < 5) {
                    error[1] = -70.5 + distance - pos[1];
                    updated = 5;

                } else {
                    //do nothing
                }
            }
            distance = dist[3] + robotLength / 2;
            if (distance < 20+ robotWidth/2 && distance > 0) {
            if (abs(180 - angle) < 5 || abs(-180 - angle)<5) {
                    error[0] = 70.5 - distance - pos[0];
                    updated = 5;

                } else if (abs(angle) < 5) {
                    error[0] = -70.5 + distance - pos[0];
                    updated = 5;

                } else if (abs(90 - angle) < 5) {
                    error[1] = -70.5 + distance - pos[1];
                    updated = 5;

                } else if (abs(-90 - angle) < 5) {
                    error[1] = 70.5 - distance - pos[1];
                    updated = 5;

                } else {
                    //do nothing
                }
            }
            if (updated==5) {
                logError();
            }
        }
        if (sufficientData() && time - lastSetPos > 2) {
            lastSetPos = time;
            return true;
        } else {
            return false;
        }
    }

    private void getDistance() {
        dist = new double[]{90.48337 * ultrasonicRight.getVoltage() - 13.12465, 90.48337 * ultrasonicLeft.getVoltage() - 13.12465
                , 90.48337 * ultrasonicFront.getVoltage() - 13.12465, - 13.12465};
//        ultraBack.enable(true);
        ultraRight.enable(true);
        ultraFront.enable(true);
        ultraLeft.enable(true);
        lastUltraUpdate = time;
        high = false;
    }
    public Pose2d getPose2d() {
        updatedto=5;
        double[] errors = averageError();
        clearError();
        logger.log("Ultrasonics", errors[0]+","+errors[1]);
        return new Pose2d(pos[0] + errors[0], pos[1] + errors[1]);
    }
}
