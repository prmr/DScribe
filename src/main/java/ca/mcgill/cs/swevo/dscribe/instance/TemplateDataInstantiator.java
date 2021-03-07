package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ca.mcgill.cs.swevo.dscribe.annotation.DScribeAnnotations;
import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;

public class TemplateDataInstantiator extends VoidVisitorAdapter<TestClass>
{
	private static final List<String> DS_ANN_NAMES = DScribeAnnotations.annotations();
	private static final ParamCollector PARAM_COLLECTOR = new ParamCollector();
	private static final ParamValueResolver PARAM_VAL_RESOLVER = new ParamValueResolver();
	
	
	@Override
	public void visit(ClassOrInterfaceDeclaration n, TestClass testClass)
	{
        for (AnnotationExpr annExpr: n.getAnnotations()) 
        	annExpr.accept(this, testClass);
    }

	@Override
	public void visit(NormalAnnotationExpr annExpr, TestClass testClass)
	{
		if (isDScribeAnnotation(annExpr))
        {
			FocalClass focalClass = testClass.focalClass();
        	String templateName = annExpr.getNameAsString();
        	Map<String, String[]> placeholders = new HashMap<String, String[]>(); 
        	
        	for (Node node : annExpr.getChildNodes())
        		node.accept(PARAM_COLLECTOR, placeholders);      
        	
            FocalMethod focalMethod = new FocalMethod(placeholders.get("$uut$")[0]);
            placeholders.remove("$uut$");
            TemplateInstance instance = new TemplateInstance(templateName, placeholders, annExpr);
            focalMethod.addTest(instance);
            focalClass.addFocalMethod(focalMethod);
        }
	}
	
	private boolean isDScribeAnnotation(AnnotationExpr annExpr)
	{
		String annName = annExpr.resolve().getQualifiedName();
        return DS_ANN_NAMES.contains(annName);
	}
	
	private static class ParamCollector extends VoidVisitorAdapter<Map<String, String[]>>
	{
		@Override
		public void visit(MemberValuePair mvp, Map<String, String[]> params)
		{
			List<String> vals = new ArrayList();
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
			for (Expression expr : aie.getValues())
				expr.accept(this, vals);
		}
	}
}
