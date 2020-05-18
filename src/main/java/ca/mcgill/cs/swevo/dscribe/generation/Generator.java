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
package ca.mcgill.cs.swevo.dscribe.generation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstances;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

public abstract class Generator
{
	private TemplateRepository repository;
	private List<TemplateInstances> invocations;

	public void prepare(Context context)
	{
		repository = context.templateRepository();
		invocations = context.templateInstancesPaths().stream().map(TemplateInstances::fromJson)
				.collect(Collectors.toList());
		invocations.forEach(i -> i.validate(repository).forEach(System.out::println));
	}

	public final void loadInvocations()
	{
		for (TemplateInstances invocationSet : invocations)
		{
			for (FocalClass focalClass : invocationSet)
			{
				for (FocalMethod focalMethod : focalClass)
				{
					for (TemplateInstance instance : focalMethod)
					{
						addDefaultPlaceholders(instance, focalClass, focalMethod);
						Template template = repository.get(instance.getName());
						addInvocation(instance, template);
					}
				}
			}
		}
	}

	private void addDefaultPlaceholders(TemplateInstance instance, FocalClass focalClass, FocalMethod focalMethod)
	{
		instance.addPlaceholder("$package$", focalClass.getPackageName());
		String className = focalClass.getSimpleName();
		instance.addPlaceholder("$class$", className);
		String methodName = focalMethod.getName();
		if (methodName.equals(className))
		{
			methodName = "new " + methodName;
		}
		instance.addPlaceholder("$method$", methodName);
		Optional<List<String>> parameters = focalMethod.getParameters();
		if (parameters.isPresent())
		{
			List<String> paramNames = new ArrayList<>();
			int i = 0;
			for (String paramType : parameters.get())
			{
				instance.addPlaceholder("$paramtype" + i + "$", paramType);
				paramNames.add(paramType);
				i++;
			}
			instance.addPlaceholder("$paramtypes$", paramNames.toArray(String[]::new));
		}
	}

	protected abstract void addInvocation(TemplateInstance instance, Template template);

	public abstract List<Exception> generate();

	public abstract List<Path> output();
}
