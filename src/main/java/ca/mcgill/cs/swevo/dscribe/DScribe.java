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
package ca.mcgill.cs.swevo.dscribe;

import ca.mcgill.cs.swevo.dscribe.cli.CommandLine;
import ca.mcgill.cs.swevo.dscribe.cli.CreateConfig;
import ca.mcgill.cs.swevo.dscribe.cli.GenerateDocs;
import ca.mcgill.cs.swevo.dscribe.cli.GenerateTests;
import ca.mcgill.cs.swevo.dscribe.cli.ParseTests;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParameterException;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Spec;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Model.CommandSpec;

/**
 * The main entry point of DScribe, which uses Picocli. A CLI-based approach enables easy wrapping for both IntelliJ and
 * Eclipse plugins.
 */
@Command(name = "dscribe",
		mixinStandardHelpOptions = true,
		synopsisSubcommandLabel = "COMMAND",
		subcommands = { CreateConfig.class, GenerateTests.class, GenerateDocs.class, ParseTests.class })
public class DScribe implements Runnable {
	
	private Context context = Context.defaultContext();
	
	@Spec
	private CommandSpec spec;
	
	/**
	 * If a subcommand e.g. codit createConfig is not specified, an error is thrown. The help menu will be displayed for
	 * the user.
	 */
	@Override
	public void run() {
		throw new ParameterException(spec.commandLine(),
				"Please specify a subcommand such as createConfig or generateTests.");
	}
	
	public Context getContext() {
		return context;
	}
	
	public static void main(String args[]) {
		int code = 0;
		try {
			new CommandLine(new DScribe()).execute(args);
		}
		catch (ParameterException e) {
			code = 1;
		}
		System.exit(code);
	}
}
