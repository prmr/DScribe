package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.HashMap;
import java.util.Map;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class TestTemplateDataInstantiator extends VoidVisitorAdapter<FocalClass> {
  private static final TemplateDataCollector TEMPLATE_DATA_COLLECTOR = new TemplateDataCollector();

  @Override
  public void visit(MethodDeclaration md, FocalClass focalClass) {
    md.getAnnotations().forEach(a -> a.accept(TEMPLATE_DATA_COLLECTOR, focalClass));
  }

  private static class TemplateDataCollector extends VoidVisitorAdapter<FocalClass> {
    @Override
    public void visit(NormalAnnotationExpr annExpr, FocalClass focalClass) {
      if (ExtractTemplateData.isDScribeAnnotation(annExpr)) {
        String templateName = annExpr.getNameAsString();
        Map<String, String[]> placeholders = new HashMap<String, String[]>();

        annExpr.getChildNodes()
            .forEach(n -> n.accept(ExtractTemplateData.PARAM_COLLECTOR, placeholders));
        TemplateInstance instance = new TemplateInstance(templateName, placeholders, annExpr);
        FocalMethod focalMethod = getFocalMethod(focalClass, placeholders.get("$uut$")[0]);
        focalMethod.addTest(instance);
      }
    }

    private FocalMethod getFocalMethod(FocalClass focalClass, String uutSignature) {
      return focalClass.getMethods().stream().filter(m -> m.getSignature().equals(uutSignature))
          .findAny().get();
    }
  }
}
