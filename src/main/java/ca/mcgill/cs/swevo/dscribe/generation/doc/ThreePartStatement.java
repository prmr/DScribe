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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ThreePartStatement implements Statement {
	
	private final Set<String> subject = new HashSet<>();
	private final String relation;
	private final Set<String> object = new HashSet<>();
	
	private final String andOr;
	private final String anyAll;
	
	public ThreePartStatement(String subject, String relation, String object, boolean isCondition) {
		assert subject != null && relation != null && object != null;
		this.subject.add(format(subject));
		this.relation = format(relation);
		this.object.add(format(object));
		if (isCondition) {
			andOr = " or ";
			anyAll = "any of ";
		}
		else {
			andOr = " and ";
			anyAll = "all of ";
		}
	}
	
	private String format(String raw) {
		return raw.replaceAll("\\s+", " ").trim();
	}
	
	@Override
	public StatementType type() {
		return StatementType.THREE_PART;
	}
	
	public static Set<ThreePartStatement> combine(Collection<Statement> stmts) {
		Collection<ThreePartStatement> statements = checkStatements(stmts);
		Collection<List<ThreePartStatement>> sets = partitionByRelation(statements);
		Set<ThreePartStatement> combined = new HashSet<>();
		for (List<ThreePartStatement> set : sets) {
			combined.addAll(combineObjects(combineSubjects(set)));
		}
		return combined;
	}
	
	@SuppressWarnings("unchecked")
	private static Collection<ThreePartStatement> checkStatements(Collection<Statement> statements) {
		for (Statement statement : statements) {
			if (!(statement instanceof ThreePartStatement)) {
				throw new IllegalArgumentException("Invalid statement class: " + statement.getClass());
			}
		}
		return (Collection<ThreePartStatement>) (Collection<?>) statements;
	}
	
	private static Collection<List<ThreePartStatement>> partitionByRelation(Collection<ThreePartStatement> statements) {
		return statements.stream().collect(Collectors.groupingBy(s -> s.relation)).values();
	}
	
	private static Collection<ThreePartStatement> combineObjects(Collection<ThreePartStatement> statements) {
		Map<Set<String>, ThreePartStatement> aggregated = new HashMap<>();
		for (ThreePartStatement statement : statements) {
			Set<String> key = statement.subject;
			ThreePartStatement aggregate = aggregated.get(key);
			if (aggregate == null) {
				aggregated.put(key, statement);
			}
			else {
				aggregate.object.addAll(statement.object);
			}
		}
		return aggregated.values();
	}
	
	private static Collection<ThreePartStatement> combineSubjects(Collection<ThreePartStatement> statements) {
		Map<Set<String>, ThreePartStatement> aggregated = new HashMap<>();
		for (ThreePartStatement statement : statements) {
			Set<String> key = statement.object;
			ThreePartStatement aggregate = aggregated.get(key);
			if (aggregate == null) {
				aggregated.put(key, statement);
			}
			else {
				aggregate.subject.addAll(statement.subject);
			}
		}
		return aggregated.values();
	}
	
	@Override
	public String print() {
		return join(subject) + " " + relation + " " + join(object);
	}
	
	private String join(Set<String> items) {
		switch (items.size()) {
			case 0:
				throw new AssertionError();
			case 1:
				return items.iterator().next();
			case 2:
				Iterator<String> iter = items.iterator();
				return iter.next() + andOr + iter.next();
			default:
				return anyAll + items.stream().collect(Collectors.joining(", "));
		}
	}
	
	@Override
	public String toString() {
		return "Stmt <SUBJ: " + subject + ", REL: " + relation + ", OBJ: " + object + ">";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		ThreePartStatement other = (ThreePartStatement) obj;
		if (object == null) {
			if (other.object != null) {
				return false;
			}
		}
		else if (!object.equals(other.object)) {
			return false;
		}
		if (relation == null) {
			if (other.relation != null) {
				return false;
			}
		}
		else if (!relation.equals(other.relation)) {
			return false;
		}
		if (subject == null) {
			if (other.subject != null) {
				return false;
			}
		}
		else if (!subject.equals(other.subject)) {
			return false;
		}
		return true;
	}
}
