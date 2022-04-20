package $package$;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class ValidTemplates {
	
	/**
	 * throws $exType$ when $state$
	 */
	@Template("AssertThrows")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void hasJavadoc() {
	    assertThrows($exType$, () -> $factory$.$method$($params$)); 
	}
	
	@Template("AssertThrows")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR)
	@Test
	public void $method$NoParam_When$state$_Throw$exType$() {
	    assertThrows($exType$, () -> $factory$.$method$()); 
	}
	
	@Template("AssertThrows")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void $method$_When$state$_Throw$exType$() {
	    assertThrows($exType$, () -> $factory$.$method$($params$)); 
	}
	
	@Template("AssertThrows")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $statement$=EXPR, $params$=EXPR_LIST)
	@Test
	public void $method$_When$state$_Throw$exType$WithStatement() {
		$statement$;
		assertThrows($exType$, () -> $factory$.$method$($params$)); 
	}
	
	@Template("PlaceholderTestTemplate")
	@Types($state$=EXPR, $exType$=EXCEPTION, $type$=TYPE, $method$=METHOD, $field$=FIELD, $params$=EXPR_LIST) 
	@Test
	public void placeHolderTestTemplate() {
		
	}
	
	/**
	 * Create class level template skeleton
	 */
	@Template("ClassLevelWithJavadoc")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $statement$=EXPR, $params$=EXPR_LIST)
	@Nested
	public class NestedClassWithJavadoc {
		
	}

}
