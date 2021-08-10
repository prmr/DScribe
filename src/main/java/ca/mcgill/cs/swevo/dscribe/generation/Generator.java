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
package ca.mcgill.cs.swevo.dscribe.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import ca.mcgill.cs.swevo.dscribe.Context;
import ca.mcgill.cs.swevo.dscribe.instance.FocalClass;
import ca.mcgill.cs.swevo.dscribe.instance.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.instance.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.instance.TemplateInvocation;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

public abstract class Generator {
  protected TemplateRepository templateRepo;
  protected final JavaParser parser;
  protected final List<FocalTestPair> focalTestPairs;

  protected Generator(List<FocalTestPair> focalTestPairs) {
    assert focalTestPairs != null;
    this.focalTestPairs = new ArrayList<>(focalTestPairs);
    parser = initParser();
  }

  public void prepare(Context context) {
    templateRepo = context.templateRepository();
    for (Iterator<FocalTestPair> iter = focalTestPairs.iterator(); iter.hasNext();) {
      var focalTestPair = iter.next();
      focalTestPair.parseCompilationUnit(parser);
      extractTemplateInvocations(focalTestPair);
    }
  }

  public final void loadInvocations() {
    focalTestPairs.forEach(this::addInvocations);
  }

  protected void addDefaultPlaceholders(TemplateInvocation instance, FocalClass focalClass,
      FocalMethod focalMethod) {
    instance.addPlaceholder("$package$", focalClass.getPackageName());
    String className = focalClass.getSimpleName();
    instance.addPlaceholder("$class$", className);
    String methodName = focalMethod.getName();

    if (methodName.equals(className)) {
      methodName = "new " + methodName;
    }
    instance.addPlaceholder("$method$", methodName);
    List<String> paramNames = new ArrayList<>();
    var i = 0;
    for (String paramType : focalMethod.getParameters()) {
      instance.addPlaceholder("$paramtype" + i + "$", paramType);
      paramNames.add(paramType);
      i++;
    }
    instance.addPlaceholder("$paramtypes$", paramNames.toArray(String[]::new));
  }

  private static JavaParser initParser() {
    var config = new ParserConfiguration();
    config.setLanguageLevel(LanguageLevel.CURRENT);
    config.setTabSize(4);
    config.setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver(false)));
    return new JavaParser(config);
  }

  public List<Exception> generate() {
    List<Exception> exceptions = new ArrayList<>();
    focalTestPairs.forEach(ftp -> ftp.writeToFile(exceptions));
    return exceptions;
  }

  protected void extractTemplateInvocations(FocalTestPair focalTestPair) {
    focalTestPair.extractTemplateInvocations(false);
  };

  protected abstract void addInvocations(FocalTestPair focalTestPair);
}
