/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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