package assignment_2;

/*
 * Name: Jan C. Rubio
 * EID: jcr4698
 */

// Methods may be added to this file, but don't remove anything
// Include this file in your final submission


public class GasStation {
    private int x,y; //coordicates
    private int id;
    double distance;
    boolean visited;
    private int upgrade_value;

    public GasStation(int _id, int _value, int _x, int _y) {
        id = _id;
        visited = false;
        upgrade_value = _value;
        x = _x;
        y = _y;
        distance = Double.MAX_VALUE;
    }


    public int getID() {
        return id;
    }

    public int getXcoordinate() {return x;}

    public int getYcoordinate() {return y;}

    public int[] getCoordinates(){return new int[]{x,y};}

    public int getUpgradeValue(){ return upgrade_value;}

    public String toString() {
        return "Gas Station " + getID();
    }

}

