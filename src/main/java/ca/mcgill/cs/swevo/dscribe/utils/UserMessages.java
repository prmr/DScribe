package ca.mcgill.cs.swevo.dscribe.utils;

import java.util.Set;
import ca.mcgill.cs.swevo.dscribe.instance.PlaceholderValue;
import ca.mcgill.cs.swevo.dscribe.template.PlaceholderType;

public class UserMessages {
  private static final String GENERATION_IS_COMPLETE = "Finished generating %s with %d error(s).";
  private static final String GENERATION_ERROR_OCCURRED = "%s generation error: %s : %s";

  private UserMessages() {}

  public static class TestGeneration {
    private TestGeneration() {}

    public static void isComplete(int numErrors) {
      System.out.println(String.format(GENERATION_IS_COMPLETE, "unit tests", numErrors));
    }

    public static void errorOccurred(String errorClass, String errorMessage) {
      System.out
          .println(String.format(GENERATION_ERROR_OCCURRED, "Test", errorClass, errorMessage));
    }
  }

  public static class DocGeneration {
    private DocGeneration() {}

    public static void isComplete(int numErrors) {
      System.out.println(String.format(GENERATION_IS_COMPLETE, "documentation", numErrors));
    }

    public static void errorOccured(String errorClass, String errorMessage) {
      System.out.println(
          String.format(GENERATION_ERROR_OCCURRED, "Documentation", errorClass, errorMessage));
    }
  }

  public static class TemplateInstance {
    private TemplateInstance() {}

    public static void doesNotExist(String templateName) {
      System.out.println(
          String.format("TEMPLATE DOES NOT EXIST: %s is not a valid template.", templateName));
    }

    public static void isMissingPlaceholder(String placeholderName, String templateName) {
      System.out.println(
          String.format("MISSING PLACEHOLDER: Missing value for placeholder %s for template %s",
              placeholderName, templateName));
    }

    public static void hasPlaceholderTypeError(PlaceholderValue placeholderValue,
        PlaceholderType placeholderType) {
      System.out.println(String.format(
          "PLACEHOLDER TYPE ERROR: Placeholder value %s failed validation for type %s.",
          placeholderValue, placeholderType));
    }

    public static void hasExtraPlaceholders(Set<String> extraPlaceholders, String templateName) {
      System.out
          .println(String.format("EXTRA PLACEHOLDERS: Unused placeholders %s for template %s.",
              extraPlaceholders, templateName));
    }
  }

  public static class TemplateFile {
    public static void isMalformed() {
      System.out.println(
          "TEMPLATE FILE ERROR: Template file cannot contain nested classes or interfaces.");
    }
  }
}