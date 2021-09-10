package ca.mcgill.cs.swevo.dscribe.template.invocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ca.mcgill.cs.swevo.dscribe.annotation.DScribeAnnotations;

/**
 * The TemplateInvaocationExtraction class provides helper functions to facilitate: (1) identifying template invocations
 * in source code; (2) extracting the placeholder values in the identified invocations
 * 
 * @author Alexa
 *
 */
public class Utils
{
	protected static final PlaceholderCollector PLACEHOLDER_COLLECTOR = new PlaceholderCollector();
	protected static final PlaceholderValueResolver PLACEHOLDER_VAL_RESOLVER = new PlaceholderValueResolver();
	private static final List<String> DS_ANN_NAMES = DScribeAnnotations.annotations();

	private Utils()
	{
	}

	/**
	 * Determine whether the given annotation expression represents a DScribe template invocation.
	 * 
	 * @param annExpr
	 *            the annotation expression node to check
	 * @return true if the annotation expression represents a DScribe template invocation. False otherwise.
	 */
	protected static boolean isDScribeAnnotation(AnnotationExpr annExpr)
	{
		String annName = annExpr.resolve().getQualifiedName();
		return DS_ANN_NAMES.contains(annName);
	}

	/**
	 * Add the placeholder name and value represented by the given MemberValuePair instance to the given map of
	 * placeholder.
	 * 
	 * @param memberValuePair
	 *            the memberValuePair to determine the placeholder name and value of
	 * @param placeholders
	 *            the map of existing placeholder names and values
	 *
	 */
	private static class PlaceholderCollector extends VoidVisitorAdapter<Map<String, String[]>>
	{
		@Override
		public void visit(MemberValuePair memberValuePair, Map<String, String[]> placeholders)
		{
			List<String> values = new ArrayList<>();
			memberValuePair.getValue().accept(PLACEHOLDER_VAL_RESOLVER, values);
			var paramName = String.format("$%s$", memberValuePair.getName());
			placeholders.put(paramName, values.toArray(String[]::new));
		}
	}

	private static class PlaceholderValueResolver extends VoidVisitorAdapter<List<String>>
	{
		@Override
		public void visit(StringLiteralExpr stringLiteralExpr, List<String> values)
		{
			values.add(stringLiteralExpr.asString());
		}

		@Override
		public void visit(ArrayInitializerExpr arrayInitilizerExpr, List<String> values)
		{
			arrayInitilizerExpr.getValues().forEach(e -> e.accept(this, values));
		}
	}
}
