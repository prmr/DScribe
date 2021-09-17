package ca.mcgill.cs.swevo.dscribe.utils;

import java.util.List;
import java.util.Set;

import ca.mcgill.cs.swevo.dscribe.template.PlaceholderType;
import ca.mcgill.cs.swevo.dscribe.template.invocation.PlaceholderValue;

public class UserMessages
{
	private static final String GENERATION_IS_COMPLETE = "\nFinished generating %s with %d error(s).";
	private static final String GENERATION_ERROR_OCCURRED = "%s generation error: %s : %s";
	private static final String MISSING_FOCAL_CLASS_NAMES = "The %s command should include a space-seperated list "
			+ "of the fully qualified \nnames of the focal classes for which to generate unit tests. For example,"
			+ " \n\t- \"generateTests java.lang.String java.lang.Integer\"";

	private UserMessages()
	{
	}

	private static void printModifiedClasses(List<String> modifiedClasses)
	{
		modifiedClasses.stream().map(c -> String.format("\t- %s\n", c)).forEach(System.out::println);
	}

	public static class TestGeneration
	{
		private TestGeneration()
		{
		}

		public static void isMissingFocalClassNames()
		{
			System.out.println(String.format(MISSING_FOCAL_CLASS_NAMES, "generateTests"));
		}

		public static void isComplete(int numErrors, List<String> modifiedTestClasses)
		{
			System.out.println(String.format(GENERATION_IS_COMPLETE, "unit tests", numErrors));
			if (!modifiedTestClasses.isEmpty())
			{
				System.out.println("Added new unit tests in:");
				printModifiedClasses(modifiedTestClasses);
			}

		}

		public static void errorOccurred(String errorClass, String errorMessage)
		{
			System.out.println(String.format(GENERATION_ERROR_OCCURRED, "Test", errorClass, errorMessage));
		}
	}

	public static class DocGeneration
	{
		private DocGeneration()
		{
		}

		public static void isMissingFocalClassNames()
		{
			System.out.println(String.format(MISSING_FOCAL_CLASS_NAMES, "generateDocs"));
		}

		public static void isComplete(int numErrors, List<String> modifiedClasses)
		{
			System.out.println(String.format(GENERATION_IS_COMPLETE, "documentation", numErrors));
			if (!modifiedClasses.isEmpty())
			{
				System.out.println("Added documentation in:");
				printModifiedClasses(modifiedClasses);
			}
		}

		public static void errorOccured(String errorClass, String errorMessage)
		{
			System.out.println(String.format(GENERATION_ERROR_OCCURRED, "Documentation", errorClass, errorMessage));
		}
	}

	public static class ParsingWarning
	{
		private ParsingWarning()
		{
		}

		public static void unresolvedProperties(String propertiesLocation)
		{
			System.out.println(String.format(
					"Cannot find DScribe properties file. It should be located at %s. Trying to run with default properties.",
					propertiesLocation));
		}

		public static void unresolvedFocal(String focalName)
		{
			System.out.println(String.format("Cannot resolve the source class %s. It will be ignored.", focalName));
		}

		public static void unresolvedTest(String testName)
		{
			System.out.println(String.format("Cannot resolve the test class %s. It will be ignored.", testName));
		}
	}

	public static class TemplateInstance
	{
		private TemplateInstance()
		{
		}

		public static void doesNotExist(String templateName)
		{
			System.out.println(String.format("TEMPLATE DOES NOT EXIST: %s is not a valid template.", templateName));
		}

		public static void isMissingPlaceholder(String placeholderName, String templateName)
		{
			System.out.println(String.format("MISSING PLACEHOLDER: Missing value for placeholder %s for template %s",
					placeholderName, templateName));
		}

		public static void hasPlaceholderTypeError(PlaceholderValue placeholderValue, PlaceholderType placeholderType)
		{
			System.out.println(
					String.format("PLACEHOLDER TYPE ERROR: Placeholder value %s failed validation for type %s.",
							placeholderValue, placeholderType));
		}

		public static void hasExtraPlaceholders(Set<String> extraPlaceholders, String templateName)
		{
			System.out.println(String.format("EXTRA PLACEHOLDERS: Unused placeholders %s for template %s.",
					extraPlaceholders, templateName));
		}
	}

	public static class TemplateFile
	{
		public static void isMalformed()
		{
			System.out.println("TEMPLATE FILE ERROR: Template file cannot contain nested classes or interfaces.");
		}
	}
}
