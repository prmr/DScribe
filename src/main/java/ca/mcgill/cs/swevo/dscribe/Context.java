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


import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * Represents the context of execution of DScribe. It contains user settings such as the template repository location.
 * 
 * @author mnassif
 *
 */
public class Context
{
	private static final Context INSTANCE = new Context();
	
	private Context()
	{
	}

	public static Context defaultContext()
	{
		return INSTANCE;
	}

	public List<Path> srcPaths()
	{
		return List.of(Path.of(Paths.get("..").toAbsolutePath().normalize().toString(),"JetUML", "src", "ca", "mcgill", "cs", "jetuml", "geom"), Path.of(Paths.get("..").toAbsolutePath().normalize().toString(),"JetUML", "src", "ca", "mcgill", "cs", "jetuml", "diagram"));
	}
	
	private void listJavaFiles(File dirFile, List<Path> javaFiles)
	{
		File[] files = dirFile.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isFile() && file.getName().endsWith(".java"))
					javaFiles.add(file.toPath());
				else if (file.isDirectory())
					listJavaFiles(file, javaFiles);
			}	
		}
	}

	public String templateRepositoryPath()
	{
		return "templates/";
	}

	public TemplateRepository templateRepository()
	{
		return new InMemoryTemplateRepository(templateRepositoryPath());
	}
}
