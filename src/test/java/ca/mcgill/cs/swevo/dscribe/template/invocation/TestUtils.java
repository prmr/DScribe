package ca.mcgill.cs.swevo.dscribe.template.invocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 
 * @author lawrenceberardelli
 * 
 * Utils class acts as a container for private annotations visitors
 */
public class TestUtils {
	
	/*
	 * visitable stub to enable testing private inner visitors
	 */
	private class StubMemberValuePair implements Visitable {
		
		MemberValuePair mvPair;
		
		public StubMemberValuePair(String annotationPairName, Expression expr) 
		{
			SimpleName name = new SimpleName(annotationPairName);
			mvPair = new MemberValuePair(name, expr);
		}

		@Override
		public <R, A> R accept(GenericVisitor<R, A> v, A arg) 
		{
			return null;
		}

		@Override
		public <A> void accept(VoidVisitor<A> v, A arg) 
		{
			v.visit(mvPair, arg);	
		}
		
	}
	
	private Map<String, String[]> foundPlaceholders = new HashMap<String, String[]>();
	
	@SuppressWarnings("unchecked")
	@Test
	void test_placeholderCollector_AggregatesBasicPlaceholder() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException 
	{
		StubMemberValuePair stub = new StubMemberValuePair("factory", new ClassExpr());
		Class<?> utilClass = Class.forName("ca.mcgill.cs.swevo.dscribe.template.invocation.Utils");
		Field visitorField = utilClass.getDeclaredField("PLACEHOLDER_COLLECTOR");
		visitorField.setAccessible(true);
		Object visitor = visitorField.get(null);
		stub.accept((VoidVisitorAdapter<Map<String, String[]>>)visitor, foundPlaceholders);
		assertTrue(foundPlaceholders.containsKey("$factory$"));
		assertTrue(foundPlaceholders.size() == 1);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void test_placeholderCollector_AggregatesPlaceholderWithStringExpression() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		StubMemberValuePair stub = new StubMemberValuePair("factory", new StringLiteralExpr("new Factory()"));
		Class<?> utilClass = Class.forName("ca.mcgill.cs.swevo.dscribe.template.invocation.Utils");
		Field visitorField = utilClass.getDeclaredField("PLACEHOLDER_COLLECTOR");
		visitorField.setAccessible(true);
		Object visitor = visitorField.get(null);
		stub.accept((VoidVisitorAdapter<Map<String, String[]>>)visitor, foundPlaceholders);
		assertTrue(foundPlaceholders.containsKey("$factory$"));
		assertTrue(foundPlaceholders.size() == 1);
		String[] oracle = new String[] {"new Factory()"};
		assertEquals(Arrays.asList(foundPlaceholders.get("$factory$")), Arrays.asList(oracle));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void test_placeholderCollector_AggregatesPlaceholderWithArrayExpression() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException
	{
		NodeList<Expression> expressions = new NodeList<Expression>();
		expressions.add(new StringLiteralExpr("new Factory()"));
		expressions.add(new StringLiteralExpr("old Factory)("));
		StubMemberValuePair stub = new StubMemberValuePair("factory", new ArrayInitializerExpr(expressions));
		
		Class<?> utilClass = Class.forName("ca.mcgill.cs.swevo.dscribe.template.invocation.Utils");
		Field visitorField = utilClass.getDeclaredField("PLACEHOLDER_COLLECTOR");
		visitorField.setAccessible(true);
		Object visitor = visitorField.get(null);
		stub.accept((VoidVisitorAdapter<Map<String, String[]>>)visitor, foundPlaceholders);
		assertTrue(foundPlaceholders.containsKey("$factory$"));
		assertTrue(foundPlaceholders.size() == 1);
		String[] oracle = new String[] {"new Factory()", "old Factory)("};
		
		assertEquals(Arrays.asList(foundPlaceholders.get("$factory$")), Arrays.asList(oracle));
	}

}
