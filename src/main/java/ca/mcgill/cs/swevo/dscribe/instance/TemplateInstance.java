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
package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.gson.annotations.Expose;

import ca.mcgill.cs.swevo.dscribe.template.Placeholder;
import ca.mcgill.cs.swevo.dscribe.template.PlaceholderType;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * A Template is a commonly seen form of code. It needs a set of params to create meaning.
 */

// rename TemplateData
public class TemplateInstance
{
	private InstanceContext context = null;

	@Expose
	private final String templateName;
	@Expose
	private final Map<String, PlaceholderValue> placeholders = new HashMap<>();
	private final NormalAnnotationExpr annExpr;

	public TemplateInstance(String templateName, Map<String, String[]> values, NormalAnnotationExpr annExpr)
	{
		this.templateName = templateName;
		this.annExpr = annExpr.clone();
		for (Entry<String, String[]> value : values.entrySet())
		{
			placeholders.put(value.getKey(), new PlaceholderValue(value.getValue()));
		}
	}
	
	public TemplateInstance(String templateName, Map<String, String[]> values)
	{
		this(templateName, values, null);
	}

	public void setContext(InstanceContext context)
	{
		this.context = context;
	}

	public String getName()
	{
		return templateName;
	}
	
	public NormalAnnotationExpr getAnnotationExpr()
	{
		return annExpr;
	}
	
	public boolean containsPlaceholder(String placeholder)
	{
		return placeholders.containsKey(placeholder);
	}

	public PlaceholderValue getPlaceholderValue(String placeholder)
	{
		return placeholders.get(placeholder);
	}

	public void addPlaceholder(String name, String... value)
	{
		placeholders.put(name, new PlaceholderValue(value));
	}

	public boolean validate(List<String> warnings, TemplateRepository repository)
	{
		if (!repository.contains(templateName))
		{
			warnings.add("TEMPLATE DOES NOT EXIST:" + templateName + " is not a valid template.");
			return false;
		}
		Template template = repository.get(templateName);
		Set<String> extraPlaceholders = new HashSet<>(placeholders.keySet());
		for (Placeholder placeholder : template)
		{
			String phName = placeholder.getName();
			extraPlaceholders.remove(phName);
			if (!containsPlaceholder(phName))
			{
				warnings.add("MISSING PLACEHOLDER: Missing value for placeholder " + phName + ".");
				return false;
			}
			PlaceholderValue value = getPlaceholderValue(phName);
			PlaceholderType type = placeholder.getType();
			if (!type.typeCheck(value, context))
			{
				warnings.add("PLACEHOLDER TYPE ERROR: Placeholder value " + value + " failed validation for type "
						+ type + ".");
				return false;
			}
		}
		if (!extraPlaceholders.isEmpty())
		{
			warnings.add("EXTRA PLACEHOLDERS: Unused placeholders " + extraPlaceholders + " for template "
					+ templateName + ".");
		}
		return true;
	}
}
