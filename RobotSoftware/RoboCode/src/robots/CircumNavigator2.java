package robots;

import robocode.*;
import standardOdometer.Odometer;
import utils.CustomOdometer;
import utils.Position;

import static robocode.util.Utils.normalRelativeAngleDegrees;
import static utils.Math.distanceBetween2Points;
import static utils.Math.pythagorasTheorem;

public class CircumNavigator2 extends AdvancedRobot {
    private boolean raceCompleted = false;
    private boolean raceOngoing = false;
    private boolean robotScanned = false;
    private boolean totalDisplayed = false;
    private int robot = 1;

    private Odometer odometer = new Odometer("IsRacing", this);
    private CustomOdometer myOdometer;

    public void run() {
        addCustomEvent(odometer);
        myOdometer = new CustomOdometer("customOdometer",this);

        // Go to bottom left corner
        goTo(18,18);

        // Turn to top
        turnRight(360-getRadarHeading());

        // Check robot in starting point
        if(myOdometer.getPositions().contains(myOdometer.getStartingPoint()))
            raceOngoing = true;

        // Start scanning robots
        int j = 0;
        while(j<3) { // 3 robots => 3 scans
            if(!robotScanned) {
                // Scanning to the right side
                turnRadarRight(45);
                j++;
            }
        }

        goTo(18, 18);
    }

    public void goAroundRobot(){

        // Start turns
        for(int i=0;i<6;i++){
            ahead(10);
            turnRight(11.125);
        }
    }

    void goTo(double toX, double toY){
        double fromX = getX();
        double fromY = getY();
        double distance =  distanceBetween2Points(fromX, fromY, toX, toY);

        // Pythagoras theorem to calculate the complementary angel
        double complementaryAngle = pythagorasTheorem(fromX, fromY, toX, toY);

        double angleToTurn = 180-complementaryAngle;

        // Turn face to our desired position. getHeading because the robot doesn't start at exactly 0 degrees (north)
        turnLeft(normalRelativeAngleDegrees(angleToTurn + getHeading()));

        // Move on
        ahead(distance);
    }

    public void onCustomEvent(CustomEvent ev) {
        Condition cd = ev.getCondition();
        if (cd.getName().equals("IsRacing")) {
            raceCompleted = true;
            raceOngoing = false;
            this.odometer.getRaceDistance();
        }
        else if (cd.getName().equals("customOdometer")){
            if(!totalDisplayed){
                System.out.println("Total " + myOdometer.getTotal());
                totalDisplayed = true;
                this.myOdometer = null;
            }
        }
    }


    public void onScannedRobot(ScannedRobotEvent e) {
        if(raceOngoing && !robotScanned){
            robotScanned = true;
            // Place the radar normally
            turnRadarLeft(45);

            // Angle to the next robot
            double degreesToTurn;

            if(robot==1) {
                if(e.getBearing()>=0)
                    degreesToTurn = e.getBearing()-5;
                else
                    degreesToTurn = e.getBearing()+5;
            }
            else {
                if(e.getBearing()>=0)
                    degreesToTurn = e.getBearing()-10;
                else
                    degreesToTurn = e.getBearing()+10;
            }

            robot++;

            // Turn to the next robot
            turnRight(degreesToTurn);

            // Go ahead and stop just before
            ahead(e.getDistance());

            // Go around the robot
            goAroundRobot();

            robotScanned = false;
        }
    }

    public void onHitRobot(HitRobotEvent e){
        if(!raceOngoing){
            back(50);
            turnLeft(30);
            ahead(50);
            goTo(18,18);
        }
    }

    public void onStatus(StatusEvent event){
        if(event == null || event.getStatus() == null){
            System.out.println("Null Event or Status");
            return ;
        }
        RobotStatus rs = event.getStatus();
        Position current = new Position( rs.getX(), rs.getY());
        if(myOdometer != null){
            myOdometer.registerPosition(current);
        }

    }

}

