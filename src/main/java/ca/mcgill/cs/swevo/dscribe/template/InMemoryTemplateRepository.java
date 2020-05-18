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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import ca.mcgill.cs.swevo.dscribe.utils.exceptions.RepositoryException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.RepositoryException.RepositoryError;

/**
 * Repository of all templateMethods: - Decouple access to templateMethods. - Ensures encapsulation - Isolate logic to
 * query in single class for ease.
 */
public class InMemoryTemplateRepository implements TemplateRepository
{
	// Data structure for the template methods
	private final Map<String, Template> templateMethods = new HashMap<>();

	public InMemoryTemplateRepository(String sourceFolder)
	{
		SourceRoot scaffolds = new SourceRoot(Paths.get(sourceFolder));
		try
		{
			scaffolds.tryToParse();
		}
		catch (IOException e)
		{
			throw new RepositoryException(RepositoryError.EXTERNAL_IO, e);
		}
		TemplateFileParser repositoryParser = new TemplateFileParser(this::addTemplate);
		for (CompilationUnit scaffoldCu : scaffolds.getCompilationUnits())
		{
			scaffoldCu.accept(repositoryParser, new ArrayList<>());
		}
	}

	/**
	 * Add a template method AST node given its name.
	 * 
	 * @param template
	 *            The template containing scaffold information.
	 */
	private void addTemplate(Template template)
	{
		templateMethods.put(template.getName(), template);
	}

	@Override
	public Template get(String name)
	{
		return templateMethods.get(name);
	}

	@Override
	public boolean contains(String name)
	{
		return templateMethods.containsKey(name);
	}

	@Override
	public Iterator<String> iterator()
	{
		return templateMethods.keySet().iterator();
	}
}
