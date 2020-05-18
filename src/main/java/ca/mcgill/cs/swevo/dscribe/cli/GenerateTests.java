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
