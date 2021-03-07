package $package$;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class $class$Test { 
	
	@Template("AssertBool")
    @Types($state$=EXPR, $bool$=EXPR, $factory$=EXPR, $params$=EXPR_LIST)
    @Test
    public void test_$method$_$state$() {
        assert$bool$($factory$.$method$($params$));
    }
	
	@Template("assertEquals")
    @Types($state$=EXPR, $oracle$=EXPR, $factory$=EXPR, $params$=EXPR_LIST)
    @Test
    public void test_$method$_$state$() {
        assertEquals($oracle$, $factory$.$method$($params$));
    }
	
	@Template("assertEqualsDelta")
    @Types($state$=EXPR, $oracle$=EXPR, $factory$=EXPR, $params$=EXPR_LIST, $delta$=EXPR)
    @Test
    public void test_$method$_$state$() {
        assertEquals($oracle$, $factory$.$method$($params$), $delta$);
    }	
	
	@Template("testClone")
    @Types($state$=EXPR, $factory$=EXPR)
    @Test
    public void test_$method$_$state$() {
		$class$ initial = $factory$;
		$class$ cloned = initial.$method$();
		assertNotSame(initial, cloned);
		assertEquals(initial, cloned);
    }
	
	@Template("testConstructor")
	@Types($state$=EXPR, $params$=EXPR_LIST, $oracle1$=EXPR, $getter1$=method, $oracle2$=EXPR, $getter2$=method)
	@Test
	public void test_$method$_$state$() {
		$class$ obj = $method$($params$);
		assertEquals($oracle1$, obj.$getter1$());
		assertEquals($oracle2$, obj.$getter2$());
	}
	
	@Template("testMethod")
	@Types($state$=EXPR, $factory$=EXPR, $params$=EXPR_LIST, $oracle1$=EXPR, $getter1$=method, $delta1$=EXPR, $oracle2$=EXPR, $getter2$=method, $delta2$=EXPR)
	@Test
	public void test_$method$_$state$() {
		$class$ obj = $factory$.$method$($params$);
		assertEquals($oracle1$, obj.$getter1$(), $delta1$);
		assertEquals($oracle2$, obj.$getter2$(), $delta2$);
	}
	
}