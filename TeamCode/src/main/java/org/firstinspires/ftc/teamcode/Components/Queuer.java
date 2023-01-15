package org.firstinspires.ftc.teamcode.Components;

import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.logger;
import static org.firstinspires.ftc.teamcode.Robots.BasicRobot.op;

import java.util.ArrayList;

public class Queuer {
    private ArrayList<QueueElement> queueElements;
    private boolean firstLoop = true, mustFinish = false;
    private int currentlyQueueing = 0, currentEvent = -1, mustStartCondition = -1, completeCurrentEvent = 0;
    private double delay = 0;


    public Queuer() {
        queueElements = new ArrayList<QueueElement>();
    }

    public void setFirstLoop(boolean p_firstLoop) {
        firstLoop = p_firstLoop;
    }

    /**
     * creates new queue element if first loop
     * updates which element is currently being queued and which element is currently being executed
     * determines if currently queued element should run
     */
    public boolean queue(boolean p_asynchronous, boolean done_condition) {
        double p_delay = delay;
        delay = 0;
        return queue(p_asynchronous, done_condition, p_delay);
    }

    /**
     * same as regular queue, but will wait inputted delay time before running
     */
    public boolean queue(boolean p_asyncrhonous, boolean done_condition, double p_delay) {
        if (!firstLoop && currentlyQueueing >= queueElements.size() - 1) {
            currentlyQueueing = -1;
        }
        return queue(p_asyncrhonous, done_condition, !firstLoop && op.getRuntime() - queueElements.get(currentlyQueueing + 1).getReadyTime() > p_delay);
    }

    /**
     * same as regular queue, but will wait for extra_condition to be true before running
     */
    public void done(){
        queueElements.get(currentlyQueueing).setDone(true);
    }
    public boolean queue(boolean p_asynchronous, boolean done_condition, boolean extra_condition) {
        //create new queue element if it is first loop
        if (firstLoop) {
            createQueueElement(p_asynchronous);
        }

        //update which element is currently being queued & which event is currently being executed
        updateQueuer(done_condition);
        boolean isReady = false;
        //determine if currently queued element should be executed with extra_condition(probably position threshold)
        if (!queueElements.get(currentlyQueueing).isMustFinish() && !queueElements.get(currentlyQueueing).isShouldFinish()) {
            isReady = queueElements.get(currentlyQueueing).isReady(currentEvent, extra_condition);
        } else {
            isReady = queueElements.get(currentlyQueueing).isReady(completeCurrentEvent, extra_condition);

        }

        //set queueElement internal value
        if (isReady) {
            queueElements.get(currentlyQueueing).setStarted(true);
        }
        return isReady || queueElements.get(currentlyQueueing).isStarted() && !queueElements.get(currentlyQueueing).isDone();
    }

    public void reset() {
        queueElements.clear();
    }

    /**
     * create new queueElement
     */
    private void createQueueElement(boolean p_asynchrnous) {
        int startCondition = -1;
        if (!mustFinish) {
            boolean shouldFinish = false;
            for (int i = 0; i < queueElements.size(); i++) {
                if (!queueElements.get(queueElements.size() - i - 1).isAsynchronous() || queueElements.get(queueElements.size() - i - 1).isMustFinish()) {
                    startCondition = queueElements.size() - i - 1;
                    if (p_asynchrnous) {
                        if (i + 1 >= queueElements.size()) {
                            startCondition = -1;
                            break;
                        }
                        startCondition = queueElements.get(queueElements.size() - i - 1).startCondition;
                        shouldFinish = queueElements.get(queueElements.size() - i - 1).isMustFinish();
                    }
                    break;
                }
            }
            queueElements.add(new QueueElement(queueElements.size(), p_asynchrnous, startCondition, mustFinish, shouldFinish));
            logger.log("/RobotLogs/GeneralRobot", queueElements.size() - 1 + "StartCondition" + startCondition);
        } else {
            mustFinish = false;
            startCondition = mustStartCondition;
            queueElements.add(new QueueElement(queueElements.size(), p_asynchrnous, startCondition, true));
            logger.log("/RobotLogs/GeneralRobot", queueElements.size() - 1 + "mustStartCondition" + mustStartCondition);
        }
    }

    /**
     * update which element is currently being queued(processed) and which element is currently being executed
     */
    private void updateQueuer(boolean done_condition) {
        //update which element is currently being queued
        if (currentlyQueueing >= queueElements.size() - 1) {
            currentlyQueueing = 0;
        } else {
            currentlyQueueing++;
        }
        //update which event is currently being executed
        if (!firstLoop) {
            if (queueElements.get(currentlyQueueing).isStarted() && !queueElements.get(currentlyQueueing).isDone()) {
                queueElements.get(currentlyQueueing).setDone(done_condition);
                if (done_condition) {
                    calculateCompleteCurrentEvent();
                    logger.log("/RobotLogs/GeneralRobot", "event" + currentlyQueueing + "Done" + "completeEvents" + completeCurrentEvent);
                    if (currentlyQueueing > currentEvent && !queueElements.get(currentlyQueueing).isAsynchronous()) {
                        currentEvent = currentlyQueueing;
                        logger.log("/RobotLogs/GeneralRobot", "currentEvent" + currentEvent);
                    }
                }
            }
        }
    }

    private void calculateCompleteCurrentEvent() {
        for (int i = 0; i < queueElements.size(); i++) {
            if (queueElements.get(i).isDone()) {
                completeCurrentEvent = i;
            } else {
                break;
            }
        }
    }

    public void addDelay(double p_delay) {
        delay = p_delay;
    }

    public void waitForFinish() {
        waitForFinish(queueElements.size() - 1);
    }

    public void waitForFinish(int p_startCondition) {
        if (firstLoop) {
            mustFinish = true;
            mustStartCondition = p_startCondition;
        }
    }
}