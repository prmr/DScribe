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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;

import ca.mcgill.cs.swevo.dscribe.parsing.UnitTestParser;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.InvocationException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.TestParsingException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.InvocationException.InvocationError;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.TestParsingException.ParsingError;

/**
 * Configuration for Test Generation. Contains configured tests (scaffolding templates to use and respective parameters)
 *
 * n.b This wrapper could evolve to have tool configuration specific parameters. For example, flags such as "ignore x",
 * etc.
 *
 */
public class TemplateInstances implements Iterable<FocalClass>
{
	private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
			.create();

	@Expose
	private List<FocalClass> classes = new ArrayList<>();

	public static TemplateInstances fromJson(Path jsonFile)
	{
		try (JsonReader reader = new JsonReader(Files.newBufferedReader(jsonFile, UTF_8)))
		{
			return GSON.fromJson(reader, TemplateInstances.class);
		}
		catch (IOException e)
		{
			throw new InvocationException(InvocationError.EXTERNAL_IO, e);
		}
	}

	public String toJson()
	{
		return GSON.toJson(this);
	}

	public static TemplateInstances fromFocalClasses(Collection<Class<?>> sources)
	{
		TemplateInstances instances = new TemplateInstances();
		for (Class<?> source : sources)
		{
			FocalClass root = initFocalClass(source);
			instances.addFocalClass(root);
		}
		return instances;
	}

	private static FocalClass initFocalClass(Class<?> source)
	{
		FocalClass root = new FocalClass(source.getCanonicalName());
		for (Method method : source.getDeclaredMethods())
		{
			if (method.isSynthetic())
			{
				continue;
			}
			String name = method.getName();
			List<String> parameters = new ArrayList<>();
			for (Class<?> param : method.getParameterTypes())
			{
				parameters.add(param.getCanonicalName());
			}
			FocalMethod focus = new FocalMethod(name, parameters);
			root.addFocalMethod(focus);
		}
		for (Constructor<?> constructor : source.getDeclaredConstructors())
		{
			String name = source.getSimpleName();
			List<String> parameters = new ArrayList<>();
			for (Class<?> param : constructor.getParameterTypes())
			{
				parameters.add(param.getCanonicalName());
			}
			FocalMethod focus = new FocalMethod(name, parameters);
			root.addFocalMethod(focus);
		}
		return root;
	}

	public static TemplateInstances fromUnitTests(CompilationUnit testClass, TemplateRepository repository)
	{
		String testPkg = testClass.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("");
		if (testClass.getTypes().size() != 1)
		{
			throw new TestParsingException(ParsingError.TEST_CLASS_FORMAT);
		}
		TypeDeclaration<?> typeDecl = testClass.getType(0);
		if (!typeDecl.isClassOrInterfaceDeclaration() || typeDecl.asClassOrInterfaceDeclaration().isInterface())
		{
			throw new TestParsingException(ParsingError.TEST_CLASS_FORMAT);
		}
		String testCls = typeDecl.getNameAsString();

		UnitTestParser parser = new UnitTestParser(repository);
		Map<String, Map<String, FocalMethod>> instances = new HashMap<>();
		for (MethodDeclaration unitTest : typeDecl.getMethods())
		{
			Optional<TemplateInstance> result = parser.parse(unitTest, testPkg, testCls);
			if (result.isPresent())
			{
				TemplateInstance instance = result.get();
				String pkg = instance.getPlaceholderValue("$package$").getValue();
				String cls = instance.getPlaceholderValue("$class$").getValue();
				String method = instance.getPlaceholderValue("$method$").getValue();
				List<String> params = null;
				if (instance.containsPlaceholder("$paramtypes$"))
				{
					params = instance.getPlaceholderValue("$paramtypes$").getValueAsList();
				}
				getOrCreateFocus(instances, pkg, cls, method, params).addTest(instance);
			}
		}
		return collectInstances(instances);
	}

	private static FocalMethod getOrCreateFocus(Map<String, Map<String, FocalMethod>> focuses, String pkg, String cls,
			String method, List<String> params)
	{
		String classKey = pkg + "." + cls;
		String methodKey = method;
		if (params != null)
		{
			methodKey += "\t" + params.stream().collect(Collectors.joining(","));
		}
		Map<String, FocalMethod> focalClass = focuses.computeIfAbsent(classKey, k -> new HashMap<>());
		return focalClass.computeIfAbsent(methodKey, k -> new FocalMethod(method, params));
	}

	private static TemplateInstances collectInstances(Map<String, Map<String, FocalMethod>> instances)
	{
		TemplateInstances result = new TemplateInstances();
		for (String focalClass : instances.keySet())
		{
			FocalClass cls = new FocalClass(focalClass);
			result.addFocalClass(cls);
			for (FocalMethod method : instances.get(focalClass).values())
			{
				cls.addFocalMethod(method);
			}
		}
		return result;
	}

	@Override
	public Iterator<FocalClass> iterator()
	{
		return classes.iterator();
	}

	public void addFocalClass(FocalClass focalClass)
	{
		classes.add(focalClass);
	}

	public List<String> validate(TemplateRepository repository)
	{
		List<String> warnings = new ArrayList<>();
		for (Iterator<FocalClass> iter = classes.iterator(); iter.hasNext();)
		{
			FocalClass focus = iter.next();
			boolean valid = focus.validate(warnings, repository);
			if (!valid)
			{
				iter.remove();
			}
		}
		return warnings;
	}
}
