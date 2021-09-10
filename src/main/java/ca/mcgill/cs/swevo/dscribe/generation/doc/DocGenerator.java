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
package ca.mcgill.cs.swevo.dscribe.generation.doc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import ca.mcgill.cs.swevo.dscribe.generation.Generator;
import ca.mcgill.cs.swevo.dscribe.model.FocalClass;
import ca.mcgill.cs.swevo.dscribe.model.FocalMethod;
import ca.mcgill.cs.swevo.dscribe.model.FocalTestPair;
import ca.mcgill.cs.swevo.dscribe.model.TestClass;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.invocation.TemplateInvocation;

public class DocGenerator extends Generator {
  private Set<String> modifiedSrcClasses;

  public DocGenerator(List<FocalTestPair> focalTestPairs) {
    super(focalTestPairs);
    modifiedSrcClasses = new HashSet<>();
  }

  public Set<String> output() {
    return modifiedSrcClasses;
  }

  @Override
  protected void addInvocations(FocalTestPair focalTestPair) {
    var focalClass = focalTestPair.focalClass();
    for (FocalMethod focalMethod : focalClass) {
      List<InfoFragment> fragments = new ArrayList<>();
      for (TemplateInvocation instance : focalMethod) {
        addDefaultPlaceholders(instance, focalClass, focalMethod);
        List<Template> templates = templateRepo.get(instance.getTemplateName());
        for (Template template : templates) {
          Optional<DocumentationFactory> factory = template.getDocFactory();
          if (factory.isPresent()) {
            fragments.add(factory.get().create(instance));
          }
        }
      }
      if (fragments.size() > 0) {
        Set<InfoFragment> combinedFragments = InfoFragment.combine(fragments);
        Set<String> dscribeFragments =
            combinedFragments.stream().map(x -> x.print()).collect(Collectors.toSet());
        addDscribeFragments(focalClass.getMethodDeclaration(focalMethod), dscribeFragments);
        modifiedSrcClasses.add(focalClass.getName());
      }
    }
  }

  private void addDscribeFragments(MethodDeclaration method, Set<String> dscribeFragments) {
    Javadoc javadoc = method.getJavadoc().orElseGet(() -> new Javadoc(new JavadocDescription()));
    for (Iterator<JavadocBlockTag> iter = javadoc.getBlockTags().iterator(); iter.hasNext();) {
      JavadocBlockTag tag = iter.next();
      if (tag.getTagName().equals("dscribe")) {
        iter.remove();
      }
    }
    for (String fragment : dscribeFragments) {
      javadoc.addBlockTag("dscribe", fragment);
    }
    method.setJavadocComment(javadoc);
    // TODO control indentation
  }

  @Override
  protected void generate(FocalClass focalClass, MethodDeclaration focalMethodDecl,
      TestClass testClass, TemplateInvocation invocation, Template template) {
    // TODO Auto-generated method stub

  }
}
