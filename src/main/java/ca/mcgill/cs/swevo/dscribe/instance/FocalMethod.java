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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.annotations.Expose;

import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.TypeNameResolver;

/**
 * A FocalMethod is unit of test (usually a method) Holds reference to templates.
 */
public class FocalMethod implements Iterable<TemplateInstance> {
  @Expose
  private final String name;
  @Expose
  private final List<String> parameters;
  @Expose
  public List<TemplateInstance> tests = new ArrayList<>();

  public FocalMethod(String name, List<String> parameters) {
    assert name != null;
    this.name = name;
    if (parameters != null)
      this.parameters = List.copyOf(parameters);
    else
      this.parameters = new ArrayList<String>();
  }

  public void addTest(TemplateInstance test) {
    assert test != null;
    tests.add(test);
  }

  @Override
  public Iterator<TemplateInstance> iterator() {
    return tests.iterator();
  }

  public String getName() {
    return name;
  }

  public List<String> getParameters() {
    return new ArrayList<>(parameters);
  }

  public String getSignature() {
    return name + "(" + String.join(",", parameters) + ")";
  }
}
