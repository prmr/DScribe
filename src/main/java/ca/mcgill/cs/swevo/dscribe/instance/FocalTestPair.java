package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.List;
import com.github.javaparser.JavaParser;

public class FocalTestPair implements DScribeClass {
  private final FocalClass focalClass;
  private final TestClass testClass;

  public FocalTestPair(FocalClass focalClass, TestClass testClass) {
    assert focalClass != null && testClass != null;
    this.focalClass = focalClass;
    this.testClass = testClass;
  }

  @Override
  public void parseCompilationUnit(JavaParser parser) {
    focalClass.parseCompilationUnit(parser);
    testClass.parseCompilationUnit(parser);
  }

  public void extractTemplateInvocations(boolean docs) {
    var srcInstantiator = new SrcTemplateDataInstantiator();
    var testInstantiator = new TestTemplateDataInstantiator();
    srcInstantiator.visit(focalClass.compilationUnit(), focalClass);
    if (docs)
      testInstantiator.visit(testClass.compilationUnit(), focalClass);
  }

  public FocalClass focalClass() {
    return focalClass;
  }

  public TestClass testClass() {
    return testClass;
  }

  @Override
  public boolean writeToFile(List<Exception> exceptions) {
    boolean success = testClass.writeToFile(exceptions);
    if (success) {
      success = focalClass.writeToFile(exceptions);
      return success;
    }
    return false;
  }
}
