package top;

import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.AssertBool;
import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.AssertBools;
import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.AssertThrows;
import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.ToString;

public class HasNestedClass {


    @AssertBools(factory = "HasNestedClass", falseParams = { "22" }, falseState = "isEven", trueParams = { "23" }, trueState = "isOdd")
    public static boolean isOdd(int n) {
        return n % 2 != 0;
    }

    static class Inner {

        @AssertBools(factory = "HasNestedClass.Inner", falseParams = { "23" }, falseState = "Odd", trueParams = { "22" }, trueState = "isEven")
        public static boolean isEven(int n) {
            return n % 2 == 0;
        }
    }
    
    @AssertThrows(exType = java.lang.Exception.class, factory = "HasNestedClass", state = "ParamIsNull", params = {"null"})
    public static void throwsWhen(String str) throws Exception 
    {
    	if (str == null) 
    	{
    		throw new Exception();
    	}
    }
    
    @ToString(factory = "HasNestedClass", target = "")
    public static String stringize() 
    {
    	return "";
    }
    
    @AssertBool(bool = "True", factory = "new HasNestedClass()", state = "n2Dividesn1")
    public boolean divides(int n1, int n2) {
    	return n1 % n2 == 0;
    }
}
