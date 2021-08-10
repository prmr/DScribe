/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.DScribe;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.Command;
import ca.mcgill.cs.swevo.dscribe.cli.CommandLine.ParentCommand;
import ca.mcgill.cs.swevo.dscribe.generation.doc.DocGenerator;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.utils.UserMessages;

@Command(name = "generateDocs", mixinStandardHelpOptions = true)
public class GenerateDocs implements Callable<Integer> {
  @ParentCommand
  private DScribe codit;

  private Context context;

  @Override
  public Integer call() throws IOException, ReflectiveOperationException, URISyntaxException {
    context = codit.getContext();
    List<FocalTestPair> focalTestPairs = collectFocalTestPairs();
    var generator = new DocGenerator(focalTestPairs);
    generator.prepare(context);
    generator.loadInvocations();

    // Inform user that generation is complete and list any errors
    List<Exception> errors = generator.generate();
    UserMessages.DocGeneration.isComplete(errors.size());
    generator.output().forEach(System.out::println);
    errors.forEach(
        e -> UserMessages.DocGeneration.errorOccured(e.getClass().getName(), e.getMessage()));
    return 0;
  }

  public List<FocalTestPair> collectFocalTestPairs() throws IOException {
    List<Path> srcPaths = context.srcPaths();
    List<String> srcClassNames = new ArrayList<>();
    for (Path path : srcPaths) {
      var sourceRoot = new SourceRoot(path);
      sourceRoot.tryToParse();
      for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
        cu.accept(new CollectClasses(), srcClassNames);
      }
    }
    return Utils.initFocalClasses(srcClassNames, context.classLoader(),
        context.testClassNameConvention());
  }

  private class CollectClasses extends VoidVisitorAdapter<List<String>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<String> classes) {
      classes.add(n.getFullyQualifiedName().get());
    }
  }
}
