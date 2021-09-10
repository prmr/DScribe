package ca.mcgill.cs.swevo.dscribe.model;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.Context.TestClassNameConvention;
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
		Class<?> focalClass = resolveClassName(focalClassName);
		if (focalClass == null)
		{
			UserMessages.ParsingWarning.unresolvedFocal(focalClassName);
			return null;
		}
		Path path = getPathToClass(focalClass, context.sourceFolder());
		// TODO: check if path exists
		return new FocalClass(path, focalClass);
	}

	private TestClass buildTestClass(String testClassName)
	{
		Class<?> testClass = resolveClassName(testClassName);
		if (testClass == null)
		{
			// TODO: create empty test class file
		}
		Path path = getPathToClass(testClass, context.testFolder());
		// TODO: check if path exists
		return new TestClass(path);
	}

	/**
	 * Return the path to the Java class file (.java) on the local machine
	 * 
	 * @param clazz
	 *            the Class instance for which to retrieve a path
	 * @param targetFolder
	 *            the folder in which to look for the Java class file
	 */
	private Path getPathToClass(Class<?> clazz, String targetFolder)
	{
		String resourceName = resourceName(clazz);
		URL binUrl = context.classLoader().getResource(resourceName);
		Path binPath = Paths.get(URI.create(binUrl.toString()));
		Path classPath = getClassPathFromBinPath(binPath, targetFolder);
		return classPath;
	}

	/**
	 * Get resource name of the corresponding class
	 */
	private String resourceName(Class<?> clazz)
	{
		return clazz.getName().replaceAll(Pattern.quote("."), "/") + ".class";
	}

	/**
	 * Get the path to the Java class file (.java) from the path to the binary class file (.class)
	 * 
	 * @param binPath
	 *            the path to the binary class file (.class)
	 * @param targetFolder
	 *            the folder to look for the Java class file
	 */
	private Path getClassPathFromBinPath(Path binPath, String targetFolder)
	{
		String binFolder = Pattern.quote(File.separator + context.binFolder() + File.separator);
		String srcFolder = Matcher.quoteReplacement(File.separator + targetFolder + File.separator);
		String binExt = Pattern.quote(".class");
		String srcExt = Matcher.quoteReplacement(".java");
		String binLocation = binPath.toString();
		String srcLocation = binLocation.replaceFirst(binFolder, srcFolder).replaceFirst(binExt, srcExt);
		return Paths.get(srcLocation);
	}

	private Class<?> resolveClassName(String className)
	{
		Class<?> resolved;
		try
		{
			resolved = Class.forName(className, false, context.classLoader());
		}
		catch (ClassNotFoundException e)
		{
			resolved = null;
		}
		return resolved;
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
