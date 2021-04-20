package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ca.mcgill.cs.swevo.dscribe.annotation.DScribeAnnotations;

public class ExtractTemplateData 
{
	protected static final ParamCollector PARAM_COLLECTOR = new ParamCollector();
	protected static final ParamValueResolver PARAM_VAL_RESOLVER = new ParamValueResolver();
	private static final List<String> DS_ANN_NAMES = DScribeAnnotations.annotations();
	
	protected static boolean isDScribeAnnotation(AnnotationExpr annExpr)
	{
		String annName = annExpr.resolve().getQualifiedName();
        return DS_ANN_NAMES.contains(annName);
	}
	
	private static class ParamCollector extends VoidVisitorAdapter<Map<String, String[]>>
	{
		@Override
		public void visit(MemberValuePair mvp, Map<String, String[]> params)
		{
			List<String> vals = new ArrayList<String>();
			mvp.getValue().accept(PARAM_VAL_RESOLVER, vals);
			params.put("$"+mvp.getName().asString() + "$", vals.toArray(String[]::new));
		}
	}
	
	private static class ParamValueResolver extends VoidVisitorAdapter<List<String>>
	{
		@Override
		public void visit(StringLiteralExpr sle, List<String> vals)
		{
			vals.add(sle.asString());
		}
		
		@Override
		public void visit(ArrayInitializerExpr aie, List<String> vals)
		{
			aie.getValues().forEach(e -> e.accept(this, vals));
		}
	}
}
