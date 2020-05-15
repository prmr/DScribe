package ca.mcgill.cs.swevo.dscribe.cli;

import java.util.List;
import java.util.concurrent.Callable;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.generation.doc.DocGenerator;

@Command(name = "generateDocs", mixinStandardHelpOptions = true)
public class GenerateDocs implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Override
	public Integer call()
	{
		Generator generator = new DocGenerator();
		generator.prepare(codit.getContext());
		generator.loadInvocations();
		List<Exception> errors = generator.generate();
		System.out.println("Finished generating documentation with " + errors.size() + " error(s).");
		generator.output().forEach(System.out::println);
		errors.forEach(e -> System.out.println("Doc generation error: " + e.getClass() + ": " + e.getMessage()));
		return 0;
	}
}
