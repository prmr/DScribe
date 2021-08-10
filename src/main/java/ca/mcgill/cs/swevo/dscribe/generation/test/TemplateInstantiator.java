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
package ca.mcgill.cs.swevo.dscribe.generation.test;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import ca.mcgill.cs.swevo.dscribe.instance.TemplateInvocation;

/**
 * Visits all the nodes of the scaffold focal method. If a $---$ pattern is found: 1. Check if a
 * matching element name is found for the template. 2. Check the type of the template element based
 * on the source code. 3. Cross-check to validate and substitute if types align.
 */
public class TemplateInstantiator extends ModifierVisitor<TemplateInvocation> {
  private static final Pattern PATTERN = Pattern.compile("\\$.*?\\$");

  @Override
  public Visitable visit(EmptyStmt n, TemplateInvocation arg) {
    Optional<Node> parentNode = n.getParentNode();
    if (parentNode.isEmpty()) {
      return null;
    }
    Node parent = parentNode.get();
    if (parent instanceof IfStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
        || parent instanceof WhileStmt || parent instanceof LabeledStmt) {
      return new BlockStmt();
    }
    return null;
  }

  @Override
  public Node visit(SimpleName md, TemplateInvocation template) {
    super.visit(md, template);
    Matcher matcher = PATTERN.matcher(md.getIdentifier());
    if (matcher.matches()) {
      String newName = replaceWholeNode(matcher.group(), template);
      if (newName.isEmpty()) {
        return null;
      }
      md.setIdentifier(newName);
      return md;
    } else {
      matcher.reset();
      String newName = replaceInnerMatches(matcher, template);
      md.setIdentifier(newName);
      return md;
    }
  }

  private static String replaceWholeNode(String identifier, TemplateInvocation template) {
    String name = identifier;
    if (template.containsPlaceholder(name)) {
      var value = template.getPlaceholderValue(name);
      if (value.isList()) {
        return value.getValueAsList().stream().collect(Collectors.joining(", "));
      } else {
        return value.getValue();
      }
    }
    return name;
  }

  private static String replaceInnerMatches(Matcher matcher, TemplateInvocation template) {
    var sb = new StringBuffer();
    while (matcher.find()) {
      String name = matcher.group();
      if (template.containsPlaceholder(name)) {
        var value = template.getPlaceholderValue(matcher.group());
        if (value.isList()) {
          throw new IllegalArgumentException("List placeholder used inside an identifier");
        } else {
          String replacement = value.getValue();
          if (replacement.startsWith("new ")) {
            replacement = replacement.substring(4);
          }
          replacement = Matcher.quoteReplacement(replacement);
          matcher.appendReplacement(sb, replacement);
        }
      } else {
        throw new IllegalArgumentException(
            "Invalid placeholder name '" + name + "' for template " + template.getName());
      }
    }
    // Perform the node string replacement.
    matcher.appendTail(sb);
    return sb.toString();
  }

  public static String resolveName(String name, TemplateInvocation instance) {
    Matcher matcher = PATTERN.matcher(name);
    return replaceInnerMatches(matcher, instance);
  }
}
