package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class RepositoryException extends RuntimeException
{
	/** Automatically generated */
	private static final long serialVersionUID = -1682109445828567902L;

	public RepositoryException(RepositoryError error)
	{
		this(error, null);
	}

	public RepositoryException(RepositoryError error, Throwable cause)
	{
		super("Template repository error due to " + error.name(), cause);
	}

	public static enum RepositoryError
	{
		BAD_CONNECTION, EXTERNAL_IO, MISSING_TEMPLATE, BAD_TEMPLATE
	}
}