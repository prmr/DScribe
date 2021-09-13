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

import java.nio.file.Path;

import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * Represents the context of execution of DScribe. It contains user settings such as the template repository location.
 * Implemented as a Singleton.
 * 
 * @author mnassif
 *
 */
public class Context
{
	/* START: Default context variables */
	private String templateRepositoryPath = defaultTemplateRepositoryPath();
	private ClassLoader classLoader = Context.class.getClassLoader();
	private final TestClassNameConvention testClassNameConvention = TestClassNameConvention.PREFIX;
	/* END: Default context variables */

	private static final Context INSTANCE = new Context();

	public enum TestClassNameConvention
	{
		POSTFIX, PREFIX
	};

	private Context()
	{
	}

	protected static Context instance()
	{
		return INSTANCE;
	}

	public String templateRepositoryPath()
	{
		return templateRepositoryPath;
	}

	public void setTemplateRepositoryPath(String path)
	{
		templateRepositoryPath = path;
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
		return "src";
	}

	public String testFolder()
	{
		return "test";
	}

	public String binFolder()
	{
		return Path.of("bin", "jetuml").toString();
	}

	private static String defaultTemplateRepositoryPath()
	{
		return Path.of("templates").toString();
	}
}
