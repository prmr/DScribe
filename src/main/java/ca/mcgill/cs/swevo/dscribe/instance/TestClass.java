package ca.mcgill.cs.swevo.dscribe.instance;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException.GenerationError;

public class TestClass 
{
	// check if path to test exists and is accessible
	private final Class<?> testClass;
	private final FocalClass srcClass;
	private CompilationUnit testCU; 
	
	public TestClass(Class<?> testClass, Class<?> srcClass)
	{
		assert testClass != null && srcClass != null;
		this.testClass = testClass;
		this.srcClass = new FocalClass(srcClass.getCanonicalName());
	}
	
	public FocalClass focalClass()
	{
		return srcClass;
	}
	
	public CompilationUnit compilationUnit() 
	{
		return testCU;
	}
	
	public void produceCompilationUnit(JavaParser parser)
	{
		try
		{
			ParseResult<CompilationUnit> parseResult = parser.parse(path());
			if (!parseResult.isSuccessful())
			{
				throw new GenerationException(GenerationError.INVALID_SOURCE_FILE);
			}
			CompilationUnit compilationUnit = parseResult.getResult().get();
			LexicalPreservingPrinter.setup(compilationUnit);
			testCU = compilationUnit;
		}
		catch (IOException e)
		{
			throw new GenerationException(GenerationError.IO_ERROR, e);
		}
	}
	
	public void extractTemplateDataFromAnnotations()
	{
		TemplateDataInstantiator tdp = new TemplateDataInstantiator();
		tdp.visit(testCU, this);
	}
		
	public Path path() 
	{
		URL binUrl = getClass().getClassLoader().getResource(resourceName());
		Path binPath = Paths.get(URI.create(binUrl.toString()));
		Path testPath = testPathFromBinPath(binPath);
		return testPath;
	}
	
	private String resourceName()
	{
		String testClassName = testClass.getName();
		return testClassName.replaceAll(Pattern.quote("."),"/") + ".class";
	}
	
	private Path testPathFromBinPath(Path binPath)
	{
		// define src, bin, test folders in context class
		String binFolder = Pattern.quote(File.separator + "bin" + File.separator);
		String testFolder = Matcher.quoteReplacement(File.separator + "test" + File.separator);
		String binExt = Pattern.quote(".class");
		String testExt = Matcher.quoteReplacement(".java");
		
		String binLocation = binPath.toString();
		String srcLocation = binLocation.replaceFirst(binFolder, testFolder).replaceFirst(binExt, testExt);
		return Paths.get(srcLocation);
	}

	public List<String> validate(TemplateRepository repository)
	{
		List<String> warnings = new ArrayList<>();
		boolean valid = srcClass.validate(warnings, repository);
		return warnings;
	}
}
