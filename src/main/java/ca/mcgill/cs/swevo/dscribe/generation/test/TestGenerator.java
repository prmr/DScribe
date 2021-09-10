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
package ca.mcgill.cs.swevo.dscribe.generation.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.model.TestClass;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.invocation.TemplateInvocation;

public class TestGenerator extends Generator
{
	public TestGenerator(List<FocalTestPair> focalTestPairs, TemplateRepository templateRepository)
	{
		super(focalTestPairs, templateRepository);
	}

	/**
	 * Generate the unit test(s) (i.e., MethodDeclaration object) for the given template invocation. Add the unit
	 * test(s) to the test class' compilation unit.
	 * 
	 * @param focalClass
	 *            the production class that the method under test belongs to
	 * @param focalMethodDecl
	 *            the MethodDeclaration of the method under test
	 * @param testClass
	 *            the test class associated with the focal class
	 * @param invocation
	 *            the template invocation to generate the unit test(s) for
	 * @param template
	 *            the template referenced by the template invocation
	 */
	@Override
	protected void generate(FocalClass focalClass, MethodDeclaration focalMethodDecl, TestClass testClass,
			TemplateInvocation invocation, Template template)
	{
		var testClassCU = testClass.compilationUnit();
		ClassOrInterfaceDeclaration testClassDecl = (ClassOrInterfaceDeclaration) testClassCU.getType(0);
		Optional<UnitTestFactory> factory = template.getTestFactory();
		if (factory.isPresent())
		{
			MethodDeclaration testMethodDecl = factory.get().create(invocation);
			addTest(testClassDecl, testMethodDecl, invocation);
			addImports(testClassCU, template);
			moveAnnotation(focalMethodDecl, testMethodDecl, invocation.getAnnotationExpr());
		}
	}

	/**
	 * Move the template invocation (i.e., DScribe annotation) from the focal method to the resulting test method
	 * 
	 * @param focalMethodDecl
	 *            the declaration of the method under test
	 * @param testMethodDecl
	 *            the declaration of the resulting test method
	 * @param annExpr
	 *            the template invocation
	 */
	private void moveAnnotation(MethodDeclaration focalMethodDecl, MethodDeclaration testMethodDecl,
			NormalAnnotationExpr annExpr)
	{
		// Remove the annotation from focal method
		NodeList<AnnotationExpr> focalAnnotations = focalMethodDecl.getAnnotations();
		focalAnnotations.remove(annExpr);
		focalMethodDecl.setAnnotations(new NodeList<>(focalAnnotations));

		// Add the annotation to test method
		testMethodDecl.addAnnotation(annExpr);
	}

	/**
	 * Add the necessary import statements to the test class' compilation unit
	 * 
	 * @param cu
	 *            the compilation unit of the test class for which unit test(s) have been generated
	 * @param template
	 *            the template used to generate unit tests
	 */
	private void addImports(CompilationUnit cu, Template template)
	{
		List<ImportDeclaration> newImports = new ArrayList<>(template.getNecessaryImports());
		newImports.removeAll(cu.getImports());
		newImports.forEach(cu::addImport);
	}

	private void addTest(ClassOrInterfaceDeclaration testClassDecl, MethodDeclaration newTest,
			TemplateInvocation invocation)
	{
		if (invocation.isFromTestMethod())
		{
			MethodDeclaration oldTest = invocation.getOldTestMethod();
			testClassDecl.remove(oldTest);
		}
		testClassDecl.addMember(newTest);
	}
}
