package view;

import java.awt.Point;

public class CardPosition
{
    private double currX, currY;
    private boolean faceUp;
    private int destX, destY;
    private double snapRatio;

    public CardPosition(Point point, boolean faceUp)
    {
        this.currX = destX = point.x;
        this.currY = destY = point.y;
        this.faceUp = faceUp;
    }

    public int currX()
    {
        return (int) currX;
    }

    public int currY()
    {
        return (int) currY;
    }

    public boolean faceUp()
    {
        return faceUp;
    }

    public void setDest(Point dest, boolean faceUp, double snapRatio)
    {
        this.destX = dest.x;
        this.destY = dest.y;
        this.faceUp = faceUp;
        this.snapRatio = snapRatio;
    }

    public void snap()
    {
        currX = destX * snapRatio + currX * (1 - snapRatio);
        currY = destY * snapRatio + currY * (1 - snapRatio);

        if (Math.hypot(currX - destX, currY - destY) < 10)
        {
            currX = destX;
            currY = destY;
        }
    }
}
