package $package$;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import ca.mcgill.cs.swevo.dscribe.annotation.DScribeAnnotations.*;
import org.junit.jupiter.api.Test;

public class Template 
{	
	@Template("AssertThrows")
	@Types($state$=EXPR, $factory$=EXPR, $exType$=EXCEPTION, $params$=EXPR_LIST)
	@Test
	public void $method$_when$state$_throws$exType$()
	{
		assertThrows($exType$.class, () -> $factory$.$method$($params$)); 
	}
//	
//	
//	@Template("LogTrace")
//	@Types($state$=EXPR, $pattern$=EXPR, $arg$=EXPR, $oracle$=EXPR)
//	@Test
//	public void testTrace$state$()
//	{
//		LogCapture capture = new LogCapture(TRACE);
//	    logger.setLevel(TRACE);
//	    $class$.trace(logger, $pattern$, $arg$);
//	    assertEquals($oracle$, capture.getMessage());	
//	}
//	

	/** $method$ returns $bool$ when called with $params$ */
	@Template("AssertBool")
	@Types($state$=EXPR, $bool$=EXPR, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void test_$method$_$state$_$bool$() {
		boolean actual = $factory$.$method$($params$); 
	    assert$bool$(actual);
	}
	
//	@Template(“EqualsContract”)
//	@Types($factory1$=EXPR, $factory2$=EXPR, $factory3$=EXPR)
//	@Nested
//	@DisplayName(“Test equals contract”)
//	class EqualsContract {
//		@Test	
//		public void test_equals_reflevixe()
//		{
//			boolean result = $factory1$.equals($factory1$);
//			assertTrue(result);
//		}	
//	
//		@Test	
//		public void test_equals_symmetric()
//		{
//			boolean result1 = $factory1$.equals($factory2$);
//			boolean result2 = $factory2$.equals($factory1$);
//			assertEquals(result1, result2);
//		}
//	
//		@Test	
//		public void test_equals_transitive ()
//		{
//			boolean result1 = $factory1$.equals($factory2$);
//			boolean result2 = $factory2$.equals($factory3$);
//			boolean result3 = $factory1$.equals($factory3$);
//			assertTrue(result1);
//			assertTrue(result2);
//			assertTrue(result3);
//		}
//	
//		@Test	
//		public void test_equals_null ()
//		{
//			boolean result = $factory1$.equals(null);
//			assertFalse(result);
//		}	
//			// Test hashCode
//	}

	
//	@Template("ShallowClone")
//    @Types($factory$=EXPR)
//    @Test
//    public void test_whenClone_returnsShallowCopy() {
//		$class$ initial = $factory$;
//		$class$ cloned = initial.$method$();
//		assertNotSame(initial, cloned);
//		assertEquals(initial, cloned);
//    }
}