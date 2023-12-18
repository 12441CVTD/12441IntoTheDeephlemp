/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Drivercontrol.lm2;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

//import org.firstinspires.ftc.teamcode.Drivercontrol.drive.Feildcentricdrive;
import org.firstinspires.ftc.teamcode.Drivercontrol.drive.Feildcentricdrive;
import org.firstinspires.ftc.teamcode.subsystems.extension;
import org.firstinspires.ftc.teamcode.util.airplane;

import java.util.List;


/*
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@TeleOp(name="lm2drive2driver", group="Linear OpMode")

public class lm2teleop2drivers extends LinearOpMode {
    airplane air=new airplane();

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    DcMotor hang;
    Servo plane;
    Servo hangs;
    Servo wrist;
    Servo claw;
    Servo wrist1;
    Servo claw1;

    Feildcentricdrive drive = new Feildcentricdrive();

    @Override
    public void runOpMode() {
        ElapsedTime runtime=new ElapsedTime();
        plane=hardwareMap.get(Servo.class,"plane");
        plane.setPosition(0);
       // boolean half = false;//half speed
        boolean open = false;
        boolean state = false;
        //boolean cstate = false;
       // boolean state2 = false;
        boolean lastState = false;// claw booleans
//        hang=hardwareMap.get(DcMotor.class,"hang");
//        hangs=hardwareMap.get(Servo.class,"hangs");
        //hangs.setPosition(0);
        wrist = hardwareMap.get(Servo.class, "wrist");
        claw = hardwareMap.get(Servo.class, "claw");//hardwaremap claw and tilt
        wrist1 = hardwareMap.get(Servo.class, "wrist1");
        claw1 = hardwareMap.get(Servo.class, "claw1");
        extension extend = new extension(hardwareMap);//tilt object made
       // extend.release();//releases the tilt to move into start pos
        extend.setStowPos();
        //boolean reset = false;//boolean for reset heading
        boolean slowturn = false;//boolean for slowturn mode
        double rx;//right x
        boolean rc = false;
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Press circle or b on gamepad1 for robot centric","ok");
        telemetry.update();
        if(gamepad1.circle||gamepad1.b){
            rc=true;
        }
        //init dt
        //extend.setStowPos();//for when auto is ready
        while(opModeInInit()){
            if(gamepad1.circle||gamepad1.b){
                rc=true;
                break;
            }
//            if(gamepad1.a){
//                extend.hold();//hold init pos
//                break;
//            }
        }
        waitForStart();
        if (rc)drive.init(hardwareMap, 1);else if(!rc)drive.init(hardwareMap, 0);
        runtime.reset();
       // boolean hangmode=false;
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        // run until the end of the match (driver presses STOP)
        gamepad2.setLedColor(0.98823529411,0.98823529411,0.03921568627,120000);
        gamepad1.setLedColor(0.13333333333,0.03921568627,0.98823529411,120000);
        runtime.reset();
        while (opModeIsActive()) {
            if(runtime.milliseconds()>120000){
                gamepad2.setLedColor(0.98823529411,0.98823529411,0.03921568627,120000);
                gamepad1.setLedColor(0.98823529411,0.03921568627,0.03921568627,120000);
            }
            air.run(extend,gamepad2,plane,false);
            telemetry.addData("y",gamepad1.touchpad_finger_1_y);
            telemetry.addData("1",gamepad1.touchpad_finger_1);
            telemetry.addData("target", extend.tilt.getTargetPosition());
            telemetry.addData("current", extend.getTilt());
            telemetry.update();
          //  gamepad1.rumble(Math.abs(extend.getTilt()/extend.tilt.getTargetPosition()),Math.abs(extend.getTilt()/extend.tilt.getTargetPosition()),50);
            if (gamepad1.circle) {//slow turn mode
                slowturn = true;
            }
            if (gamepad1.square) {
                slowturn = false;
            }
            rx = gamepad1.right_stick_x;
            if (slowturn) {
                rx *=0.7;
            } //could be more compact

           // half=false;
            if (gamepad1.right_trigger >=0.5) drive.run(gamepad1.left_stick_y, gamepad1.left_stick_x, rx, 0.5, true,gamepad1.back&& gamepad1.start);
            else drive.run(gamepad1.left_stick_y, gamepad1.left_stick_x, rx, 1, true, gamepad1.back&& gamepad1.start);

//            if(half)drive.run(gamepad1.left_stick_y, gamepad1.left_stick_x, rx, 0.5, true,gamepad1.back);
//            else if(!half)drive.run(gamepad1.left_stick_y, gamepad1.left_stick_x, rx, 1, true, gamepad1.back);

            state = gamepad2.left_bumper;//state for claw

            if (gamepad2.dpad_up&&!(gamepad2.left_trigger >= 0.5)) {extend.setStowPos();wrist.setPosition(0.65);wrist1.setPosition(0.35);}//set positions
            else if (gamepad2.dpad_right&&!(gamepad2.left_trigger >= 0.5)) {extend.setIntakeClosePos();wrist.setPosition(0.0);wrist1.setPosition(1);}
            //else if (gamepad2.square&&!(gamepad1.left_trigger >= 0.5)) {extend.setPlaceLow();wrist.setPosition(0.65);wrist1.setPosition(0.35);}
           // else if (gamepad2.circle&&!(gamepad1.left_trigger >= 0.5)) {extend.setPlaceMid();wrist.setPosition(0.65);wrist1.setPosition(0.35);}
            //else if (gamepad2.dpad_left&&!(gamepad1.left_trigger >= 0.5)) {extend.setIntakeFarPos();wrist.setPosition(0.0);wrist1.setPosition(1);}
            else if (gamepad2.dpad_left&&!(gamepad2.left_trigger >= 0.5)) {extend.setPlaceLow();wrist.setPosition(0.65);wrist1.setPosition(0.35);}

            if (gamepad2.left_bumper && !lastState) {//new claw code for easier driving
                if (open) {claw.setPosition(0.7);claw1.setPosition(0.33);open = false;}
                else {claw.setPosition(0.35);claw1.setPosition(0.6);open = true;}
            }
           // if(!hangmode) {
                if (gamepad2.dpad_down && gamepad2.left_trigger >= 0.5) {extend.makelesstilt();}// pos over-rides
                else if (gamepad2.dpad_up && gamepad2.left_trigger >= 0.5) {extend.makemoretilt();}
           // }else{
//                if (gamepad1.dpad_down) hang.setPower(-1);//place pos over-rides
//                else if (gamepad1.dpad_up) hang.setPower(1);
//                else hang.setPower(0);

            //}
//            if(gamepad1.dpad_right){
//                extend.sethang();
//                hangs.setPosition(1);
//            }
//            if(gamepad1.start){
//                hangmode=true;
//            }

//            if(gamepad1.dpad_up) {//old claw code
//                claw.setPosition(0.2);
//
//            }else if(gamepad1.dpad_down)
//                claw.setPosition(1);
//
//            }
           // cstate= gamepad1.dpad_down;
            //state2=gamepad1.dpad_up;
            lastState = gamepad2.left_bumper;//set last state to the state at end of loop
        extend.run();
        }
    }
}


