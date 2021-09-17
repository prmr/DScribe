package ca.mcgill.cs.swevo.dscribe.template.invocation;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * The TestTemplateInvocationExtractor class extracts all DScribe template invocations that are present in a given test
 * class. To do so, it visits the CompilationUnit of the test class. For each MethodDeclation in the CompilationUnit, it
 * determines whether it has any DScribe annotations (template invocations). If so, it extracts all the placeholder
 * values and adds a new TemplateInvocation instances to the corresponding FocalMethod.
 * 
 * @author Alexa
 *
 */
public class TestTemplateInvocationExtractor extends VoidVisitorAdapter<FocalClass>
{
	private static final InvocationDataCollector INVOCATION_DATA_COLLECTOR = new InvocationDataCollector();
	private final TemplateRepository templateRepo;

	public TestTemplateInvocationExtractor(TemplateRepository templateRepo)
	{
		assert templateRepo != null;
		this.templateRepo = templateRepo;
	}

	@Override
	public void visit(MethodDeclaration md, FocalClass focalClass)
	{
		md	.getAnnotations()
			.stream()
			.filter(a -> templateRepo.contains(a.getNameAsString())) // filter out non-dscribe annotationS
			.forEach(a -> a.accept(INVOCATION_DATA_COLLECTOR, focalClass));
	}

	private static class InvocationDataCollector extends VoidVisitorAdapter<FocalClass>
	{
		/**
		 * If the annotation expression is a DScribe annotation, create a corresponding TemplateInvocation instance and
		 * add it to the corresponding focal method.
		 */
		@Override
		public void visit(NormalAnnotationExpr annExpr, FocalClass focalClass)
		{
			var templateName = annExpr.getNameAsString();
			Map<String, String[]> placeholders = new HashMap<>();

			annExpr.getChildNodes().forEach(n -> n.accept(Utils.PLACEHOLDER_COLLECTOR, placeholders));

			var invocation = new TemplateInvocation(templateName, placeholders, annExpr);

			MethodDeclaration methodDecl = (MethodDeclaration) (annExpr.getParentNode().get());
			invocation.setOldTest(methodDecl);

			var focalMethod = getFocalMethod(focalClass, placeholders.get("$uut$")[0]);
			if (focalMethod != null)
			{
				focalMethod.addTest(invocation);
			}
			// TODO: Add error here
		}

		/**
		 * Retrieve the focal method with the given signature in the given focal class
		 */
		private FocalMethod getFocalMethod(FocalClass focalClass, String uutSignature)
		{
			return focalClass	.getMethods()
								.stream()
								.filter(m -> m.getSignature().equals(uutSignature))
								.findAny()
								.orElse(null);
		}
	}
}
