/**
 * 
 */
package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

/**
 * @author Alexa
 *
 */
public class GenerationWarning {
	
	public enum Type
	{ 
		UNRESOLVED_TEST_CLASS("Cannot resolve test class %s. It will be ignored."), 
		UNRESOLVED_SRC_CLASS("Cannot resolve the source class of test class %s. The test class will be ignored.");
	
		private final String msg; 
		
		private Type(String msg)
		{
			this.msg = msg;
		}
		
		public String msg()
		{
			return msg;
		}
	}
	
	public static String format(Type type, String className)
	{
		return String.format(type.msg(), className); 
	}
}
