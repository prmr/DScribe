package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.List;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

public class TestClass extends AbstractDScribeClass {
  // check if path to test exists and is accessible

  public TestClass(Class<?> testClass) {
    super(testClass);
  }


  @Override
  protected String targetFolderName() {
    return "test";
  }
}