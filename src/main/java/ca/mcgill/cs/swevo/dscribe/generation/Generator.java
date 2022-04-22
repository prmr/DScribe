/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.generation;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.model.TestClass;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.invocation.TemplateInvocation;

public abstract class Generator
{
	private final TemplateRepository templateRepo;
	private final JavaParser parser;
	private final List<FocalTestPair> focalTestPairs;

	protected Generator(List<FocalTestPair> focalTestPairs, TemplateRepository templateRepo)
	{
		assert focalTestPairs != null && templateRepo != null;
		this.focalTestPairs = new ArrayList<>(focalTestPairs);
		this.templateRepo = templateRepo;
		parser = initParser();
	}

	/**
	 * Prepare for test or documentation generation by doing the following for each FocalTestPair: (1) Parse the focal
	 * and test classes into compilation units; (2) Extract all template invocations (i.e., DScribe Annotations) from
	 * the compilation units; (3) Remove invalid template invocations.
	 */
	public final void prepare()
	{
		focalTestPairs.forEach(pair -> {
			pair.parseCompilationUnit(parser);
			pair.extractTemplateInvocations(templateRepo);
			pair.validateTemplateInvocations(templateRepo);
		});
	}

	/**
	 * For each template invocation in the focalTestPairs, generate the corresponding unit test or documentation
	 * fragment.
	 * 
	 * @return
	 */
	public final List<String> generate()
	{
		preGenerate();
		for (FocalTestPair focalTestPair : focalTestPairs)
		{
			var focalClass = focalTestPair.focalClass();
			for (FocalMethod focalMethod : focalClass)
			{
				for (TemplateInvocation invocation : focalMethod)
				{
					addDefaultPlaceholders(invocation, focalClass, focalMethod);
					List<Template> templates = templateRepo.get(invocation.getTemplateName());
					for (Template template : templates)
					{
						generate(focalClass, focalClass.getMethodDeclaration(focalMethod), focalTestPair.testClass(),
								invocation, template);
					}
				}
			}
		}
		return postGenerate();
	}

	/**
	 * For each FocalTestPair, save the modified focal and test classes to file.
	 * 
	 * @return a list containing any exceptions that occurred while writing to file
	 */
	public List<Exception> save()
	{
		List<Exception> exceptions = new ArrayList<>();
		focalTestPairs.forEach(ftp -> ftp.writeToFile(exceptions));
		return exceptions;
	}

	/**
	 * Add the default placeholder values to the given template invocation
	 * 
	 * @param invocation
	 *            the template invocation for which to add the default parameters
	 * @param focalClass
	 *            the production class containing the method under test
	 * @param focalMethod
	 *            the method under test
	 */
	private static void addDefaultPlaceholders(TemplateInvocation invocation, FocalClass focalClass, FocalMethod focalMethod)
	{
		invocation.addPlaceholder("$package$", focalClass.getPackageName());
		String className = focalClass.getSimpleName();
		invocation.addPlaceholder("$class$", className);
		String methodName = focalMethod.getName();

		if (methodName.equals(className))
		{
			methodName = "new " + methodName;
		}
		invocation.addPlaceholder("$method$", methodName);
		List<String> paramNames = new ArrayList<>();
		var i = 0;
		for (String paramType : focalMethod.getParameters())
		{
			invocation.addPlaceholder("$paramtype" + i + "$", paramType);
			paramNames.add(paramType);
			i++;
		}
		invocation.addPlaceholder("$paramtypes$", paramNames.toArray(String[]::new));
	}

	/**
	 * Initialize a JavaParser instance with the desired configuration
	 * 
	 * @return the initialized JavaParser instance
	 */
	private static JavaParser initParser()
	{
		var config = new ParserConfiguration();
		config.setLanguageLevel(LanguageLevel.CURRENT);
		config.setTabSize(4);
		config.setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver(false)));
		return new JavaParser(config);
	}

	protected abstract void generate(FocalClass focalClass, MethodDeclaration focalMethodDecl, TestClass testClass,
			TemplateInvocation invocation, Template template);

	protected abstract void preGenerate();

	protected abstract List<String> postGenerate();
}
