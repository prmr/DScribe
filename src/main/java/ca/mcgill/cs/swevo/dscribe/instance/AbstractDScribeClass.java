package ca.mcgill.cs.swevo.dscribe.instance;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public abstract class AbstractDScribeClass implements DScribeClass
{
	protected Class<?> cls;
	protected CompilationUnit cu;
	
	public AbstractDScribeClass(Class<?> cls)
	{
		assert cls != null;
		this.cls = cls;
	}

	public CompilationUnit compilationUnit() 
	{
		return cu;
	}
	
	@Override
	public void parseCompilationUnit(JavaParser parser)
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
			cu = compilationUnit;
		}
		catch (IOException e)
		{
			throw new GenerationException(GenerationError.IO_ERROR, e);
		}
	}
	
	public Path path() 
	{
		URL binUrl = getClass().getClassLoader().getResource(resourceName());
		Path binPath = Paths.get(URI.create(binUrl.toString()));
		Path classPath = classPathFromBinPath(binPath);
		return classPath;
	}
	
	private String resourceName()
	{
		String className = cls.getName();
		return className.replaceAll(Pattern.quote("."),"/") + ".class";
	}
	
	private final Path classPathFromBinPath(Path binPath)
	{
		// define src, bin, test folders in context class
		String binFolder = Pattern.quote(File.separator + "bin" + File.separator);
		String srcFolder = Matcher.quoteReplacement(File.separator + targetFolderName() + File.separator);
		String binExt = Pattern.quote(".class");
		String srcExt = Matcher.quoteReplacement(".java");
		
		String binLocation = binPath.toString();
		String srcLocation = binLocation.replaceFirst(binFolder, srcFolder).replaceFirst(binExt, srcExt);
		return Paths.get(srcLocation);
	}
	
	@Override
	public boolean writeToFile(List<Exception> exceptions)
	{
		try (BufferedWriter fileWriter = Files.newBufferedWriter(path(), UTF_8))
		{
			fileWriter.write(cu.toString());
		}
		catch (IOException exception)
		{
			exceptions.add(exception);
			return false;
		}
		return true;
	}
		
	abstract protected String targetFolderName();

	abstract public boolean validate(List<String> warnings, TemplateRepository repository);
}
