package ca.mcgill.cs.swevo.dscribe.instance;

public class InstanceContext
{
	private final Class<?> type;

	public InstanceContext(Class<?> declaringType)
	{
		type = declaringType;
	}

	public String getPackageName()
	{
		return type.getPackageName();
	}
}
