package ca.mcgill.cs.swevo.dscribe.generation.doc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface InfoFragment
{
	public FragmentType type();

	public String print();

	public static Set<InfoFragment> combine(Collection<InfoFragment> fragments)
	{
		Map<FragmentType, Set<InfoFragment>> sets = new HashMap<>();
		for (InfoFragment fragment : fragments)
		{
			sets.computeIfAbsent(fragment.type(), t -> new HashSet<>()).add(fragment);
		}
		Set<InfoFragment> combined = new HashSet<>();
		for (FragmentType type : sets.keySet())
		{
			combined.addAll(type.combine(sets.get(type)));
		}
		return combined;
	}

	public static enum FragmentType
	{
		PRE_POST(PrePostInfoFragment::combine), CUSTOM(x -> x);

		private final Function<Set<InfoFragment>, Set<? extends InfoFragment>> combiner;

		private FragmentType(Function<Set<InfoFragment>, Set<? extends InfoFragment>> combiner)
		{
			this.combiner = combiner;
		}

		public Set<? extends InfoFragment> combine(Set<InfoFragment> fragments)
		{
			return combiner.apply(fragments);
		}
	}
}