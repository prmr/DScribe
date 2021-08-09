package ca.mcgill.cs.swevo.dscribe.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationWarning;

public class Utils {
  // possibly move logger to Warning class
  private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

  private Utils() {
    throw new IllegalStateException("Utility class");
  }


  /**
   * Initialize a FocalTestPair object for each focal class name provided (if able to resolve using
   * provided class loader)
   * 
   * @param focalClassNames the name of the focal classes to generate tests for
   * @param classLoader the ClassLoader to use to resolve the names of the focal class
   * @return a list of FocalTestPairs
   */
  public static List<FocalTestPair> initFocalClasses(List<String> focalClassNames,
      ClassLoader classLoader) {
    assert focalClassNames != null && classLoader != null;
    List<FocalTestPair> focalTestPairs = new ArrayList<>();
    for (String focalClassName : focalClassNames) {
      Class<?> focalClass = resolveClassName(focalClassName, classLoader,
          GenerationWarning.Type.UNRESOLVED_SRC_CLASS);
      if (focalClass != null) // Resolved successfully
      {
        String testClassName = testClassName(focalClassName); // Identify associated test class name
        Class<?> testClass = resolveClassName(testClassName, classLoader,
            GenerationWarning.Type.UNRESOLVED_TEST_CLASS);
        if (testClass == null) {
          // TO DO: initialize empty test class;
        } else {
          FocalTestPair focalTestPair =
              new FocalTestPair(new FocalClass(focalClass), new TestClass(testClass));
          focalTestPairs.add(focalTestPair);
        }
      }
    }
    return focalTestPairs;
  }

  /**
   * Retrieve the Class object associated with the focal class
   * 
   * @param className the name of the focal class
   * @param type the type of warning to log if the class cannot be located
   * @return the Class object with the specified name
   */
  public static Class<?> resolveClassName(String className, ClassLoader classLoader,
      GenerationWarning.Type type) {
    Class<?> resolved = null;
    try {
      resolved = Class.forName(className, false, classLoader);
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.WARNING, GenerationWarning.format(type, className));
    }
    return resolved;
  }


  /**
   * Determine the name of the test class associated with the given focal class
   * 
   * @param focalClassName the name of the focal class
   * @return the name of the associated test class
   */
  public static String testClassName(String focalClassName) {
    int idx = focalClassName.lastIndexOf(".") + 1;
    return focalClassName.substring(0, idx) + "Test" + focalClassName.substring(idx);
  }
}
