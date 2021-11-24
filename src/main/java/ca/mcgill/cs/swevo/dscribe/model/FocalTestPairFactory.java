package ca.mcgill.cs.swevo.dscribe.model;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.Context.TestClassNameConvention;
import ca.mcgill.cs.swevo.dscribe.cli.Utils;
import ca.mcgill.cs.swevo.dscribe.utils.UserMessages;

/**
 * The FocalTestPairFactory class tries to create a new FocalTestPair instance given a focal class name and the context
 * of the DScribe execution. If the focal class name cannot be resolved, null is returned.
 * 
 * @author Alexa
 */
public class FocalTestPairFactory
{
	private final Context context;

	/**
	 * Instant a FocalTestPairFactory for a specific context
	 * 
	 * @param context
	 *            the context of the DScribe execution
	 */
	public FocalTestPairFactory(Context context)
	{
		assert context != null;
		this.context = context;
	}

	/**
	 * Instantiate a FocalTestPair instance for the given focal class name
	 * 
	 * @param focalClassName
	 *            the name of the focal class for which to instantiate a FocalTestPair instance
	 * @return the instantiated FocalTestPair instance if able to resolve the class name. Null otherwise.
	 */
	public FocalTestPair get(String focalClassName)
	{
		assert focalClassName != null;
		FocalClass focalClass = buildFocalClass(focalClassName);
		if (focalClass == null)
		{
			return null;
		}
		String testClassName = testClassName(focalClassName);
		TestClass testClass = buildTestClass(testClassName);
		if (testClass == null)
		{
			UserMessages.ParsingWarning.unresolvedTest(testClassName);
		}
		return new FocalTestPair(focalClass, testClass);
	}

	/**
	 * Instantiate a FocalClass for the given focal class name
	 * 
	 * @param focalClassName
	 *            the name of the focal class for which to instantiate a FocalClass instance
	 * @return the instantiated FocalClass instance if able to resolve the focal class name. Null otherwise.
	 */
	private FocalClass buildFocalClass(String focalClassName)
	{
		Path path = getPathToClass(focalClassName, context.sourceFolder());
		if (path == null)
		{
			UserMessages.ParsingWarning.unresolvedFocal(focalClassName);
			return null;
		}
		return new FocalClass(path);
	}

	private TestClass buildTestClass(String testClassName)
	{
		Path path = getPathToClass(testClassName, context.testFolder());
		if (path == null)
		{
			// TODO: If generateDocs, dont care about not resolving test classes
			return null;
		}
		return new TestClass(path);
	}

	/**
	 * Return the path to the Java class file (.java) on the local machine
	 * 
	 * @param clazz
	 *            the Class instance for which to retrieve a path
	 * @param targetFolder
	 *            the folder in which to look for the Java class file
	 * @throws ClassNotFoundException
	 */
	private Path getPathToClass(String className, String targetFolder)
	{
		String resourceName = Utils.resourceName(className);
		URL binUrl = context.classLoader().getResource(resourceName);
		if (binUrl != null)
		{
			Path binPath = Paths.get(URI.create(binUrl.toString()));
			Path classPath = Utils.getClassPathFromBinPath(binPath, targetFolder, context.binFolder());
			return classPath;
		}
		return null;
	}

	/**
	 * Determine the name of the test class associated with the given focal class
	 * 
	 * @param focalClassName
	 *            the name of the focal class
	 * @return the name of the associated test class
	 */
	private String testClassName(String focalClassName)
	{
		int lastDot = focalClassName.lastIndexOf(".") + 1;
		var focalClassSimpleName = focalClassName.substring(lastDot);
		String testClassSimpleName;
		if (context.testClassNameConvention() == TestClassNameConvention.PREFIX)
		{
			testClassSimpleName = "Test" + focalClassSimpleName;
		}
		else
		{
			testClassSimpleName = focalClassSimpleName + "Test";
		}
		return focalClassName.substring(0, lastDot) + testClassSimpleName;
	}
}
