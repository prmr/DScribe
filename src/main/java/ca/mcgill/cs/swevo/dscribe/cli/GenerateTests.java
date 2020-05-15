package ca.mcgill.cs.swevo.dscribe.cli;

import java.util.List;
import java.util.concurrent.Callable;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.generation.test.TestGenerator;

@Command(name = "generateTests", mixinStandardHelpOptions = true)
public class GenerateTests implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Override
	public Integer call()
	{
		Generator generator = new TestGenerator();
		generator.prepare(codit.getContext());
		generator.loadInvocations();
		List<Exception> errors = generator.generate();
		System.out.println("Finished generating unit tests with " + errors.size() + " error(s).");
		generator.output().forEach(System.out::println);
		errors.forEach(e -> System.out.println("Test generation error: " + e.getClass() + ": " + e.getMessage()));
		return 0;
	}
}
