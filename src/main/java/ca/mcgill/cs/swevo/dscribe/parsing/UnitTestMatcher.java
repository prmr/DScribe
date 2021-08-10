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
package ca.mcgill.cs.swevo.dscribe.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import ca.mcgill.cs.swevo.dscribe.instance.TemplateInvocation;

public class UnitTestMatcher extends BaseEqualityMatcher {
  private static final PrettyPrinterConfiguration PP_CONFIG = new PrettyPrinterConfiguration();
  private static final Pattern PLACEHOLDER = Pattern.compile("\\$(.+)\\$");

  static {
    PP_CONFIG.setPrintComments(false);
    PP_CONFIG.setPrintJavadoc(false);
  }

  private final String templateName;
  private final String templatePkg;
  private final String templateCls;
  private final MethodDeclaration template;
  private final Map<String, String[]> values = new HashMap<>();
  private final Map<String, String> innameValues = new HashMap<>();

  public UnitTestMatcher(String templateName, String templatePkg, String templateCls,
      MethodDeclaration template) {
    this.templateName = templateName;
    this.templatePkg = templatePkg;
    this.templateCls = templateCls;
    this.template = template;
  }

  public Optional<TemplateInvocation> match(MethodDeclaration unitTest, String pkgName,
      String clsName) {
    values.clear();
    innameValues.clear();
    if (!matchName(templatePkg, pkgName) || !matchName(templateCls, clsName)) {
      return Optional.empty();
    }
    if (!template.accept(this, unitTest)) {
      return Optional.empty();
    }
    if (!reconcileValues()) {
      return Optional.empty();
    }
    if (!(values.containsKey("$package$") && values.containsKey("$class$")
        && values.containsKey("$method$"))) {
      return Optional.empty();
    }
    return Optional.of(new TemplateInvocation(templateName, values));
  }

  private boolean matchName(String target, String actual) {
    Matcher firstPass = PLACEHOLDER.matcher(target);
    StringBuilder regex = new StringBuilder();
    Set<String> groups = new HashSet<>();
    while (firstPass.find()) {
      groups.add(firstPass.group(1));
      firstPass.appendReplacement(regex, "(?<$1>.*)");
    }
    firstPass.appendTail(regex);
    Matcher secondPass = Pattern.compile(regex.toString()).matcher(actual);
    if (!secondPass.matches()) {
      return false;
    }
    for (String group : groups) {
      String value = secondPass.group(group);
      String oldValue = innameValues.put("$" + group + "$", value);
      if (oldValue != null && !oldValue.equals(value)) {
        return false;
      }
    }
    return true;
  }

  private boolean reconcileValues() {
    for (Entry<String, String> inname : innameValues.entrySet()) {
      String key = inname.getKey();
      String value = inname.getValue();
      if (values.containsKey(key)) {
        String[] otherVals = values.get(key);
        if (otherVals.length != 1 || value.equals(otherVals[0].replaceFirst("^new ", ""))) {
          return false;
        }
      } else {
        values.put(key, new String[] {value});
      }
    }
    return true;
  }

  private boolean isPlaceholder(String name) {
    return PLACEHOLDER.matcher(name).matches();
  }

  private boolean setPlaceholderValue(String key, String... value) {
    if (values.containsKey(key)) {
      String[] oldValue = values.get(key);
      return Arrays.equals(oldValue, value);
    } else {
      values.put(key, value);
      return true;
    }
  }

  private boolean setPlaceholderInnameValue(String key, String value) {
    if (innameValues.containsKey(key)) {
      String oldValue = innameValues.get(key);
      return oldValue.equals(value);
    } else {
      innameValues.put(key, value);
      return true;
    }
  }

  private <T extends Node> Boolean compareListsWithHoles(NodeList<T> targetList,
      NodeList<T> actualList, Predicate<T> holeCheck, boolean capturePlaceholders) {
    if (targetList.size() == 0) {
      return actualList.size() == 0;
    }
    Map<String, List<List<Node>>> captured = new HashMap<>();
    List<Node> captureList = null;
    int targetPos = 0;
    T target;
    boolean freepass = false;
    do {
      if (targetList.size() <= targetPos) {
        if (freepass || actualList.isEmpty()) {
          if (captureList != null) {
            captureList.addAll(actualList);
          }
          return addCapturedNodes(captured);
        } else {
          return false;
        }
      }
      target = targetList.get(targetPos);
      targetPos++;
      if (holeCheck.test(target)) {
        freepass = true;
        if (capturePlaceholders) {
          String placeholder = target.toString(PP_CONFIG).trim();
          captureList = new ArrayList<>();
          captured.computeIfAbsent(placeholder, p -> new ArrayList<>()).add(captureList);
        }
      } else {
        break;
      }
    } while (true);
    int nextPos = 0;
    for (T actual : actualList) {
      nextPos++;
      if (nodeEquals(target, actual)) {
        freepass = false;
        do {
          if (targetList.size() <= targetPos) {
            if (freepass || nextPos == actualList.size()) {
              if (captureList != null) {
                captureList.addAll(actualList.subList(nextPos + 1, actualList.size()));
              }
              return addCapturedNodes(captured);
            } else {
              return false;
            }
          }
          target = targetList.get(targetPos);
          targetPos++;
          if (holeCheck.test(target)) {
            freepass = true;
            if (capturePlaceholders) {
              String placeholder = target.toString(PP_CONFIG).trim();
              captureList = new ArrayList<>();
              captured.computeIfAbsent(placeholder, p -> new ArrayList<>()).add(captureList);
            }
          } else {
            break;
          }
        } while (true);
      } else if (freepass) {
        if (captureList != null) {
          captureList.add(actual);
        }
      } else {
        return false;
      }
    }
    while (targetPos < targetList.size()) {
      if (!holeCheck.test(targetList.get(targetPos))) {
        return false;
      }
      targetPos++;
    }
    return addCapturedNodes(captured);
  }

  private boolean addCapturedNodes(Map<String, List<List<Node>>> capturedNodes) {
    for (String key : capturedNodes.keySet()) {
      List<List<Node>> groups = capturedNodes.get(key);
      if (!PLACEHOLDER.matcher(key).matches() || groups.isEmpty()) {
        return false;
      }
      boolean skipFirst;
      String[] capturedValues;
      if (values.containsKey(key)) {
        skipFirst = false;
        capturedValues = values.get(key);
      } else {
        skipFirst = true;
        capturedValues = nodesToStrings(groups.get(0));
        values.put(key, capturedValues);
      }
      for (List<Node> list : groups) {
        if (skipFirst) {
          skipFirst = false;
          continue;
        }
        String[] matchingValues = nodesToStrings(list);
        if (!Arrays.equals(capturedValues, matchingValues)) {
          return false;
        }
      }
    }
    return true;
  }

  private String[] nodesToStrings(List<Node> nodes) {
    String[] capturedValues = new String[nodes.size()];
    int i = 0;
    for (Node node : nodes) {
      capturedValues[i++] = node.toString(PP_CONFIG);
    }
    return capturedValues;
  }

  @Override
  public Boolean visit(EmptyStmt n, Node arg) {
    return arg instanceof Statement;
  }

  @Override
  public Boolean visit(BlockStmt n, Node arg) {
    if (!(arg instanceof BlockStmt)) {
      return false;
    }
    BlockStmt n2 = (BlockStmt) arg;
    NodeList<Statement> target = n.getStatements();
    NodeList<Statement> actual = n2.getStatements();
    return compareListsWithHoles(target, actual, Statement::isEmptyStmt, false);
  }

  @Override
  public Boolean visit(MethodCallExpr target, Node arg) {
    SimpleName name = target.getName();
    Optional<Expression> scope = target.getScope();
    Optional<NodeList<Type>> types = target.getTypeArguments();
    NodeList<Expression> arguments = target.getArguments();
    boolean isPlaceholder = isPlaceholder(name.asString());

    if (!isPlaceholder) {
      if (!(arg instanceof MethodCallExpr)) {
        return false;
      }
      MethodCallExpr actual = (MethodCallExpr) arg;
      return nodeEquals(name, actual.getName()) && nodeEquals(scope, actual.getScope())
          && nodesEquals(types, actual.getTypeArguments())
          && compareListsWithHoles(arguments, actual.getArguments(),
              e -> e.isNameExpr() && isPlaceholder(e.asNameExpr().getNameAsString()), true);
    }

    boolean constructor;
    String actualName;
    Optional<Expression> actualScope;
    Optional<NodeList<Type>> actualTypes;
    NodeList<Expression> actualArguments;
    if (arg instanceof MethodCallExpr) {
      MethodCallExpr actual = (MethodCallExpr) arg;
      constructor = false;
      actualName = actual.getNameAsString();
      actualScope = actual.getScope();
      actualTypes = actual.getTypeArguments();
      actualArguments = actual.getArguments();
    } else if (arg instanceof ObjectCreationExpr) {
      ObjectCreationExpr actual = (ObjectCreationExpr) arg;
      constructor = true;
      actualName = actual.getTypeAsString();
      actualScope = actual.getScope();
      actualTypes = actual.getTypeArguments();
      actualArguments = actual.getArguments();
    } else {
      return false;
    }

    if (types.isPresent()) {
      if (!nodesEquals(types, actualTypes)) {
        return false;
      }
    } else if (actualTypes.isPresent()) {
      actualName =
          "<" + actualTypes.get().stream().map(Type::asString).collect(Collectors.joining(", "))
              + ">" + actualName;
    }
    if (constructor) {
      actualName = "new " + actualName;
    }
    if (scope.isPresent()) {
      if (!nodeEquals(scope, actualScope)) {
        return false;
      }
    } else if (actualScope.isPresent()) {
      actualName = actualScope.get().toString(PP_CONFIG) + "." + actualName;
    }
    if (!setPlaceholderValue(name.asString(), actualName)) {
      return false;
    }
    return compareListsWithHoles(arguments, actualArguments,
        e -> e.isNameExpr() && isPlaceholder(e.asNameExpr().getNameAsString()), true);
  }

  @Override
  public Boolean visit(MethodReferenceExpr target, Node arg) {
    if (!(arg instanceof MethodReferenceExpr)) {
      return false;
    }
    MethodReferenceExpr actual = (MethodReferenceExpr) arg;
    Expression scope = target.getScope();
    String name = target.getIdentifier();
    Optional<NodeList<Type>> types = target.getTypeArguments();
    if (!nodeEquals(scope, actual.getScope())) {
      return false;
    }
    if (!isPlaceholder(name)) {
      return name.equals(actual.getIdentifier()) && nodesEquals(types, actual.getTypeArguments());
    }
    String actualName = actual.getIdentifier();
    Optional<NodeList<Type>> actualTypes = actual.getTypeArguments();
    if (types.isPresent()) {
      if (!nodesEquals(types, actualTypes)) {
        return false;
      }
    } else {
      actualName =
          "<" + actualTypes.get().stream().map(Type::asString).collect(Collectors.joining(", "))
              + ">" + actualName;
    }
    return setPlaceholderValue(name, actualName);
  }

  @Override
  public Boolean visit(TypeExpr target, Node arg) {
    String name = target.getTypeAsString();
    if (!isPlaceholder(name)) {
      return super.visit(target, arg);
    }
    if (!(arg instanceof Expression)) {
      return false;
    }
    return setPlaceholderValue(name, ((Expression) arg).toString(PP_CONFIG));
  }

  @Override
  public Boolean visit(ClassOrInterfaceType target, Node arg) {
    String name = target.getNameAsString();
    if (!isPlaceholder(name)) {
      return super.visit(target, arg);
    }
    Optional<ClassOrInterfaceType> scope = target.getScope();
    if (scope.isEmpty()) {
      if (!(arg instanceof Type)) {
        return false;
      }
      Type actual = (Type) arg;
      return setPlaceholderValue(name, actual.asString());
    }
    if (!(arg instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType actual = (ClassOrInterfaceType) arg;
    if (!nodeEquals(scope, actual.getScope())) {
      return false;
    }
    return setPlaceholderValue(name, actual.getNameAsString());
  }

  @Override
  public Boolean visit(NameExpr target, Node arg) {
    String name = target.getNameAsString();
    if (!isPlaceholder(name)) {
      return super.visit(target, arg);
    }
    if (!(arg instanceof Expression)) {
      return false;
    }
    return setPlaceholderValue(name, ((Expression) arg).toString(PP_CONFIG));
  }

  @Override
  public Boolean visit(SimpleName target, Node arg) {
    if (!(arg instanceof SimpleName)) {
      return false;
    }
    String actual = ((SimpleName) arg).asString();
    Matcher firstPass = PLACEHOLDER.matcher(target.asString());
    StringBuilder regex = new StringBuilder();
    Set<String> groups = new HashSet<>();
    while (firstPass.find()) {
      groups.add(firstPass.group(1));
      firstPass.appendReplacement(regex, "(?<$1>.*)");
    }
    firstPass.appendTail(regex);
    Matcher secondPass = Pattern.compile(regex.toString()).matcher(actual);
    if (!secondPass.matches()) {
      return false;
    }
    for (String group : groups) {
      if (!setPlaceholderInnameValue("$" + group + "$", secondPass.group(group))) {
        return false;
      }
    }
    return true;
  }
}
