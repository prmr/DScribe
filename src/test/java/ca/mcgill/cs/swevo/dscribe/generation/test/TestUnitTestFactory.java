package ca.mcgill.cs.swevo.dscribe.generation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.invocation.TemplateInvocation;

public class TestUnitTestFactory 
{
	private static String expected = "@Test\n"
			+ "public void throwsWhen_WhenParamIsNull_ThrowException() {\n"
			+ "    assertThrows(java.lang.Exception.class, () -> HasNestedClass.throwsWhen(null));\n"
			+ "}";
	
	private void addDefaultPlaceholders(TemplateInvocation invocation, FocalClass focalClass, FocalMethod method) 
	{
		try {
			Class<?> clazz = Class.forName("ca.mcgill.cs.swevo.dscribe.generation.Generator");
			Method methodDefaultPlaceholders = clazz.getDeclaredMethod("addDefaultPlaceholders", TemplateInvocation.class, FocalClass.class, FocalMethod.class);
			methodDefaultPlaceholders.setAccessible(true);
			methodDefaultPlaceholders.invoke(null, invocation, focalClass, method);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void test_create_MatchesExpectedOutput() {
		InMemoryTemplateRepository templateRepo = new InMemoryTemplateRepository(System.getProperty("user.dir") + "/dscribe/templates");
		FocalTestPair pair = TestTemplateInstantiator.setup("top.HasNestedClass", "top.TestHasNestedClass", "testdata");
		for (FocalMethod method : pair.focalClass()) {
			if (method.getSignature().equals("throwsWhen(String)")) {
				for (TemplateInvocation invocation : method) {
					addDefaultPlaceholders(invocation, pair.focalClass(), method);
					List<Template> template = templateRepo.get(invocation.getTemplateName());
					assertEquals(expected, template.get(0).getTestFactory().get().create(invocation).toString());
				}
			}
		}
	}
}
