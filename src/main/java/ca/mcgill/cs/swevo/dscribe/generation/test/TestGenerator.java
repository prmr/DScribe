package ca.mcgill.cs.swevo.dscribe.generation.test;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;
import ca.mcgill.cs.swevo.dscribe.template.Template;

/**
 * Generates the final compilation units in order to output tests. The Test Generator performs its task without state,
 * i.e. like a function Given the correct inputs it will output the correct outputs. This idea of immutability is to
 * ensure: 1. Centralizes the responsibility of the object 2. Focus on the core function / processing 3. In turn,
 * enables decoupling the functionality, the class can be used whenever/wherever easily
 */
public class TestGenerator extends Generator
{
	private Path output;
	private Map<String, CompilationUnit> generatedTestFiles;

	@Override
	public List<Path> output()
	{
		List<Path> paths = new ArrayList<>();
		for (CompilationUnit unit : generatedTestFiles.values())
		{
			String file = unit.getType(0).getNameAsString() + ".java";
			paths.add(output.resolve(file));
		}
		return paths;
	}

	@Override
	public void prepare(Context context)
	{
		super.prepare(context);
		output = context.testsOutputPath();
		output.toFile().mkdirs();
		generatedTestFiles = new HashMap<>();
	}

	@Override
	protected void addInvocation(TemplateInstance instance, Template template)
	{
		CompilationUnit cu = getOrCreateCompilationUnit(instance, template);
		ClassOrInterfaceDeclaration type = cu.getType(0).asClassOrInterfaceDeclaration();
		Optional<UnitTestFactory> factory = template.getTestFactory();
		if (factory.isPresent())
		{
			MethodDeclaration method = factory.get().create(instance);
			type.addMember(method);
			addImports(cu, template);
		}
	}

	private CompilationUnit getOrCreateCompilationUnit(TemplateInstance instance, Template template)
	{
		String packName = TemplateInstantiator.resolveName(template.getPackageName(), instance);
		String className = TemplateInstantiator.resolveName(template.getClassName(), instance);
		String key = packName + "." + className;
		if (generatedTestFiles.containsKey(key))
		{
			return generatedTestFiles.get(key);
		}
		CompilationUnit cu = new CompilationUnit();
		if (!packName.isEmpty())
		{
			cu.setPackageDeclaration(packName);
		}
		cu.addClass(className);
		generatedTestFiles.put(key, cu);
		return cu;
	}

	private void addImports(CompilationUnit cu, Template template)
	{
		List<ImportDeclaration> newImports = new ArrayList<>(template.getNecessaryImports());
		newImports.removeAll(cu.getImports());
		for (ImportDeclaration newImport : newImports)
		{
			cu.addImport(newImport);
		}
	}

	@Override
	public List<Exception> generate()
	{
		List<Exception> exceptions = new ArrayList<>();
		for (CompilationUnit finalTestCu : generatedTestFiles.values())
		{
			String className = finalTestCu.getType(0).getNameAsString();
			try (BufferedWriter fileWriter = Files.newBufferedWriter(output.resolve(className + ".java"), UTF_8))
			{
				fileWriter.write(finalTestCu.toString());
				System.out.println("Successfully generated tests for " + className + ".java");
			}
			catch (IOException exception)
			{
				exceptions.add(exception);
			}
		}
		return exceptions;
	}
}
