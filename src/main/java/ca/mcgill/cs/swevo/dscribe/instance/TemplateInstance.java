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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import ca.mcgill.cs.swevo.dscribe.template.Placeholder;
import ca.mcgill.cs.swevo.dscribe.template.PlaceholderType;
import ca.mcgill.cs.swevo.dscribe.template.Template;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;
import ca.mcgill.cs.swevo.dscribe.utils.UserMessages;

/**
 * A Template is a commonly seen form of code. It needs a set of params to create meaning.
 */

// rename TemplateData
public class TemplateInstance {
  private InstanceContext context = null;

  private final String templateName;
  private final Map<String, PlaceholderValue> placeholders = new HashMap<>();
  private final NormalAnnotationExpr annExpr;

  public TemplateInstance(String templateName, Map<String, String[]> values,
      NormalAnnotationExpr annExpr) {
    assert templateName != null && annExpr != null;
    this.templateName = templateName;
    this.annExpr = annExpr.clone();
    for (Entry<String, String[]> value : values.entrySet())
      placeholders.put(value.getKey(), new PlaceholderValue(value.getValue()));
  }

  public TemplateInstance(String templateName, Map<String, String[]> values) {
    this(templateName, values, null);
  }

  public void setContext(InstanceContext context) {
    assert context != null;
    this.context = context;
  }

  public String getName() {
    return templateName;
  }

  public NormalAnnotationExpr getAnnotationExpr() {
    return annExpr;
  }

  public boolean containsPlaceholder(String placeholder) {
    return placeholders.containsKey(placeholder);
  }

  public PlaceholderValue getPlaceholderValue(String placeholder) {
    return placeholders.get(placeholder);
  }

  public void addPlaceholder(String name, String... value) {
    placeholders.put(name, new PlaceholderValue(value));
  }

  public boolean validate(TemplateRepository repository) {
    if (!repository.contains(templateName)) {
      UserMessages.TemplateInstance.doesNotExist(templateName);
      return false;
    }
    List<Template> templates = repository.get(templateName);
    Set<String> extraPlaceholders = new HashSet<>(placeholders.keySet());
    for (Template template : templates) {
      for (Placeholder placeholder : template) {
        String phName = placeholder.getName();
        extraPlaceholders.remove(phName);
        if (!containsPlaceholder(phName)) {
          UserMessages.TemplateInstance.isMissingPlaceholder(phName, templateName);
          return false;
        }
        var value = getPlaceholderValue(phName);
        PlaceholderType type = placeholder.getType();
        if (!type.typeCheck(value, context)) {
          UserMessages.TemplateInstance.hasPlaceholderTypeError(value, type);
          return false;
        }
      }
      if (!extraPlaceholders.isEmpty()) {
        UserMessages.TemplateInstance.hasExtraPlaceholders(extraPlaceholders, templateName);
      }
    }
    return true;
  }
}
