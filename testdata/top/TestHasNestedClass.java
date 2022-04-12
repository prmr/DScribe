package top;

import static org.junit.jupiter.api.Assertions.*;
import ca.mcgill.cs.swevo.dscribe.annotations.DScribeAnnotations.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

public class TestHasNestedClass {

    @Test
    @AssertBools(factory = "HasNestedClass", falseParams = { "22" }, falseState = "isEven", trueParams = { "23" }, trueState = "isOdd", uut = "isOdd(int)")
    public void isOdd_WhenisOddReturnTrueWhenisEvenReturnFalse() {
        boolean actual = HasNestedClass.isOdd(23);
        boolean fOracle = HasNestedClass.isOdd(22);
        assertTrue(actual);
        assertFalse(fOracle);
    }

    @Test
    @AssertBools(factory = "HasNestedClass.Inner", falseParams = { "23" }, falseState = "Odd", trueParams = { "22" }, trueState = "isEven", uut = "isEven(int)")
    public void isEven_WhenisEvenReturnTrueWhenOddReturnFalse() {
        boolean actual = HasNestedClass.Inner.isEven(22);
        boolean fOracle = HasNestedClass.Inner.isEven(23);
        assertTrue(actual);
        assertFalse(fOracle);
    }
}
