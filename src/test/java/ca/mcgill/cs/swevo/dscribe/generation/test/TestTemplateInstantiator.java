package ca.mcgill.cs.swevo.dscribe.generation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.expr.SimpleName;

import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.model.TestClass;
import ca.mcgill.cs.swevo.dscribe.setup.Setup;
import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.invocation.TemplateInvocation;

public class TestTemplateInstantiator 
{
	
	static FocalTestPair setup(String className, String testClassName, String targetFolder)
	{
		Path pathToClass = null;
		Path pathToTestClass = null;
		try {
			pathToClass = Setup.getPathToClass(Setup.setupContext(), className, targetFolder);
			pathToTestClass = Setup.getPathToClass(Setup.setupContext(), testClassName, targetFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		FocalClass classToParse = new FocalClass(pathToClass);
		TestClass testClassToParse = new TestClass(pathToTestClass);
		FocalTestPair pair = new FocalTestPair(classToParse, testClassToParse);
		pair.parseCompilationUnit(new JavaParser(new ParserConfiguration()));
		
		InMemoryTemplateRepository templateRepo = new InMemoryTemplateRepository(System.getProperty("user.dir") + "/dscribe/templates");
		pair.extractTemplateInvocations(templateRepo);
		return pair;
	}
	
	@Test
	void test_TemplateInstatiator_ParseWholeExpression() 
	{
		//setup the template invocation data
		FocalTestPair pair = setup("top.MinimalUsage", "top.TestMinimalUsage", "testdata");
		SimpleName statement = new SimpleName("$factory$");
		for (FocalMethod method : pair.focalClass()) {
			for (TemplateInvocation invocation : method) {
				SimpleName result = (SimpleName)statement.accept(new TemplateInstantiator(), invocation);
				assertEquals(result.asString(), "MinimalUsage");
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_ParseWithinIdentifier() 
	{
		FocalTestPair pair = setup("top.MinimalUsage", "top.TestMinimalUsage", "testdata");
		SimpleName statement = new SimpleName("When_$trueState$");
		for (FocalMethod method : pair.focalClass()) {
			for (TemplateInvocation invocation : method) {
				SimpleName result = (SimpleName)statement.accept(new TemplateInstantiator(), invocation);
				assertEquals(result.asString(), "When_IsOdd");
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_ThrowsExceptionWhenListTypeIsUsedInIdentifier() 
	{
		FocalTestPair pair = setup("top.MinimalUsage", "top.TestMinimalUsage", "testdata");
		SimpleName statement = new SimpleName("When_$trueParams$");
		for (FocalMethod method : pair.focalClass()) {
			for (TemplateInvocation invocation : method) {
				assertThrows(IllegalArgumentException.class, () -> statement.accept(new TemplateInstantiator(), invocation));
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_IgnoresIdentifierThatDoesNotExistInInvocation() 
	{
		FocalTestPair pair = setup("top.MinimalUsage", "top.TestMinimalUsage", "testdata");
		SimpleName statement = new SimpleName("$params$");
		for (FocalMethod method : pair.focalClass()) {
			for (TemplateInvocation invocation : method) {
				SimpleName result = (SimpleName) statement.accept(new TemplateInstantiator(), invocation);
				assertEquals(result.asString(), "$params$");
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_ReturnsCommaSeperatedStringWhenExpressionIsList() {
		FocalTestPair pair = setup("top.MinimalUsage", "top.TestMinimalUsage", "testdata");
		SimpleName expression = new SimpleName("$falseParams$");
		for (FocalMethod method : pair.focalClass()) {
			for (TemplateInvocation invocation : method) {
				SimpleName result = (SimpleName)expression.accept(new TemplateInstantiator(), invocation);
				assertEquals(result.asString(), "22, 0");
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_ThrowsExceptionWhenInvalidPlaceholder() 
	{
		FocalTestPair pair = setup("top.MinimalUsage", "top.TestMinimalUsage", "testdata");
		SimpleName statement = new SimpleName("When_$invalidplaceholder$");
		for (FocalMethod method : pair.focalClass()) {
			for (TemplateInvocation invocation : method) {
				assertThrows(IllegalArgumentException.class, () -> statement.accept(new TemplateInstantiator(), invocation));
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_ReturnsNullWhenEmptyPlaceholderValue()
	{
		FocalTestPair pair = setup("top.HasNestedClass", "top.TestHasNestedClass", "testdata");
		SimpleName statement = new SimpleName("$target$");
		for (FocalMethod method : pair.focalClass()) {
			if (method.getSignature().equals("stringize()")) {
				for (TemplateInvocation invocation : method) {
					assertNull(statement.accept(new TemplateInstantiator(), invocation));
				}
			}
		}
	}
	
	/*
	 * The template instantiator tries to recover from using a placeholder value of form "new ClassName()" 
	 * when used inside an identifier by removing the new keyword
	 */
	@Test
	void test_TemplateInstantiator_ReplacesnewKeywordWhenUsedInIdentifier() 
	{
		FocalTestPair pair = setup("top.HasNestedClass", "top.TestHasNestedClass", "testdata");
		SimpleName statement = new SimpleName("has_$factory$");
		for (FocalMethod method : pair.focalClass()) {
			if (method.getSignature().equals("divides(int,int)")) {			
				for (TemplateInvocation invocation : method) {
					
					assertEquals("has_HasNestedClass()", ((SimpleName)statement.accept(new TemplateInstantiator(), invocation)).asString());
				}
			}
		}
	}
	
	@Test
	void test_TemplateInstantiator_TrimsPlaceholderValueWithClassTypeWhenUsedInIdentifier()
	{
		FocalTestPair pair = setup("top.HasNestedClass", "top.TestHasNestedClass", "testdata");
		SimpleName statement = new SimpleName("with_$exType$");
		for (FocalMethod method : pair.focalClass()) {
			if (method.getSignature().equals("throwsWhen(String)")) {			
				for (TemplateInvocation invocation : method) {
					assertEquals("with_Exception", ((SimpleName)statement.accept(new TemplateInstantiator(), invocation)).asString());
				}
			}
		}
	}
}
