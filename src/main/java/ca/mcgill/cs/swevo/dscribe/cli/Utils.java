package ca.mcgill.cs.swevo.dscribe.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationWarning;

public class Utils 
{	
	// possibly move logger to Warning class
	private static final Logger LOGGER = Logger.getLogger(GenerateTests.class.getName());
	
	public static List<FocalTestPair> initFocalClasses(List<String> focalClassNames)
	{
		List<FocalTestPair> focalTestPairs = new ArrayList<FocalTestPair>();
		for (String focalClassName : focalClassNames)
		{
			Class<?> focalClass = resolveClassName(focalClassName, GenerationWarning.Type.UNRESOLVED_SRC_CLASS);
			if (focalClass != null)
			{
				String testClassName = testClassName(focalClassName);
				Class<?> testClass = resolveClassName(testClassName, GenerationWarning.Type.UNRESOLVED_TEST_CLASS);
				if (testClass == null)
				{
					// initialize empty test class; 
				}
				else {
				FocalTestPair ftPair = new FocalTestPair(new FocalClass(focalClass), new TestClass(testClass)); 
				focalTestPairs.add(ftPair);
				}
			}
		}
		return focalTestPairs;
	}
	
	/**
	 * Returns the Class object associated with the class or interface with the given string name.
	 * @param className  the name of the Class object to retrieve
	 * @param type  the type of warning to log if the class cannot be located
	 * @return  the Class object with the specified name
	 */
	public static Class<?> resolveClassName(String className, GenerationWarning.Type type)
	{
		Class<?> resolved = null;
		try
		{
			resolved = Class.forName(className, false, Utils.class.getClassLoader());
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.log(Level.WARNING, GenerationWarning.format(type, className));
		}
		return resolved;
	}
	
	
	// Hard coded test class naming convention for now, must update afterwards
	public static String testClassName(String focalClassName)
	{
		int idx = focalClassName.lastIndexOf(".") + 1;
		return focalClassName.substring(0, idx) + "Test" + focalClassName.substring(idx);
	}
}
