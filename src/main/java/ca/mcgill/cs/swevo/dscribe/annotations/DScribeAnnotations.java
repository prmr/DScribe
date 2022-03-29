package ca.mcgill.cs.swevo.dscribe.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class DScribeAnnotations {
	
	// AssertThrows Template
	@Repeatable(AssertThrowsList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrows {
		public String state();

		Class<?> exType();

		public String factory();

		public String[] params() default {};

		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrowsList {
		AssertThrows[] value();
	}
	
	//ReturnNull Template
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface ReturnNull {
		String factory();
		
		String[] params();
		
		String state();
		
		public String uut() default "";
	}

	// AssertThrowsMessage Template
	@Repeatable(AssertThrowsMessageList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrowsMessage {
		public String state();

		public Class<?> exType();

		public String factory();

		public String[] params() default {};

		public String message();

		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrowsMessageList {
		AssertThrowsMessage[] value();
	}

	// ShallowClone Template
	@Repeatable(ShallowCloneList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface ShallowClone {
		public String factory();

		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface ShallowCloneList {
		ShallowClone[] value();
	}

	// AssertBool Template
	@Repeatable(AssertBoolList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertBool {
		public String state();

		public String bool();

		public String factory();

		public String[] params() default {};

		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertBoolList {
		AssertBool[] value();
	}

	// EqualsContract Template
	@Repeatable(EqualsContractList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface EqualsContract {
		public String factory1();

		public String factory2();

		public String factory3();

		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface EqualsContractList {
		EqualsContract[] value();
	}
	
	//ToString Template
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	public @interface ToString {
		public String factory();
		
		public String target();
		
		public String uut() default "";
	}
	
	//AssertBools Template
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	public @interface AssertBools {
		public String factory();
		
		public String trueState();
		
		public String falseState();
		
		public String[] trueParams();
		
		public String[] falseParams();
		
		public String uut() default "";
	}
	
	//NullParam Template
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	public @interface NullParam {
		public String factory();
		
		public String expected();
		
		public String returnClass();

		public String uut() default "";
	}
	
}
