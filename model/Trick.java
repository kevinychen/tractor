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

    public Play getPlayByID(int playerID)
    {
        for (Play play : plays)
            if (play.getPlayerID() == playerID)
                return play;

        return null;
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

    public Play getInitialPlay()
    {
        return plays.get(0);
    }
}
