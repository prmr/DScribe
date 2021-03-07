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
package ca.mcgill.cs.swevo.dscribe.generation.test;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.instance.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;
import ca.mcgill.cs.swevo.dscribe.template.Template;

/**
 * Generates the final compilation units in order to output tests. The Test Generator performs its task without state,
 * i.e. like a function Given the correct inputs it will output the correct outputs. This idea of immutability is to
 * ensure: 1. Centralizes the responsibility of the object 2. Focus on the core function / processing 3. In turn,
 * enables decoupling the functionality, the class can be used whenever/wherever easily
 */
public class TestGenerator extends Generator
{
	public TestGenerator(TestClass testClass)
	{
		super(testClass);
	}

	protected void addInvocations(TestClass testClass)
	{
		for (FocalMethod focalMethod : testClass.focalClass())
		{
			for (TemplateInstance instance : focalMethod)
			{
				addDefaultPlaceholders(instance, testClass.focalClass(), focalMethod);
				Template template = super.repository.get(instance.getName());
				addInvocation(testClass, instance, template);
			}		
		}
	}
	
	private void addInvocation(TestClass testClass, TemplateInstance instance, Template template)
	{
		CompilationUnit cu = testClass.compilationUnit();
		ClassOrInterfaceDeclaration type = cu.getType(0).asClassOrInterfaceDeclaration();
		Optional<UnitTestFactory> factory = template.getTestFactory();
		if (factory.isPresent())
		{
			MethodDeclaration method = factory.get().create(instance);
			type.addMember(method);
			addImports(cu, template);
			moveAnnotation(type, method, instance.getAnnotationExpr());
		}
	}

	private void moveAnnotation(ClassOrInterfaceDeclaration type, MethodDeclaration method, NormalAnnotationExpr annExpr) 
	{
		List<AnnotationExpr> annotations = type.getAnnotations();
		annotations.remove(annExpr);
		type.setAnnotations(new NodeList<AnnotationExpr>(annotations));
		method.addAnnotation(annExpr);		
	}

	private void addImports(CompilationUnit cu, Template template)
	{
		List<ImportDeclaration> newImports = new ArrayList<>(template.getNecessaryImports());
		newImports.removeAll(cu.getImports());
		for (ImportDeclaration newImport : newImports)
		{
			cu.addImport(newImport);
		}
	}

	@Override
	public List<Exception> generate()
	{
		List<Exception> exceptions = new ArrayList<>();
		CompilationUnit finalTestCu = testClass.compilationUnit();
		String className = finalTestCu.getType(0).getNameAsString();
		try (BufferedWriter fileWriter = Files.newBufferedWriter(testClass.path(), UTF_8))
		{
			fileWriter.write(finalTestCu.toString());
			System.out.println("Successfully generated tests for " + className + ".java");
		}
		catch (IOException exception)
		{
			exceptions.add(exception);
		}
		return exceptions;
	}
}
