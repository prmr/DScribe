package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import ca.mcgill.cs.swevo.dscribe.Context;


public class SrcTemplateDataInstantiator extends VoidVisitorAdapter<FocalClass> {
  private static final TemplateDataCollector TEMPLATE_DATA_COLLECTOR = new TemplateDataCollector();

  /**
   * 
   */
  @Override
  public void visit(MethodDeclaration methodDecl, FocalClass focalClass) {
    List<String> params = new ArrayList<>();
    methodDecl.getParameters().forEach(p -> params.add(p.getTypeAsString()));
    var focalMethod = new FocalMethod(methodDecl.getNameAsString(), params);
    methodDecl.getAnnotations().forEach(a -> a.accept(TEMPLATE_DATA_COLLECTOR, focalMethod));
    focalClass.addFocalMethod(focalMethod);
  }

  private static class TemplateDataCollector extends VoidVisitorAdapter<FocalMethod> {
    /**
     * Extract the template instance from the given annotation invocation and add it to the
     * FocalMethod
     */
    @Override
    public void visit(NormalAnnotationExpr annExpr, FocalMethod focalMethod) {
      if (ExtractTemplateData.isDScribeAnnotation(annExpr)) {
        var templateName = annExpr.getNameAsString();
        Map<String, String[]> placeholders = new HashMap<>();

        annExpr.getChildNodes()
            .forEach(n -> n.accept(ExtractTemplateData.PARAM_COLLECTOR, placeholders));
        var instance = new TemplateInvocation(templateName, placeholders, annExpr);
        boolean isValidInstance = instance.validate(Context.instance().templateRepository());
        if (isValidInstance) {
          focalMethod.addTest(instance);
        }
      }
    }
  }


}
