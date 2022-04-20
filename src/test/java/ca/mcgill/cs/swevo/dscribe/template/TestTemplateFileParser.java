package ca.mcgill.cs.swevo.dscribe.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;

import ca.mcgill.cs.swevo.dscribe.setup.Setup;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.RepositoryException;


public class TestTemplateFileParser {

	List<Template> templateList;

	private void addTemplate(Template template) {
		templateList.add(template);
	}
	
	private TemplateFileParser fileParser;
	private CompilationUnit cu;
	
	public void parse(String templatePath) throws FileNotFoundException {
		cu = Setup.parse(System.getProperty("user.dir") + templatePath);
	}
	
	@BeforeEach
	void setup() {
		fileParser = new TemplateFileParser(this::addTemplate);
		templateList = new ArrayList<Template>();
	}
	
	@Test
	void test_VisitClassOrInterface_FindsClassName() throws FileNotFoundException {
		parse("/dscribe-test-data/valid_templates/TestTemplate.java");
		fileParser.visit(cu, new ArrayList<ImportDeclaration>());
		
		for (Template template : templateList) {
			//ignore top level class, where the convention is to have a null name and classname
			if (template.getName() != null) {
				assertEquals(template.getClassName(), "ValidTemplates");
			}
		}
	}
	
	@Test
	void test_VisitPackageDeclaration_FindsPackageName() throws FileNotFoundException {
		parse("/dscribe-test-data/valid_templates/TestTemplate.java");
		fileParser.visit(cu, new ArrayList<ImportDeclaration>());
		
		for (Template template : templateList) {
			assertEquals(template.getPackageName(), "$package$");
		}
	}
	
	@Test
	void test_TemplateFileParser_IgnoreNamelessTemplate() throws FileNotFoundException {
		parse("/dscribe-test-data/bad_templates/TestTemplate.java");
		fileParser.visit(cu.getClassByName("BadTemplates").get().getMethodsByName("noNameTemplate").get(0), new ArrayList<ImportDeclaration>());
		assertTrue(templateList.size()==0);
	}
	
	@Test
	void test_TemplateFileParser_ThrowsRepositoryExceptionWhenSingleTemplateAnnotation() throws FileNotFoundException {
		parse("/dscribe-test-data/bad_templates/TestTemplate.java");
		assertThrows(RepositoryException.class, 
				() -> fileParser.visit(cu.getClassByName("BadTemplates").get().getMethodsByName("singleAnnotationTemplateDecleration").get(0), new ArrayList<ImportDeclaration>()));
	}
	
	@Test
	void test_TemplateFileParser_ThrowsRepositoryExceptionWhenTemplateNameIsNotString() throws FileNotFoundException {
		parse("/dscribe-test-data/bad_templates/TestTemplate.java");
		assertThrows(RepositoryException.class,
				() -> fileParser.visit(cu.getClassByName("BadTemplates").get().getMethodsByName("templateNameNotString").get(0), new ArrayList<ImportDeclaration>()));
	}
	
	@Test
	void test_TemplateFileParser_AggregatesDocFactoryWhenJavadocPresent() throws FileNotFoundException {
		parse("/dscribe-test-data/valid_templates/TestTemplate.java");
		fileParser.visit(cu.getClassByName("ValidTemplates").get().getMethodsByName("hasJavadoc").get(0), new ArrayList<ImportDeclaration>());
		assertTrue(templateList.get(0).getDocFactory().isPresent());
	}
	
	@Test
	void test_TemplateFileParser_AggregatesJavadocOnClassLevelTemplate() throws FileNotFoundException {
		parse("/dscribe-test-data/valid_templates/TestTemplate.java");
		fileParser.visit(cu.getClassByName("ValidTemplates").get(), new ArrayList<ImportDeclaration>());
		for (Template template : templateList) {
			if (template.getName() != null && template.getName().equals("ClassLevelWithJavadoc")) {
				assertTrue(template.getDocFactory().isPresent());
			}
		}
	}
	
	private List<Placeholder> getPlaceholderOracles() {
		List<Placeholder> ret = new ArrayList<Placeholder>();
		ret.add(new Placeholder("$state$", PlaceholderType.EXPR));
		ret.add(new Placeholder("$exType$", PlaceholderType.EXCEPTION));
		ret.add(new Placeholder("$type$", PlaceholderType.TYPE));
		ret.add(new Placeholder("$method$", PlaceholderType.METHOD));
		ret.add(new Placeholder("$field$", PlaceholderType.FIELD));
		ret.add(new Placeholder("$params$", PlaceholderType.EXPR_LIST));
		return ret;
	}
	
	@Test
	void test_TemplateFileParser_AggregatesExpectedPlaceholderPairs() throws FileNotFoundException {
		parse("/dscribe-test-data/valid_templates/TestTemplate.java");
		fileParser.visit(cu.getClassByName("ValidTemplates").get().getMethodsByName("placeHolderTestTemplate").get(0), new ArrayList<ImportDeclaration>());
		List<Placeholder> foundPlaceholders = new ArrayList<Placeholder>();
		for (Template template : templateList) {
			for (Placeholder placeholder : template) {
				foundPlaceholders.add(placeholder);
			}
		}
		List<Placeholder> oraclePlaceholders = getPlaceholderOracles();
		assertTrue(oraclePlaceholders.size() == foundPlaceholders.size());
		for (int i = 0; i < foundPlaceholders.size(); ++i) {
			assertTrue(foundPlaceholders.get(i).getName().equals(oraclePlaceholders.get(i).getName()));
			assertSame(foundPlaceholders.get(i).getType(), oraclePlaceholders.get(i).getType());
		}
	}
	
	@Test
	void test_TemplateFileParser_InvalidPlaceholderTypeThrowsRepositoryException() throws FileNotFoundException {
		parse("/dscribe-test-data/bad_templates/TestTemplate.java");
		assertThrows(RepositoryException.class, 
				() -> fileParser.visit(cu.getClassByName("BadTemplates").get().getMethodsByName("annotationTypeDoesNotExist").get(0), 
						new ArrayList<ImportDeclaration>()));
	}
	
	@Test
	void test_TemplateFileParser_NotNormalAnnotationTypePlaceholderThrowsRepositoryException() throws FileNotFoundException {
		parse("/dscribe-test-data/bad_templates/TestTemplate.java");
		assertThrows(RepositoryException.class, 
				() -> fileParser.visit(cu.getClassByName("BadTemplates").get().getMethodsByName("notNormalTypesAnnotation").get(0), 
						new ArrayList<ImportDeclaration>()));
	}

}
