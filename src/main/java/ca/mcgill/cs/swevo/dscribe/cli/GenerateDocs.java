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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
//import ca.mcgill.cs.swevo.dscribe.generation.doc.DocGenerator;
import ca.mcgill.cs.swevo.dscribe.generation.doc.DocGenerator;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;

@Command(name = "generateDocs", mixinStandardHelpOptions = true)
public class GenerateDocs implements Callable<Integer>
{
	@ParentCommand
	private DScribe codit;

	@Override
	public Integer call() throws IOException, ReflectiveOperationException, URISyntaxException 
	{
		List<FocalTestPair> focalTestPairs = collectFocalTestPairs(); 
		DocGenerator generator = new DocGenerator(focalTestPairs);
		generator.prepare(codit.getContext());
		generator.loadInvocations();
		List<Exception> errors = generator.generate();
		System.out.println("Finished generating documentation with " + errors.size() + " error(s).");
		generator.output().forEach(System.out::println);
		errors.forEach(e -> System.out.println("Doc generation error: " + e.getClass() + ": " + e.getMessage()));
		return 0;
	}
	
	public List<FocalTestPair> collectFocalTestPairs() throws IOException
	{
		List<Path> srcPaths = codit.getContext().srcPaths();
		List<String> srcClassNames = new ArrayList<>();
		for (Path path : srcPaths)
		{
			SourceRoot sourceRoot = new SourceRoot(path);
			sourceRoot.tryToParse();
			List<CompilationUnit> test = sourceRoot.getCompilationUnits();
			for (CompilationUnit cu : sourceRoot.getCompilationUnits())
			{
				cu.accept(new collectClasses(), srcClassNames);
			}	
		}
		return Utils.initFocalClasses(srcClassNames);
	}
	
	private class collectClasses extends VoidVisitorAdapter<List<String>> 
	{
		@Override
		public void visit(ClassOrInterfaceDeclaration n, List<String> classes)
		{
			classes.add(n.getFullyQualifiedName().get());
		}
	}
}
