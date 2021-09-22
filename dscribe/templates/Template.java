package $package$;
import static org.junit.jupiter.api.Assertions.*;

import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotation.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class Template 
{	
	/**
	 * Throws $exType$ when $state$
	 */
	@Template("AssertThrows")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void $method$_When$state$_Throw$exType$()
	{
		assertThrows($exType$, () -> $factory$.$method$($params$)); 
	}
	
	/**
	 * Throws $exType$ when $state$
	 */
	@Template("AssertThrowsMessage")
	@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST, $message$=EXPR)
	@Test
	public void $method$_When$state$_Throw$exType$()
	{
		final Exception thrown = assertThrows($exType$, () -> $factory$.$method$($params$));
        assertEquals($message$, thrown.getMessage());
	}
	
	/**
	 * Performs a shallow copy of the object.
	 */
	@Template("ShallowClone")
    @Types($factory$=EXPR)
    @Test
    public void clone_ReturnShallowCopy() {
		$class$ initial = $factory$;
		$class$ cloned = initial.$method$();
		assertNotSame(initial, cloned);
		assertEquals(initial, cloned);
    }	

	/** Returns $bool$ when $state$ */
	@Template("AssertBool")
	@Types($state$=EXPR, $bool$=EXPR, $factory$=EXPR, $params$=EXPR_LIST)
	@Test
	public void $method$_When$state$_Return$bool$() {
		boolean actual = $factory$.$method$($params$); 
	    assert$bool$(actual);
	}
		
	
	@Template("EqualsContract")
	@Types($factory1$=EXPR, $factory2$=EXPR, $factory3$=EXPR)
	public class EqualsContractTest {
		
		@Test	
		public void when$class$sAreSame_ReturnTrue()
		{
			boolean actual = $factory1$.equals($factory1$);
			assertTrue(actual);
		}	
		
		@Test	
		public void whenAppliedSymmetrically_ReturnSameResult()
		{
			boolean actual1 = $factory1$.equals($factory2$);
			boolean actual2 = $factory2$.equals($factory1$);
			assertEquals(actual1, actual2);
		}
		
		@Test	
		public void when$class$sAreEqual_ReturnTrue() { 
			boolean actual = $factory1$.equals($factory3$);
			assertTrue(actual);
		}

		
		@Test
		public void when$class$sAreDifferent_ReturnFalse() {
			boolean actual = $factory1$.equals($factory2$);
			assertFalse(actual);
		}
		
		@Test	
		public void when$class$IsNull_ReturnFalse ()
		{
			boolean actual = $factory1$.equals(null);
			assertFalse(actual);
		}
	}
}