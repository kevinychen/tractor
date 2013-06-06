package model;

import java.io.Serializable;

public final class Player implements Serializable, Comparable<Player>
{
    private static final long serialVersionUID = 1L;

    public final int ID;
    public final String name;

    public Player(int ID, String name)
    {
        this.ID = ID;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Player [ID=" + ID + ", name=" + name + "]";
    }

    @Override
    public int compareTo(Player other)
    {
        return ID - other.ID;
    }
}
