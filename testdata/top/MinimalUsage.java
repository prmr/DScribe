package top;

import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.AssertBools;

public class MinimalUsage {
	
	@AssertBools(factory = "MinimalUsage", falseParams = { "22" }, falseState = "IsEven", trueParams = { "23" }, trueState = "IsOdd")
	public static boolean isOdd(int n) {
		return n % 2 != 0;
	}

}
