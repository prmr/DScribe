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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;
import ca.mcgill.cs.swevo.dscribe.template.Template;

/**
 * Generates the final compilation units in order to output tests. The Test
 * Generator performs its task without state, i.e. like a function Given the
 * correct inputs it will output the correct outputs. This idea of immutability
 * is to ensure: 1. Centralizes the responsibility of the object 2. Focus on the
 * core function / processing 3. In turn, enables decoupling the functionality,
 * the class can be used whenever/wherever easily
 */
public class TestGenerator extends Generator 
{
	public TestGenerator(List<FocalTestPair> focalTestPairs) 
	{
		super(focalTestPairs);
	}

	public void prepare(Context context) throws ReflectiveOperationException, URISyntaxException
	{
		super.prepare(context, false);
	}
	
	protected void addInvocations(FocalTestPair focalTestPair) 
	{
		FocalClass focalClass = focalTestPair.focalClass();
		for (FocalMethod focalMethod : focalClass) {
			for (TemplateInstance instance : focalMethod) {
				addDefaultPlaceholders(instance, focalClass, focalMethod);
				List<Template> templates = repository.get(instance.getName());
				for (Template template : templates) {
					addInvocation(focalTestPair.testClass(), focalClass.getMethodDeclaration(focalMethod), instance,
							template);
				}
			}
		}
	}

	private void addInvocation(TestClass testClass, MethodDeclaration methodDecl, TemplateInstance instance,
			Template template) 
	{
		CompilationUnit cu = testClass.compilationUnit();
		TypeDeclaration<?> type = cu.getType(0);
		Optional<UnitTestFactory> factory = template.getTestFactory();
		if (factory.isPresent()) 
		{
			MethodDeclaration method = factory.get().create(instance);
			type.addMember(method);
			addImports(cu, template);
			moveAnnotation(methodDecl, method, instance.getAnnotationExpr());
		}
	}

	private void moveAnnotation(MethodDeclaration methodDecl, MethodDeclaration method, NormalAnnotationExpr annExpr) 
	{
		List<AnnotationExpr> annotations = methodDecl.getAnnotations();
		annotations.remove(annExpr);
		methodDecl.setAnnotations(new NodeList<AnnotationExpr>(annotations));
		annExpr.addPair("uut", "\"" + methodDecl.getSignature().asString() + "\"");
		method.addAnnotation(annExpr);
	}

	private void addImports(CompilationUnit cu, Template template) 
	{
		List<ImportDeclaration> newImports = new ArrayList<>(template.getNecessaryImports());
		newImports.removeAll(cu.getImports());
		newImports.forEach(i -> cu.addImport(i));		
	}
}
