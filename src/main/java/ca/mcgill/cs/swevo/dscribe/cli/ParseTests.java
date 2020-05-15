package ca.mcgill.cs.swevo.dscribe.cli;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Parameters;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstances;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.TestParsingException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.TestParsingException.ParsingError;

@Command(name = "parseTests", mixinStandardHelpOptions = true)
public class ParseTests implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Parameters
	private List<String> paths;

	@Override
	public Integer call()
	{
		TemplateRepository repository = codit.getContext().templateRepository();
		for (String file : paths)
		{
			try
			{
				Path path = Path.of(file);
				CompilationUnit source = StaticJavaParser.parse(path);
				TemplateInstances emptyInstances = TemplateInstances.fromUnitTests(source, repository);
				String json = emptyInstances.toJson();
				String configName = path.toFile().getName().replaceAll("\\.java$", "") + ".parsed";
				Path outPath = codit.getContext().formatTemplateInstancePath(configName);
				try (BufferedWriter fileWriter = Files.newBufferedWriter(outPath, UTF_8))
				{
					fileWriter.write(json);
					System.out.println(outPath.toString() + " has been generated.");
				}
			}
			catch (IOException e)
			{
				throw new TestParsingException(ParsingError.IO_ERROR, e);
			}
		}
		return 0;
	}
}
