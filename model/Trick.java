package model;

import java.util.ArrayList;
import java.util.List;

public class Trick
{
    private final List<Play> plays;
    
    public Trick()
    {
        plays = new ArrayList<Play>();
    }
     
    public void addPlay(Play play)
    {
        plays.add(play);
    }
    
    public List<Play> getPlays()
    {
        return new ArrayList<Play>(plays);
    }
    
    public int numPlays()
    {
        return plays.size();
    }
    
    public int numPoints()
    {
        int numPoints = 0;
        for (Play play : plays)
            numPoints += play.numPoints();
        return numPoints;
    }
}
