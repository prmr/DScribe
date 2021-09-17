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
package ca.mcgill.cs.swevo.dscribe;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;

import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.ConfigurationException;
import ca.mcgill.cs.swevo.dscribe.utils.exceptions.ConfigurationException.ConfigurationError;

/**
 * Represents the context of execution of DScribe. It contains user settings such as the template repository location
 * and the test class name convention. Default values are used unless specific values are set in the
 * dscribe/properties.config file of the user's project. Implemented as a Singleton.
 * 
 * @author mnassif
 *
 */
public class Context
{
	/* START: Default context variables */
	private String templateRepoPath = Path.of("dscribe", "templates").toString();
	private ClassLoader classLoader = Context.class.getClassLoader();
	private TestClassNameConvention testClassNameConvention = TestClassNameConvention.PREFIX;
	private String srcFolder = "src";
	private String testFolder = "test";
	private String binFolder = "bin";
	/* END: Default context variables */

	private String projectPath;

	private static final Context INSTANCE = new Context();

	public enum TestClassNameConvention
	{
		POSTFIX, PREFIX
	};

	private Context()
	{
	}

	public static Context instance()
	{
		return INSTANCE;
	}

	/**
	 * Configure the context variables with the values specific in the Java properties file. If no value is indicated
	 * for a variable, the default is kept.
	 */
	public void configure(String projectPath, Properties properties)
	{
		this.projectPath = format(projectPath);
		templateRepoPath = format(properties.getProperty("templateRepoPath", templateRepoPath));
		srcFolder = format(properties.getProperty("srcFolder", srcFolder));
		testFolder = format(properties.getProperty("testFolder", testFolder));
		binFolder = format(properties.getProperty("binFolder", binFolder));
		if (properties.containsKey("testClassNameConvention"))
		{
			testClassNameConvention = parseConvention(properties.getProperty("testClassNameConvention"));
		}
	}

	public String templateRepositoryPath()
	{
		return projectPath + templateRepoPath;
	}

	public ClassLoader classLoader()
	{
		return classLoader;
	}

	public void setClassLoader(ClassLoader loader)
	{
		classLoader = loader;
	}

	public TestClassNameConvention testClassNameConvention()
	{
		return testClassNameConvention;
	}

	public TemplateRepository templateRepository()
	{
		return new InMemoryTemplateRepository(templateRepositoryPath());
	}

	public String sourceFolder()
	{
		return srcFolder;
	}

	public String testFolder()
	{
		return testFolder;
	}

	public String binFolder()
	{
		return binFolder;
	}

	/**
	 * Replace all forward and backward slashes with the File.seperator.
	 */
	private String format(String path)
	{
		return path.replaceAll("[/\\\\]", Matcher.quoteReplacement(File.separator));
	}

	/**
	 * Return the TestClassNameConvention value if it exists. Throw a ConfigurationException otherwise.
	 */
	private TestClassNameConvention parseConvention(String conventionName)
	{
		if (conventionName.equalsIgnoreCase("POSTFIX"))
		{
			return TestClassNameConvention.POSTFIX;
		}
		else if (conventionName.equalsIgnoreCase("PREFIX"))
		{
			return TestClassNameConvention.PREFIX;
		}
		else
		{
			throw new ConfigurationException(ConfigurationError.INVALID_TEST_NAME_CONVENTION);
		}
	}
}
