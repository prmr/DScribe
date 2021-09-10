package ca.mcgill.cs.swevo.dscribe.template.invocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * The FocalTemplateInvocationExtractor class extracts all DScribe template invocations that are present in a given
 * focal class. To do so, it visits the CompilationUnit of the focal class. For each MethodDeclation in the
 * CompilationUnit, it determines whether it has any DScribe annotations (template invocations). If so, it extracts all
 * the placeholder values and adds a new TemplateInvocation instances to the corresponding FocalMethod.
 * 
 * @author Alexa
 *
 */
public class FocalTemplateInvocationExtractor extends VoidVisitorAdapter<FocalClass>
{

	private TemplateRepository templateRepo;

	private static final InvocationDataCollector INVOCATION_DATA_COLLECTOR = new InvocationDataCollector();

	/**
	 * Create a new FocalMethod instance for the given method declaration and add it to the focal class. For each
	 * DScribe annotation that the method declaration has, create a corresponding TemplateInvocation instance and add it
	 * to the focal method.
	 * 
	 * @param methodDecl
	 *            the method declaration to check for template invocations
	 * @param focalClass
	 *            the production class that the methodDecl belongs to
	 */
	@Override
	public void visit(MethodDeclaration methodDecl, FocalClass focalClass)
	{
		List<String> params = new ArrayList<>();
		var methodName = methodDecl.getNameAsString();
		methodDecl.getParameters().forEach(p -> params.add(p.getTypeAsString()));
		var focalMethod = new FocalMethod(methodName, params);

		methodDecl.getAnnotations().forEach(a -> a.accept(INVOCATION_DATA_COLLECTOR, focalMethod));

		focalClass.addFocalMethod(focalMethod);
	}

	private static class InvocationDataCollector extends VoidVisitorAdapter<FocalMethod>
	{
		/**
		 * If the annotation expression is a DScribe annotation, extract the template invocation data and create a
		 * corresponding TemplateInvocation instance. Add the TemplateInvocation instance to the focal method.
		 */
		@Override
		public void visit(NormalAnnotationExpr annExpr, FocalMethod focalMethod)
		{
			if (Utils.isDScribeAnnotation(annExpr))
			{
				annExpr.addPair("uut", String.format("\"%s\"", focalMethod.getSignature()));
				var templateName = annExpr.getNameAsString();
				Map<String, String[]> placeholders = new HashMap<>();

				annExpr.getChildNodes().forEach(n -> n.accept(Utils.PLACEHOLDER_COLLECTOR, placeholders));

				var instance = new TemplateInvocation(templateName, placeholders, annExpr);
				focalMethod.addTest(instance);
			}
		}
	}
}
