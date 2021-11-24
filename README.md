
# DScribe
DScribe is a technique to generate both unit tests and documentation from a set of templates and invocations.

## License and Attribution

The content of this repository is licensed unter the terms of the [Apache license, Version 2.0](LICENSE). To indicate attribution, please link to this repository and cite the following technical report:

Mathieu Nassif, Alexa Hernandez, Ashvitha Sridharan, and Martin P. Robillard. "Generating Unit Tests for Documentation," To appear in IEEE Transactions on Software Engineering, doi: [10.1109/TSE.2021.3087087](10.1109/TSE.2021.3087087).

## How DScribe Works
DScribe allows developers to create templates that jointly capture the structure to test and document a recurring concern. Developers can then invoke the templates to generate consistent and checkable unit tests and documentation.

### Writing Templates 
DScribe templates are defined in Java. They associate two components: a code skeleton and a documentation fragment. The code skeleton is created as an abstract syntax tree with a set of nodes marked as placeholders (denoted by surrounding "$" signs). It captures the structure to test the concern. The documentation fragmentation describes the concern under test using free-form text and may refer to the code skeleton's placeholders. For example:
```
/**										 
 * Throws $exType$ when $state$			  	
 */									
@Template("AssertThrows")
@Types($state$=EXPR, $exType$=EXCEPTION, $factory$=EXPR, $params$=EXPR_LIST)
@Test
public void $method$_When$state$_Throw$exType$()
{
	assertThrows($exType$, () -> $class$.$method$($params$)); 
}
```
Since DScribe uses method-level Java annotations to invoke templates (see [invoking templates](#invoking-templates)), for each template, you also need to define an annotation. The annotation must have the same name as the template as well as one parameter for each template placeholder. For example, the annotation for the `AssertThrows` template should look like: 
```
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
```

### Predefined Templates
To help developers get started using DScribe, we provide a set of [six predefined templates](https://github.com/prmr/DScribe/blob/master/dscribe/templates/Template.java) that capture well-known universal specification such as specfications about exceptions, clones contracts, and equals contracts. We also provide the corresponding [annotation definitions](https://github.com/prmr/DScribe/blob/annotations/dscribe/DScribeAnnotations.java) for each of the predefined templates.
	
### Invoking Templates
To invoke a template, you annotate the focal method with the annotation corresponding to the template. For example, to use the `AssertThrows` template to test and document the method `int divide(int x, int y)`, simply annotate the method with the `AssertThrows` annotation and pass the template's placeholder values as input to the annotation. 
```
@AssertThrows(state="DivisorIsZero", exType=java.lang.IllegalArugmentException, params = {"5", "0"})
public int divide(int x, int y) {
	if (y == 0) throw new IllegalArgumentException();
	return x/y;
}
```
You do not need to pass values for the `$method$` and `$class$` placeholders. They are predefined placeholders, their values are derived directly from the focal method and its declaring class.

## Installing DScribe
There are different ways to run DScribe&mdash;as an Eclipse project, as a jar file, or using the more user-friendly DScribe Eclipse plug-in. 

**Eclipse Project:** You can import this repository as an Eclipse project, and run it with a recent Java version (>= 11).

**Compiling sources:** All source files are in the `src/main/java` folder, and dependencies are distributed in the `libs` folder.

**Eclipse Plug-in:** To learn how to install and use the DScribe Eclipse plug-in, checkout the [plug-in's README](https://github.com/prmr/DScribe-plugin). 

## Configuring DScribe
To use DScribe, add the following files to your project:
```
├── dscribe 					
|   └── config.properties     <- Defines DScribe configuration properties
|   └── templates             <- The DScribe template repository
|        └── Template.java    <- Java class defining DScribe templates 
|        └── ... 
```
The `config.properties`can define the following properties: 
* `templateRepoPath`: relative path to the DScribe template repository, `dscribe/templates` by default. 
* `binFolder`: the name of the project's binary folder on the classpath, `bin` by default.
* `srcFolder`: the name of the project's source folder on the classpath, 	`src` by default.
* `testFolder`: the name of the project's test folder, `test` by default.
* `testClassNameConvention`: the test class naming convention to use to identify the test class associated with a given production class. The two options are `prefix` (e.g., given a production class `Calculator`look for a test class named `CalculatorTest`) and `post` (e.g., `TestCalculator`).  The default is `post`. 

Look [here](https://github.com/prmr/DScribe/blob/master/dscribe/config.properties) for an example `config.properties` file! 

## Running DScribe
### Using the DScribe Eclipse Plug-in 
The DScribe Eclipse plug-in's [README](https://github.com/prmr/DScribe-plugin) explains how you can use it to run DScribe.

### Using an Eclipse Project or Jar File 
The entry point of DScribe is the `main` method in `ca.mcgill.cs.swevo.dscribe.DScribe`. Use command line arguments to specify which operation you want to perform, among the following:

- `generateTests`: This operation generates unit tests for all template invocations (e.g., method-level Java annotations) in the inputted focal class(es). When a unit test is generated, it is added to the test class associated with the focal method's declaring type. The template invocation is moved from the focal method to the resulting test method.

  To use this operation, provide a space-separated list of the fully qualified names of the classes for which you want to generate unit tests. For example,

  ```
  java -cp libs/*.jar:bin:your/project ca.mcgill.cs.swevo.dscribe.DScribe generateTests com.mypackage.MyClass1 com.mypackage.MyClass2
  ```

- `generateDocs`: This operation generates documentation based on the template invocations in the inputted focal classes as well as in their associated test classes. 

  To use this operation, provide a space-separated list of the fully qualified names of the classes for which you want to generate documentation. For example,

  ```
  java -cp libs/*.jar:bin:your/project ca.mcgill.cs.swevo.dscribe.DScribe generateDocs com.mypackage.MyClass1
  ```

## Dependencies

DScribe depends on the following Java libraries:

1. *Picocli* is a one-file library to create CLI applications easily. We redistribute the source code of this library in our source folder.
2. *JavaParser* is a library to parse Java source files into ASTs. The compiled class files are included in the `libs` folder.

All dependencies are licensed under the Apache License 2.0 (the same as this tool). A copy of the license is included with this software.
