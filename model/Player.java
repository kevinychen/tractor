package model;

public final class Player
{
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
}
