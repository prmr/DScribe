package ca.mcgill.cs.swevo.dscribe.template;

public class Placeholder
{
	private final String name;
	private final PlaceholderType type;

	public Placeholder(String name, PlaceholderType type)
	{
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public PlaceholderType getType()
	{
		return type;
	}
}
