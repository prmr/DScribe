package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class SrcTemplateDataInstantiator extends VoidVisitorAdapter<FocalClass>
{
	private static final TemplateDataCollector TEMPLATE_DATA_COLLECTOR = new TemplateDataCollector();
	
	@Override
	public void visit(MethodDeclaration md, FocalClass focalClass) 
	{
		List<String> params = new ArrayList<String>();
		md.getParameters().forEach(p -> params.add(p.getTypeAsString()));
		FocalMethod focalMethod = new FocalMethod(md.getNameAsString(), params);
		md.getAnnotations().forEach(a -> a.accept(TEMPLATE_DATA_COLLECTOR, focalMethod));
		focalClass.addFocalMethod(focalMethod);
	}
	
	private static class TemplateDataCollector extends VoidVisitorAdapter<FocalMethod>
	{
		@Override
		public void visit(NormalAnnotationExpr annExpr, FocalMethod focalMethod)
		{
			if (ExtractTemplateData.isDScribeAnnotation(annExpr))
	        {
	        	String templateName = annExpr.getNameAsString();
	        	Map<String, String[]> placeholders = new HashMap<String, String[]>(); 
	        	
	        	annExpr.getChildNodes().forEach(n -> n.accept(ExtractTemplateData.PARAM_COLLECTOR, placeholders));  
	            TemplateInstance instance = new TemplateInstance(templateName, placeholders, annExpr);
	            focalMethod.addTest(instance);
	        }
		}
	}
	
	
}
