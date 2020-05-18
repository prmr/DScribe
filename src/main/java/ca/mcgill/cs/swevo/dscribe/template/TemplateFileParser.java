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

import static ca.mcgill.cs.swevo.dscribe.utils.exceptions.RepositoryException.RepositoryError.BAD_TEMPLATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;

import ca.mcgill.cs.swevo.dscribe.generation.doc.DocumentationFactory;
import ca.mcgill.cs.swevo.dscribe.generation.test.UnitTestFactory;
import ca.mcgill.cs.swevo.dscribe.parsing.UnitTestMatcher;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.RepositoryException;

/**
 * Visits all classes or interface declarations. Each class accepts a visitor to check all methods within a class.
 * <p>
 * Accumulates a mapping of the Template name to its MethodDeclaration node.
 */
public class TemplateFileParser extends VoidVisitorAdapter<List<ImportDeclaration>>
{
	private final Consumer<Template> add;
	private String className = null;
	private String packageName = "";

	public TemplateFileParser(Consumer<Template> addMethod)
	{
		add = addMethod;
	}

	@Override
	public void visit(PackageDeclaration n, List<ImportDeclaration> arg)
	{
		packageName = n.getNameAsString();
		super.visit(n, arg);
	}

	@Override
	public void visit(ImportDeclaration importDeclaration, List<ImportDeclaration> imports)
	{
		imports.add(importDeclaration);
		super.visit(importDeclaration, imports);
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, List<ImportDeclaration> arg)
	{
		if (className != null)
		{
			System.out.println("TEMPLATE FILE ERROR: Template file cannot contain nested classes or interfacse.");
			return;
		}
		className = n.getNameAsString();
		super.visit(n, arg);
	}

	/**
	 * The visitor can be called on the compilation unit at a higher level. This method searches through all the classes
	 * and interfaces. It further visits the methods within a class or method.
	 */
	@Override
	public void visit(MethodDeclaration c, List<ImportDeclaration> imports)
	{
		MethodDeclaration method = c.clone();
		String templateName = templateName(method);
		List<Placeholder> types = templatePlaceholders(method);
		DocumentationFactory docFactory = null;
		if (method.hasJavaDocComment())
		{
			Javadoc javadoc = method.getJavadoc().get();
			docFactory = new DocumentationFactory(javadoc.getDescription().toText());
			method.removeJavaDocComment();
		}
		UnitTestFactory testFactory = null;
		UnitTestMatcher matcher = null;
		if (!method.isAbstract())
		{
			testFactory = new UnitTestFactory(method);
			matcher = new UnitTestMatcher(templateName, packageName, className, method);
		}
		Template scaffoldTemplateMethod = new Template(templateName, className, packageName, testFactory, docFactory,
				matcher, types, imports);
		add.accept(scaffoldTemplateMethod);
	}

	private String templateName(MethodDeclaration method)
	{
		Optional<AnnotationExpr> templateName = method.getAnnotationByName("Template");
		if (!templateName.isPresent())
		{
			throw new RepositoryException(BAD_TEMPLATE);
		}
		AnnotationExpr annotation = templateName.get();
		method.remove(annotation);
		if (!annotation.isSingleMemberAnnotationExpr())
		{
			throw new RepositoryException(BAD_TEMPLATE);
		}
		Expression annotationValue = annotation.asSingleMemberAnnotationExpr().getMemberValue();
		if (!annotationValue.isStringLiteralExpr())
		{
			throw new RepositoryException(BAD_TEMPLATE);
		}
		return annotationValue.asStringLiteralExpr().asString();
	}

	private List<Placeholder> templatePlaceholders(MethodDeclaration method)
	{
		Optional<AnnotationExpr> typesAnnotationOpt = method.getAnnotationByName("Types");
		if (typesAnnotationOpt.isEmpty())
		{
			return List.of();
		}
		List<Placeholder> types = new ArrayList<>();
		AnnotationExpr typesAnnotation = typesAnnotationOpt.get();
		if (!typesAnnotation.isNormalAnnotationExpr())
		{
			throw new RepositoryException(BAD_TEMPLATE);
		}
		for (MemberValuePair placeholder : typesAnnotation.asNormalAnnotationExpr().getPairs())
		{
			String name = placeholder.getNameAsString();
			String type = placeholder.getValue().toString();
			try
			{
				PlaceholderType resolvedType = PlaceholderType.valueOf(type.trim().toUpperCase());
				types.add(new Placeholder(name, resolvedType));
			}
			catch (IllegalArgumentException e)
			{
				throw new RepositoryException(BAD_TEMPLATE, e);
			}
		}
		method.remove(typesAnnotation);
		return types;
	}
}