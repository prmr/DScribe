package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class ConfigurationException extends RuntimeException
{
	/** Automatically generated */
	private static final long serialVersionUID = -4809926596198122536L;

	public ConfigurationException(ConfigurationError error)
	{
		this(error, null);
	}

	public ConfigurationException(ConfigurationError error, Throwable cause)
	{
		super("Configuration error due to " + error.name(), cause);
	}

	public enum ConfigurationError
	{
		INVALID_TEST_NAME_CONVENTION
	}
}
