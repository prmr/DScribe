package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class GenerationException extends RuntimeException
{
	/** Automatically generated */
	private static final long serialVersionUID = 5251901280853682051L;

	public GenerationException(GenerationError error)
	{
		this(error, null);
	}

	public GenerationException(GenerationError error, Throwable cause)
	{
		super("Generation error due to " + error.name(), cause);
	}

	public static enum GenerationError
	{
		MISSING_SOURCE_FILE, INVALID_SOURCE_FILE, IO_ERROR
	}
}
