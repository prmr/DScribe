package ca.mcgill.cs.swevo.dscribe.template.invocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitor;

import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.setup.Setup;
import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * 
 * @author lawrenceberardelli
 *
 */

public class TestFocalTemplateInvocationExtractor {

	/*
	 * stub visitable to enable access to private inner annotation visitors
	 */
	private static class StubAnnotation implements Visitable {

		NormalAnnotationExpr annotation;

		public StubAnnotation(String[] keyNames, String[] values) 
		{
			assert keyNames.length == values.length;

			annotation = new NormalAnnotationExpr();
			for (int i = 0; i < keyNames.length; ++i) {
				annotation.addPair(keyNames[i], values[i]);
			}
		}

		@Override
		public <R, A> R accept(GenericVisitor<R, A> v, A arg) 
		{
			return null;
		}

		@Override
		public <A> void accept(VoidVisitor<A> v, A arg) 
		{
			v.visit(annotation, arg);
		}
	}

	private static TemplateRepository templateRepo;
	private static FocalTemplateInvocationExtractor extractor;
	private static FocalClass focalClass;

	@BeforeAll
	static void setupContext() 
	{
		templateRepo = new InMemoryTemplateRepository(System.getProperty("user.dir") + "/dscribe/templates");
	}

	@BeforeEach
	void setupTemplateRepo()
	{
		extractor = new FocalTemplateInvocationExtractor(templateRepo);
		Path path = null;
		try {
			path = Setup.getPathToClass(Setup.setupContext(), "top.MinimalUsage", "src");
		} catch (Exception e) {
			e.printStackTrace();
		}
		focalClass = new FocalClass(path);
	}

	private static String annotationString = "@AssertBools(factory = \"MinimalUsage\", falseParams = { \"22\", \"0\" }, falseState = \"IsEven\", trueParams = { \"23\", \"0\" }, trueState = \"IsOdd\", uut = \"isOdd(int,int)\")";

	@Test
	void test_invocationExtractor_FindsAllTemplateInvocations() throws FileNotFoundException 
	{
		CompilationUnit cu = Setup.parse(System.getProperty("user.dir") + "/testdata/top/MinimalUsage.java");
		boolean bFound = false;
		cu.accept(extractor, focalClass);
		for (FocalMethod method : focalClass) {
			for (TemplateInvocation invocation : method) {
				assertEquals(invocation.getAnnotationExpr().toString(), annotationString);
				bFound = true;
			}
		}
		assertTrue(bFound);
	}

	@Test
	void test_invocationExtractor_FindsFocalMethod() throws FileNotFoundException 
	{
		CompilationUnit cu = Setup.parse(System.getProperty("user.dir") + "/testdata/top/MinimalUsage.java");
		cu.accept(extractor, focalClass);
		List<String> foundMethods = new ArrayList<String>();
		for (FocalMethod method : focalClass) {
			foundMethods.add(method.getSignature());
		}
		List<String> oracle = new ArrayList<String>();
		oracle.add("isOdd(int,int)");
		assertEquals(new HashSet<String>(oracle), new HashSet<String>(foundMethods));
	}


	/*
	 * asserts that the invocation collector adds a UUT pair if one is not provided
	 */
	@SuppressWarnings("unchecked")
	@Test
	void test_invocationCollector_adds_UUTAnnotationPair()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		FocalMethod dummyMethod = new FocalMethod("Dummy", null);
		Field collector = extractor.getClass().getDeclaredField("INVOCATION_DATA_COLLECTOR");
		collector.setAccessible(true);
		
		StubAnnotation noUUT = new StubAnnotation(new String[] { "factory" }, new String[] { "\"MinimalUsage\"" });
		noUUT.accept((VoidVisitor<FocalMethod>) collector.get(extractor), dummyMethod);
		
		List<String> foundInvocations = new ArrayList<String>();
		for (TemplateInvocation invocation : dummyMethod) 
		{
			foundInvocations.add(invocation.getAnnotationExpr().toString());
		}
		
		List<String> oracle = new ArrayList<String>();
		oracle.add("@empty(factory = \"MinimalUsage\", uut = \"Dummy()\")");
		assertEquals(new HashSet<String>(oracle), new HashSet<String>(foundInvocations));
	}

	/*
	 * asserts that the invocation collector won't add a UUT if the user has provided one.
	 */
	@SuppressWarnings("unchecked")
	@Test
	void test_invocationCollector_no_duplicate_UUTAnnotationPair()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		FocalMethod dummyMethod = new FocalMethod("Dummy", null);
		Field collector = extractor.getClass().getDeclaredField("INVOCATION_DATA_COLLECTOR");
		collector.setAccessible(true);
		
		StubAnnotation withUUT = new StubAnnotation(new String[] { "factory", "uut" }, new String[] { "\"MinimalUsage\"", "\"Baz()\"" });
		withUUT.accept((VoidVisitor<FocalMethod>) collector.get(extractor), dummyMethod);
		
		List<String> foundInvocations = new ArrayList<String>();
		for (TemplateInvocation invocation : dummyMethod) 
		{
			foundInvocations.add(invocation.getAnnotationExpr().toString());
		}
		
		List<String> oracle = new ArrayList<String>();
		oracle.add("@empty(factory = \"MinimalUsage\", uut = \"Baz()\")");
		assertEquals(new HashSet<String>(oracle), new HashSet<String>(foundInvocations));
	}

}
