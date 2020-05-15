package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class TestParsingException extends RuntimeException
{
	/** Automatically generated */
	private static final long serialVersionUID = 3352721447167309965L;

	public TestParsingException(ParsingError error)
	{
		this(error, null);
	}

	public TestParsingException(ParsingError error, Throwable cause)
	{
		super("Parsing error due to " + error.name(), cause);
	}

	public static enum ParsingError
	{
		TEST_CLASS_FORMAT, INCONSISTENT_PLACEHOLDERS, IO_ERROR
	}
}
