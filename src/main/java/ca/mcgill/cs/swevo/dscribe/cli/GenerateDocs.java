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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Parameters;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.doc.DocGenerator;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.utils.UserMessages;

@Command(name = "generateDocs", mixinStandardHelpOptions = true)
public class GenerateDocs implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Parameters
	String packageName;

	@Override
	public Integer call() throws IOException, ReflectiveOperationException, URISyntaxException
	{
		if (packageName == null || packageName.isEmpty())
		{
			UserMessages.DocGeneration.isMissingPackageName();
			return 0;
		}

		var context = codit.getContext();
		List<String> focalClassNames = collectFocalClassNames(packageName, context);
		List<FocalTestPair> focalTestPairs = Utils.initFocalClasses(focalClassNames, context);

		var generator = new DocGenerator(focalTestPairs, context.templateRepository());
		generator.prepare();
		List<String> modifiedClasses = generator.generate();
		List<Exception> errors = generator.save();

		// Inform user that generation is complete and list any errors
		UserMessages.DocGeneration.isComplete(errors.size(), modifiedClasses);
		errors.forEach(e -> UserMessages.DocGeneration.errorOccured(e.getClass().getName(), e.getMessage()));
		return errors.size();
	}

	public List<String> collectFocalClassNames(String packageName, Context context)
	{
		Path packagePath = pathToPackage(packageName, context);
		List<String> focalClassNames;
		try
		{
			focalClassNames = Files	.walk(packagePath)
									.filter(p -> p.toString().endsWith(".java"))
									.map(p -> classNameFromPath(p, packageName))
									.collect(Collectors.toList());
		}
		catch (IOException e)
		{
			return Collections.emptyList();
		}
		return focalClassNames;
	}

	private Path pathToPackage(String packageName, Context context)
	{
		String resourcePath = packageName.replace('.', '/');
		URL binUrl = context.classLoader().getResource(resourcePath);
		Path binPath = Paths.get(URI.create(binUrl.toString()));
		return Utils.getClassPathFromBinPath(binPath, context.sourceFolder(), context.binFolder());
	}

	private String classNameFromPath(Path classPath, String packageName)
	{
		String classPathAsString = classPath.toAbsolutePath().toString();
		classPathAsString = classPathAsString.replaceAll(Pattern.quote(File.separator), ".");
		int idx = classPathAsString.indexOf(packageName);
		return classPathAsString.substring(idx, classPathAsString.length() - 5);
	}
}
