package ca.mcgill.cs.swevo.dscribe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * Represents the context of execution of DScribe. It contains user settings such as the template repository location.
 * 
 * @author mnassif
 *
 */
public class Context
{
	private static final Context INSTANCE = new Context();
	
	private Context()
	{
	}

	public static Context defaultContext()
	{
		return INSTANCE;
	}

	public List<Path> srcPaths()
	{
		return List.of(Path.of("src", "main", "java"));
	}

	public Path testsOutputPath()
	{
		return Path.of("output/");
	}

	public Path formatTemplateInstancePath(String className)
	{
		return Path.of("configs", className + ".config.json");
	}

	public List<Path> templateInstancesPaths()
	{
		try (Stream<Path> allPaths = Files.list(Path.of("configs")))
		{
			List<Path> configPaths = allPaths.filter(f -> f.toString().endsWith(".config.json"))
					.collect(Collectors.toList());
			allPaths.close();
			return configPaths;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public String templateRepositoryPath()
	{
		return "templates/";
	}

	public TemplateRepository templateRepository()
	{
		return new InMemoryTemplateRepository(templateRepositoryPath());
	}
}
