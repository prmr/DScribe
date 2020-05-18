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
package ca.mcgill.cs.swevo.dscribe.parsing;

import java.util.ArrayList;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;

import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

public class UnitTestParser
{
	private final TemplateRepository repository;

	public UnitTestParser(TemplateRepository repository)
	{
		this.repository = repository;
	}

	public Optional<TemplateInstance> parse(MethodDeclaration test, String pkgName, String clsName)
	{
		for (String templateName : repository)
		{
			Template template = repository.get(templateName);
			Optional<UnitTestMatcher> matcher = template.getMatcher();
			if (matcher.isEmpty())
			{
				continue;
			}
			Optional<TemplateInstance> match = matcher.get().match(test, pkgName, clsName);
			if (match.isPresent() && match.get().validate(new ArrayList<>(), repository))
			{
				return match;
			}
		}
		return Optional.empty();
	}
}
