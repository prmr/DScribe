/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.cli;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Parameters;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.doc.DocGenerator;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.utils.UserMessages;

/**
 * The GenerateDocs class generates documentation fragments for each template invocation in the given focal classes and
 * their corresponding test classes. The generated documentation fragments are inserted in the JavaDoc comment of the
 * corresponding focal method.
 * 
 * @author Alexa
 *
 */
@Command(name = "generateDocs", mixinStandardHelpOptions = true)
public class GenerateDocs implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Parameters
	List<String> focalClassNames;

	@Override
	public Integer call() throws IOException, ReflectiveOperationException, URISyntaxException
	{
		if (focalClassNames == null || focalClassNames.isEmpty())
		{
			UserMessages.DocGeneration.isMissingFocalClassNames();
			return 0;
		}
		// Configure context
		var context = codit.getContext();
		Utils.configureContext(context, focalClassNames.get(0));

		// // Create a FocalTestPair instance for each focal class name
		List<FocalTestPair> focalTestPairs = Utils.initFocalClasses(focalClassNames, context);

		// Generate documentation for each template invocation in the focal and test classes
		var generator = new DocGenerator(focalTestPairs, context.templateRepository());
		generator.prepare();
		List<String> modifiedClasses = generator.generate();
		List<Exception> errors = generator.save();

		// Inform user that generation is complete and list any errors
		UserMessages.DocGeneration.isComplete(errors.size(), modifiedClasses);
		errors.forEach(e -> UserMessages.DocGeneration.errorOccured(e.getClass().getName(), e.getMessage()));
		return errors.size();
	}
}
