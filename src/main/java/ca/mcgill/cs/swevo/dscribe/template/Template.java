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
package ca.mcgill.cs.swevo.dscribe.template;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.ImportDeclaration;

import ca.mcgill.cs.swevo.dscribe.generation.doc.DocumentationFactory;
import ca.mcgill.cs.swevo.dscribe.generation.test.UnitTestFactory;
import ca.mcgill.cs.swevo.dscribe.parsing.UnitTestMatcher;

public class Template implements Iterable<Placeholder>
{
	private final String name;
	private final String className;
	private final String packageName;
	private final Optional<UnitTestFactory> testFactory;
	private final Optional<DocumentationFactory> docFactory;
	private final Optional<UnitTestMatcher> matcher;
	private final List<Placeholder> placeholders;
	private final List<ImportDeclaration> necessaryImports;

	public Template(String name, String className, String packageName, UnitTestFactory testFactory,
			DocumentationFactory docFactory, UnitTestMatcher matcher, List<Placeholder> placeholders,
			List<ImportDeclaration> necessaryImports)
	{
		this.name = name;
		this.className = className;
		this.packageName = packageName;
		this.testFactory = Optional.ofNullable(testFactory);
		this.docFactory = Optional.ofNullable(docFactory);
		this.matcher = Optional.ofNullable(matcher);
		this.placeholders = List.copyOf(placeholders);
		this.necessaryImports = List.copyOf(necessaryImports);
	}

	public String getClassName()
	{
		return className;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public Optional<UnitTestFactory> getTestFactory()
	{
		return testFactory;
	}

	public Optional<DocumentationFactory> getDocFactory()
	{
		return docFactory;
	}

	public Optional<UnitTestMatcher> getMatcher()
	{
		return matcher;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public Iterator<Placeholder> iterator()
	{
		return placeholders.iterator();
	}

	public List<ImportDeclaration> getNecessaryImports()
	{
		return Collections.unmodifiableList(necessaryImports);
	}
}
