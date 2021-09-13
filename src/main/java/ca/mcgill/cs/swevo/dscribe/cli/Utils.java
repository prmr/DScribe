package ca.mcgill.cs.swevo.dscribe.cli;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPairFactory;

/**
 * The Utils class provides functionality to instantiate a list of FocalTestPairs given a list of focal class names. To
 * do so, for each focal class name it: (1) Retrieves the Class object associated with the name (2) Determines the name
 * of the associated test class (using the convention defined in the Context) (3) Retrieves the Class object associated
 * with the test class name.
 * 
 * @author Alexa
 *
 */
public class Utils
{
	private Utils()
	{
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Initialize a FocalTestPair instance for each focal class name provided (if able to resolve using provided
	 * context)
	 * 
	 * @param focalClassNames
	 *            the list focal class names to generate tests/documentation for
	 * @param context
	 *            the context of the DScribe execution (has target ClassLoader, test and source directory, etc.
	 * @return a list of FocalTestPairs
	 */
	public static List<FocalTestPair> initFocalClasses(List<String> focalClassNames, Context context)
	{
		assert focalClassNames != null && context != null;
		List<FocalTestPair> focalTestPairs = new ArrayList<>();
		var factory = new FocalTestPairFactory(context);

		for (String focalClassName : focalClassNames)
		{
			var focalTestPair = factory.get(focalClassName);
			if (focalTestPair != null)
			{
				focalTestPairs.add(focalTestPair);
			}

		}
		return focalTestPairs;
	}

	/**
	 * Get the path to the Java class file (.java) or folder from the path to the binary class file (.class) or folder
	 * 
	 * @param binPath
	 *            the path to the binary class file (.class) or folder
	 * @param targetFolder
	 *            the folder to look for the Java class file or folder
	 * @param binFolder
	 *            the folder containing the Java binary file or folder
	 */
	public static Path getClassPathFromBinPath(Path binPath, String targetFolder, String binFolder)
	{
		binFolder = Pattern.quote(File.separator + binFolder + File.separator);
		String srcFolder = Matcher.quoteReplacement(File.separator + targetFolder + File.separator);
		String binExt = Pattern.quote(".class");
		String srcExt = Matcher.quoteReplacement(".java");
		String binLocation = binPath.toString();
		String srcLocation = binLocation.replaceFirst(binFolder, srcFolder).replaceFirst(binExt, srcExt);
		return Paths.get(srcLocation);
	}
}
