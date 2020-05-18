/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.cli;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Parameters;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstances;

@Command(name = "createConfig", mixinStandardHelpOptions = true)
public class CreateConfig implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Parameters
	private List<String> className;

	@Override
	public Integer call()
	{
		List<Class<?>> sources = resolveClassNames(className);
		writeConfigFiles(sources);
		return 0;
	}

	private List<Class<?>> resolveClassNames(List<String> names)
	{
		List<Class<?>> classes = new ArrayList<>();
		for (String name : names)
		{
			try
			{
				Class<?> resolved = Class.forName(name, false, getClass().getClassLoader());
				classes.add(resolved);
			}
			catch (ClassNotFoundException e)
			{
				System.err.println("Ignoring unresolved class " + name);
			}
		}
		return classes;
	}

	/**
	 * Output a config.json file for the user based on the source code. Steps: 1. Build a Config object from the Source
	 * Code. 2. Serialize to JSON. 3. Write to config.json.
	 *
	 * @param sourceCodeCu
	 * @throws IOException
	 */
	private void writeConfigFiles(List<Class<?>> sources)
	{
		for (Class<?> source : sources)
		{
			TemplateInstances emptyInstances = TemplateInstances.fromFocalClasses(Arrays.asList(source));
			String json = emptyInstances.toJson();
			Path path = codit.getContext().formatTemplateInstancePath(source.getSimpleName());
			try (BufferedWriter fileWriter = Files.newBufferedWriter(path, UTF_8))
			{
				fileWriter.write(json);
				System.out.println(path.toString() + " has been generated. Please fill it in and rerun the tool.");
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
	}
}
