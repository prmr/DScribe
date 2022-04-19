package $package$;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.*;

public class BadTemplates {
	
	@Template(AssertThrows)
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void templateNameNotString() {
	}
	
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void noNameTemplate() {
	}
	
	@Template
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void singleAnnotationTemplateDecleration() {
		
	}
	
	@Template("BadAnnotationType")
	@Types($state$=DNE)
	@Test
	public void annotationTypeDoesNotExist() {
		
	}
	
	@Template("NotNormalTypesAnnotation")
	@Types(state)
	@Test
	public void notNormalTypesAnnotation() {
		
	}
}
