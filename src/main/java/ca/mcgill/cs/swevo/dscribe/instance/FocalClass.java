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
package ca.mcgill.cs.swevo.dscribe.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.gson.annotations.Expose;

/**
 * A FocalClass holds reference to different units under test (for now, units under test correspond
 * to methods in the focal class)
 */

public class FocalClass extends AbstractDScribeClass implements Iterable<FocalMethod> {
  @Expose
  private List<FocalMethod> methods = new ArrayList<>();

  public FocalClass(Class<?> focalClass) {
    super(focalClass);
  }

  public String getName() {
    return cls.getCanonicalName();
  }

  public void addFocalMethod(FocalMethod focalMethod) {
    methods.add(focalMethod);
  }

  public String getSimpleName() {
    return cls.getSimpleName();
  }

  public String getPackageName() {
    return cls.getPackageName();
  }

  public List<FocalMethod> getMethods() {
    return new ArrayList<>(methods);
  }

  public MethodDeclaration getMethodDeclaration(FocalMethod focalMethod) {
    TypeDeclaration<?> classDecl = cu.getType(0);
    List<MethodDeclaration> methodDecl = classDecl.getMethodsBySignature(focalMethod.getName(),
        focalMethod.getParameters().toArray(String[]::new));
    return methodDecl.get(0);
  }

  @Override
  public Iterator<FocalMethod> iterator() {
    return methods.iterator();
  }

  // TODO: get from context
  @Override
  protected String targetFolderName() {
    return "src";
  }
}
