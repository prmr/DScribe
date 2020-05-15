package ca.mcgill.cs.swevo.dscribe.generation.doc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface Statement
{
	public StatementType type();

	public String print();

	public static Set<Statement> combine(Collection<Statement> statements)
	{
		Map<StatementType, Set<Statement>> sets = new HashMap<>();
		for (Statement statement : statements)
		{
			sets.computeIfAbsent(statement.type(), t -> new HashSet<>()).add(statement);
		}
		Set<Statement> combined = new HashSet<>();
		for (StatementType type : sets.keySet())
		{
			combined.addAll(type.combine(sets.get(type)));
		}
		return combined;
	}

	public static enum StatementType
	{
		THREE_PART(ThreePartStatement::combine), CUSTOM(x -> x);

		private Function<Set<Statement>, Set<? extends Statement>> combiner;

		private StatementType(Function<Set<Statement>, Set<? extends Statement>> combiner)
		{
			this.combiner = combiner;
		}

		public Set<? extends Statement> combine(Set<Statement> statements)
		{
			return combiner.apply(statements);
		}
	}
}