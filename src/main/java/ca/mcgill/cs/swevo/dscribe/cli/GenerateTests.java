/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Parameters;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.generation.test.TestGenerator;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstances;
import ca.mcgill.cs.swevo.dscribe.instance.TestClass;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationWarning;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationWarning.Type;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException.GenerationError;

@Command(name = "generateTests", mixinStandardHelpOptions = true)
public class GenerateTests implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;
	
	@Parameters(index="0")
	String testClassName; 

	// possibly move logger to Warning class
	private static final Logger LOGGER = Logger.getLogger(GenerateTests.class.getName());
	
	@Override
	public Integer call() throws URISyntaxException, ReflectiveOperationException
	{
		TestClass testClass = initTestClass(testClassName);
		Generator generator = new TestGenerator(testClass);
		generator.prepare(codit.getContext());		
		generator.loadInvocations();
		List<Exception> errors = generator.generate();
		System.out.println("Finished generating unit tests with " + errors.size() + " error(s).");
		errors.forEach(e -> System.out.println("Test generation error: " + e.getClass() + ": " + e.getMessage()));
		return 0;
	}
	
	// Later will return list of TestClass
	private TestClass initTestClass(String testClassName)
	{
		TestClass tc = null;
		Class<?> testClass = resolveClassName(testClassName, GenerationWarning.Type.UNRESOLVED_TEST_CLASS);;
		// normally just ignore
		if (testClass != null)
		{
			String srcClassName = srcClassName(testClassName);
			Class<?> srcClass = resolveClassName(srcClassName, GenerationWarning.Type.UNRESOLVED_SRC_CLASS);
			if (srcClass != null)
			{
				return new TestClass(testClass, srcClass);
			}
		}
		return null;
	}

	private Class<?> resolveClassName(String testClassName, GenerationWarning.Type type)
	{
		Class<?> resolved = null;
		try
		{
			resolved = Class.forName(testClassName, false, getClass().getClassLoader());
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.log(Level.WARNING, GenerationWarning.format(type, testClassName));
		}
		return resolved;
	}
	
	
	// deal with errors 
	private String srcClassName(String testClassName)
	{
		int idx = testClassName.lastIndexOf(".") + 1 ;
		String simpleTestClsName = testClassName.substring(idx);
		if (!simpleTestClsName.startsWith("Test"))
		{
			System.err.println("Ignoring " + testClassName + "as it does not follow the naming conventions.");
			return null;
		}
		return testClassName.substring(0, idx) + testClassName.substring(idx+4); 
		
	}
}
