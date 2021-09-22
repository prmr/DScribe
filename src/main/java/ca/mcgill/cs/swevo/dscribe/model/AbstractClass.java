package ca.mcgill.cs.swevo.dscribe.model;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.GenerationException.GenerationError;

public abstract class AbstractClass implements Parseable
{
	private CompilationUnit cu;
	private final Path path;

	protected AbstractClass(Path path)
	{
		assert path != null;
		this.path = path;
	}

	public CompilationUnit compilationUnit()
	{
		return cu;
	}

	public String getName()
	{
		assert cu != null;
		return getClassDeclaration().getFullyQualifiedName().get();
	}

	/**
	 * Parse this class into a compilation unit and store the result
	 * 
	 * @param parser
	 *            the JavaParser instance to use for parsing
	 * @throws GenerationException
	 *             if there is a parsing error or the path cannot be accessed
	 */
	@Override
	public void parseCompilationUnit(JavaParser parser)
	{
		try
		{
			ParseResult<CompilationUnit> parseResult = parser.parse(path);
			if (!parseResult.isSuccessful())
			{
				throw new GenerationException(GenerationError.INVALID_SOURCE_FILE, getCause(parseResult));
			}
			Optional<CompilationUnit> result = parseResult.getResult();
			if (result.isPresent())
			{
				cu = result.get();
			}
		}
		catch (IOException e)
		{
			throw new GenerationException(GenerationError.IO_ERROR, e);
		}
	}

	@Override
	public boolean writeToFile(List<Exception> exceptions)
	{
		try (BufferedWriter fileWriter = Files.newBufferedWriter(path, UTF_8))
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

	private Throwable getCause(ParseResult<CompilationUnit> parseResult)
	{
		Problem firstProblem = parseResult.getProblem(0);
		Optional<Throwable> cause = firstProblem.getCause();
		return cause.orElse(null);
	}

	protected ClassOrInterfaceDeclaration getClassDeclaration()
	{
		assert cu != null;
		return (ClassOrInterfaceDeclaration) cu.getType(0);
	}
}
