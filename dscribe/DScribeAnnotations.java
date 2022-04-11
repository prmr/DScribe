package ca.mcgill.cs.swevo.dscribe.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.AssertBool;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.AssertBoolList;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.AssertThrows;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.AssertThrowsList;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.AssertThrowsMessage;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.AssertThrowsMessageList;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.EqualsContract;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.EqualsContractList;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.ShallowClone;
import ca.mcgill.cs.jetuml.annotations.DScribeAnnotations.ShallowCloneList;

public class DScribeAnnotations
{
	// AssertThrows Template
	@Repeatable(AssertThrowsList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrows
	{
		public String state();
		public Class<?> exType();
		public String factory();
		public String[] params() default {};
		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrowsList
	{
		AssertThrows[] value();
	}

	// AssertThrowsMessage Template
	@Repeatable(AssertThrowsMessageList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrowsMessage
	{
		public String state();
		public Class<?> exType();
		public String factory();
		public String[] params() default {};
		public String message();
		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertThrowsMessageList
	{
		AssertThrowsMessage[] value();
	}

	// ShallowClone Template
	@Repeatable(ShallowCloneList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface ShallowClone
	{
		public String factory();
		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface ShallowCloneList
	{
		ShallowClone[] value();
	}

	// AssertBool Template
	@Repeatable(AssertBoolList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertBool
	{
		public String state();
		public String bool();
		public String factory();
		public String[] params() default {};
		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface AssertBoolList
	{
		AssertBool[] value();
	}

	// EqualsContract Template
	@Repeatable(EqualsContractList.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface EqualsContract
	{
		public String factory1();
		public String factory2();
		public String factory3();
		public String uut() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface EqualsContractList
	{
		EqualsContract[] value();
	}
}
