package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class InvocationException extends RuntimeException
{
	/** Automatically generated */
	private static final long serialVersionUID = -2588400211554024832L;

	public InvocationException(InvocationError error)
	{
		this(error, null);
	}

	public InvocationException(InvocationError error, Throwable cause)
	{
		super("Template invocation error due to " + error.name(), cause);
	}

	public static enum InvocationError
	{
		EXTERNAL_IO, BAD_FORMAT
	}
}
