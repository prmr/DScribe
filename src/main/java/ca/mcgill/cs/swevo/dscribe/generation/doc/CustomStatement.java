package ca.mcgill.cs.swevo.dscribe.generation.doc;

public class CustomStatement implements Statement
{
	private final String statement;

	public CustomStatement(String statement)
	{
		this.statement = statement;
	}

	@Override
	public StatementType type()
	{
		return StatementType.CUSTOM;
	}

	@Override
	public String print()
	{
		return statement;
	}

	@Override
	public String toString()
	{
		return "STMT <" + statement + ">";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((statement == null) ? 0 : statement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		CustomStatement other = (CustomStatement) obj;
		if (statement == null)
		{
			if (other.statement != null)
			{
				return false;
			}
		}
		else if (!statement.equals(other.statement))
		{
			return false;
		}
		return true;
	}
}
