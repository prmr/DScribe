package ca.mcgill.cs.swevo.dscribe.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ca.mcgill.cs.swevo.dscribe.Context.TestClassNameConvention;
import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationWarning;

/**
 * The Utility class provides functionality to instantiate a list of FocalTestPairs given a list of
 * focal class names. To do so, for each focal class name it: (1) Retrieves the Class object
 * associated with the name (2) Determines the name of the associated test class (using the
 * convention defined in the Context) (3) Retrieves the Class object associated with the test class
 * name
 * 
 * @author Alexa
 *
 */
public class Utils {
  private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

  private Utils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Initialize a FocalTestPair object for each focal class name provided (if able to resolve using
   * provided class loader)
   * 
   * @param focalClassNames the list focal class names to generate tests/documentation for
   * @param classLoader the ClassLoader to use to resolve the names of the focal classes
   * @return a list of FocalTestPairs
   */
  public static List<FocalTestPair> initFocalClasses(List<String> focalClassNames,
      ClassLoader classLoader, TestClassNameConvention testClassNameConvention) {
    assert focalClassNames != null && classLoader != null;
    List<FocalTestPair> focalTestPairs = new ArrayList<>();
    for (String focalClassName : focalClassNames) {
      // (1) Resolve the focal class name
      Class<?> focalClass = resolveClassName(focalClassName, classLoader,
          GenerationWarning.Type.UNRESOLVED_SRC_CLASS);
      if (focalClass != null) {
        // (2) Identify expected name of associated test class
        String testClassName = testClassName(focalClassName, testClassNameConvention);
        // (3) Resolve test class name
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
   * Retrieve the Class object associated with the given class name
   * 
   * @param className the name of the class to resolve
   * @param type the type of warning to log if the class cannot be located
   * @return the Class object with the specified name
   */
  private static Class<?> resolveClassName(String className, ClassLoader classLoader,
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
  private static String testClassName(String focalClassName,
      TestClassNameConvention testClassNameConvention) {
    int idx = focalClassName.lastIndexOf(".") + 1;
    var focalClassSimpleName = focalClassName.substring(idx);
    String testClassSimpleName;
    if (testClassNameConvention == TestClassNameConvention.PREFIX) {
      testClassSimpleName = "Test" + focalClassSimpleName;
    } else {
      testClassSimpleName = focalClassSimpleName + "Test";
    }
    return focalClassName.substring(0, idx) + testClassSimpleName;
  }
}
