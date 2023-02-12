package org.firstinspires.ftc.teamcode.powerplayV2.commands;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.robotbase.GamepadExEx;

public class RumbleCommand extends CommandBase {
    private GamepadExEx gp;

    public RumbleCommand(GamepadExEx gp) {
        this.gp = gp;
    }

    @Override
    public void initialize() {
        gp.rumble();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
