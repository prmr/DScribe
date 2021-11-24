/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.generation.doc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PrePostInfoFragment implements InfoFragment {

  private final Set<Statement> condition = new HashSet<>();
  private final Set<Statement> consequence = new HashSet<>();

  public PrePostInfoFragment(Statement condition, Statement consequence) {
    this.condition.add(condition);
    this.consequence.add(consequence);
  }

  @Override
  public FragmentType type() {
    return FragmentType.PRE_POST;
  }

  public static Set<InfoFragment> combine(Collection<InfoFragment> infos) {
    return new HashSet<>(combineConsequences(combineConditions(checkFragments(infos))));
  }

  @SuppressWarnings("unchecked")
  private static Collection<PrePostInfoFragment> checkFragments(
      Collection<InfoFragment> fragments) {
    for (InfoFragment fragment : fragments) {
      if (!(fragment instanceof PrePostInfoFragment)) {
        throw new IllegalArgumentException("Invalid statement class: " + fragment.getClass());
      }
    }
    return (Collection<PrePostInfoFragment>) (Collection<?>) fragments;
  }

  private static Collection<PrePostInfoFragment> combineConditions(
      Collection<PrePostInfoFragment> infos) {
    Map<Set<Statement>, PrePostInfoFragment> aggregated = new HashMap<>();
    for (PrePostInfoFragment info : infos) {
      Set<Statement> key = info.consequence;
      PrePostInfoFragment aggregate = aggregated.get(key);
      if (aggregate == null) {
        aggregated.put(key, info);
      } else {
        aggregate.condition.addAll(info.condition);
      }
    }
    for (PrePostInfoFragment info : aggregated.values()) {
      Set<Statement> recombined = Statement.combine(info.condition);
      info.condition.clear();
      info.condition.addAll(recombined);
    }
    return aggregated.values();
  }

  private static Collection<PrePostInfoFragment> combineConsequences(
      Collection<PrePostInfoFragment> infos) {
    Map<Set<Statement>, PrePostInfoFragment> aggregated = new HashMap<>();
    for (PrePostInfoFragment info : infos) {
      Set<Statement> key = info.condition;
      PrePostInfoFragment aggregate = aggregated.get(key);
      if (aggregate == null) {
        aggregated.put(key, info);
      } else {
        aggregate.consequence.addAll(info.consequence);
      }
    }
    for (PrePostInfoFragment info : aggregated.values()) {
      Set<Statement> recombined = Statement.combine(info.consequence);
      info.consequence.clear();
      info.consequence.addAll(recombined);
    }
    return aggregated.values();
  }

  @Override
  public String print() {
    return "IF " + join(condition, true) + ", THEN " + join(consequence, false);
  }

  private String join(Set<Statement> statements, boolean isCondition) {
    String conjunction;
    if (isCondition) {
      conjunction = " OR ";
    } else {
      conjunction = " AND ";
    }
    return statements.stream().map(Statement::print).collect(Collectors.joining(conjunction));
  }

  @Override
  public String toString() {
    return "Info <PRE: " + condition + ", POST: " + consequence + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((condition == null) ? 0 : condition.hashCode());
    result = prime * result + ((consequence == null) ? 0 : consequence.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PrePostInfoFragment other = (PrePostInfoFragment) obj;
    if (condition == null) {
      if (other.condition != null) {
        return false;
      }
    } else if (!condition.equals(other.condition)) {
      return false;
    }
    if (consequence == null) {
      if (other.consequence != null) {
        return false;
      }
    } else if (!consequence.equals(other.consequence)) {
      return false;
    }
    return true;
  }
}
