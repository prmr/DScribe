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

import java.util.Optional;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleProvidesDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.modules.ModuleUsesDirective;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.YieldStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.GenericVisitor;

/**
 * AST visitor with default methods to check equality between two ASTs (excluding comments and
 * whitespace from comparison). It is meant as a base implementation to match ASTs using a more
 * complex logic.
 * 
 * @author Mathieu
 * @implNote The source is adapted from JavaParser's NoCommentEqualsVisitor.
 * @see <a href=
 *      "https://github.com/javaparser/javaparser/blob/master/javaparser-core/src/main/java/com/github/javaparser/ast/visitor/NoCommentEqualsVisitor.java">NoCommentEqualsVisitor</a>
 */
public class BaseEqualityMatcher implements GenericVisitor<Boolean, Node> {

  protected final boolean nodesEquals(NodeList<?> n1, NodeList<?> n2) {
    if (n1 == n2) {
      return true;
    }
    if (n1 == null || n2 == null) {
      return false;
    }
    if (n1.size() != n2.size()) {
      return false;
    }
    for (int i = 0; i < n1.size(); i++) {
      if (!nodeEquals(n1.get(i), n2.get(i))) {
        return false;
      }
    }
    return true;
  }

  protected final boolean nodeEquals(Node n, Node n2) {
    if (n == n2) {
      return true;
    }
    if (n == null || n2 == null) {
      return false;
    }
    return n.accept(this, n2);
  }

  protected final boolean nodeEquals(Optional<? extends Node> n, Optional<? extends Node> n2) {
    return nodeEquals(n.orElse(null), n2.orElse(null));
  }

  protected final boolean nodesEquals(Optional<? extends NodeList<?>> n,
      Optional<? extends NodeList<?>> n2) {
    return nodesEquals(n.orElse(null), n2.orElse(null));
  }

  @Override
  public Boolean visit(CompilationUnit n, Node arg) {
    if (!(arg instanceof CompilationUnit)) {
      return false;
    }
    CompilationUnit n2 = (CompilationUnit) arg;
    if (!nodesEquals(n.getImports(), n2.getImports())) {
      return false;
    }
    if (!nodeEquals(n.getModule(), n2.getModule())) {
      return false;
    }
    if (!nodeEquals(n.getPackageDeclaration(), n2.getPackageDeclaration())) {
      return false;
    }
    if (!nodesEquals(n.getTypes(), n2.getTypes())) {
      return false;

    }
    return true;
  }

  @Override
  public Boolean visit(PackageDeclaration n, Node arg) {
    if (!(arg instanceof PackageDeclaration)) {
      return false;
    }
    PackageDeclaration n2 = (PackageDeclaration) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(TypeParameter n, Node arg) {
    if (!(arg instanceof TypeParameter)) {
      return false;
    }
    TypeParameter n2 = (TypeParameter) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getTypeBound(), n2.getTypeBound())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(LineComment n, Node arg) {
    return arg instanceof Comment;
  }

  @Override
  public Boolean visit(BlockComment n, Node arg) {
    return arg instanceof Comment;
  }

  @Override
  public Boolean visit(ClassOrInterfaceDeclaration n, Node arg) {
    if (!(arg instanceof ClassOrInterfaceDeclaration)) {
      return false;
    }
    ClassOrInterfaceDeclaration n2 = (ClassOrInterfaceDeclaration) arg;
    if (!nodesEquals(n.getExtendedTypes(), n2.getExtendedTypes())) {
      return false;
    }
    if (!nodesEquals(n.getImplementedTypes(), n2.getImplementedTypes())) {
      return false;

    }
    if (n.isInterface() != n2.isInterface()) {
      return false;
    }
    if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters())) {
      return false;

    }
    if (!nodesEquals(n.getMembers(), n2.getMembers())) {
      return false;

    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(EnumDeclaration n, Node arg) {
    if (!(arg instanceof EnumDeclaration)) {
      return false;
    }
    EnumDeclaration n2 = (EnumDeclaration) arg;
    if (!nodesEquals(n.getEntries(), n2.getEntries())) {
      return false;
    }
    if (!nodesEquals(n.getImplementedTypes(), n2.getImplementedTypes())) {
      return false;
    }
    if (!nodesEquals(n.getMembers(), n2.getMembers())) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(EnumConstantDeclaration n, Node arg) {
    if (!(arg instanceof EnumConstantDeclaration)) {
      return false;
    }
    EnumConstantDeclaration n2 = (EnumConstantDeclaration) arg;
    if (!nodesEquals(n.getArguments(), n2.getArguments())) {
      return false;
    }
    if (!nodesEquals(n.getClassBody(), n2.getClassBody())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(AnnotationDeclaration n, Node arg) {
    if (!(arg instanceof AnnotationDeclaration)) {
      return false;
    }
    AnnotationDeclaration n2 = (AnnotationDeclaration) arg;
    if (!nodesEquals(n.getMembers(), n2.getMembers())) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(AnnotationMemberDeclaration n, Node arg) {
    if (!(arg instanceof AnnotationMemberDeclaration)) {
      return false;
    }
    AnnotationMemberDeclaration n2 = (AnnotationMemberDeclaration) arg;
    if (!nodeEquals(n.getDefaultValue(), n2.getDefaultValue())) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(FieldDeclaration n, Node arg) {
    if (!(arg instanceof FieldDeclaration)) {
      return false;
    }
    FieldDeclaration n2 = (FieldDeclaration) arg;
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodesEquals(n.getVariables(), n2.getVariables())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(VariableDeclarator n, Node arg) {
    if (!(arg instanceof VariableDeclarator)) {
      return false;
    }
    VariableDeclarator n2 = (VariableDeclarator) arg;
    if (!nodeEquals(n.getInitializer(), n2.getInitializer())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ConstructorDeclaration n, Node arg) {
    if (!(arg instanceof ConstructorDeclaration)) {
      return false;
    }
    ConstructorDeclaration n2 = (ConstructorDeclaration) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getParameters(), n2.getParameters())) {
      return false;

    }
    if (!nodeEquals(n.getReceiverParameter(), n2.getReceiverParameter())) {
      return false;
    }
    if (!nodesEquals(n.getThrownExceptions(), n2.getThrownExceptions())) {
      return false;
    }
    if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(MethodDeclaration n, Node arg) {
    if (!(arg instanceof MethodDeclaration)) {
      return false;
    }
    MethodDeclaration n2 = (MethodDeclaration) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getParameters(), n2.getParameters())) {
      return false;
    }
    if (!nodeEquals(n.getReceiverParameter(), n2.getReceiverParameter())) {
      return false;
    }
    if (!nodesEquals(n.getThrownExceptions(), n2.getThrownExceptions())) {
      return false;
    }
    if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(Parameter n, Node arg) {
    if (!(arg instanceof Parameter)) {
      return false;
    }
    Parameter n2 = (Parameter) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    if (n.isVarArgs() != n2.isVarArgs()) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    if (!nodesEquals(n.getVarArgsAnnotations(), n2.getVarArgsAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(InitializerDeclaration n, Node arg) {
    if (!(arg instanceof InitializerDeclaration)) {
      return false;
    }
    InitializerDeclaration n2 = (InitializerDeclaration) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (n.isStatic() != n2.isStatic()) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(JavadocComment n, Node arg) {
    return arg instanceof Comment;
  }

  @Override
  public Boolean visit(ClassOrInterfaceType n, Node arg) {
    if (!(arg instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType n2 = (ClassOrInterfaceType) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getScope(), n2.getScope())) {
      return false;
    }
    if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(PrimitiveType n, Node arg) {
    if (!(arg instanceof PrimitiveType)) {
      return false;
    }
    PrimitiveType n2 = (PrimitiveType) arg;
    if (n.getType() != n2.getType()) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ArrayType n, Node arg) {
    if (!(arg instanceof ArrayType)) {
      return false;
    }
    ArrayType n2 = (ArrayType) arg;
    if (!nodeEquals(n.getComponentType(), n2.getComponentType())) {
      return false;
    }
    if (n.getOrigin() != n2.getOrigin()) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ArrayCreationLevel n, Node arg) {
    if (!(arg instanceof ArrayCreationLevel)) {
      return false;
    }
    ArrayCreationLevel n2 = (ArrayCreationLevel) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    if (!nodeEquals(n.getDimension(), n2.getDimension())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(IntersectionType n, Node arg) {
    if (!(arg instanceof IntersectionType)) {
      return false;
    }
    IntersectionType n2 = (IntersectionType) arg;
    if (!nodesEquals(n.getElements(), n2.getElements())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(UnionType n, Node arg) {
    if (!(arg instanceof UnionType)) {
      return false;
    }
    UnionType n2 = (UnionType) arg;
    if (!nodesEquals(n.getElements(), n2.getElements())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(VoidType n, Node arg) {
    if (!(arg instanceof VoidType)) {
      return false;
    }
    VoidType n2 = (VoidType) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(WildcardType n, Node arg) {
    if (!(arg instanceof WildcardType)) {
      return false;
    }
    WildcardType n2 = (WildcardType) arg;
    if (!nodeEquals(n.getExtendedType(), n2.getExtendedType())) {
      return false;
    }
    if (!nodeEquals(n.getSuperType(), n2.getSuperType())) {
      return false;
    }
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(UnknownType n, Node arg) {
    if (!(arg instanceof UnknownType)) {
      return false;
    }
    UnknownType n2 = (UnknownType) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ArrayAccessExpr n, Node arg) {
    if (!(arg instanceof ArrayAccessExpr)) {
      return false;
    }
    ArrayAccessExpr n2 = (ArrayAccessExpr) arg;
    if (!nodeEquals(n.getIndex(), n2.getIndex())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ArrayCreationExpr n, Node arg) {
    if (!(arg instanceof ArrayCreationExpr)) {
      return false;
    }
    ArrayCreationExpr n2 = (ArrayCreationExpr) arg;
    if (!nodeEquals(n.getElementType(), n2.getElementType())) {
      return false;
    }
    if (!nodeEquals(n.getInitializer(), n2.getInitializer())) {
      return false;
    }
    if (!nodesEquals(n.getLevels(), n2.getLevels())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ArrayInitializerExpr n, Node arg) {
    if (!(arg instanceof ArrayInitializerExpr)) {
      return false;
    }
    ArrayInitializerExpr n2 = (ArrayInitializerExpr) arg;
    if (!nodesEquals(n.getValues(), n2.getValues())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(AssignExpr n, Node arg) {
    if (!(arg instanceof AssignExpr)) {
      return false;
    }
    AssignExpr n2 = (AssignExpr) arg;
    if (n.getOperator() != n2.getOperator()) {
      return false;
    }
    if (!nodeEquals(n.getTarget(), n2.getTarget())) {
      return false;
    }
    if (!nodeEquals(n.getValue(), n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(BinaryExpr n, Node arg) {
    if (!(arg instanceof BinaryExpr)) {
      return false;
    }
    BinaryExpr n2 = (BinaryExpr) arg;
    if (!nodeEquals(n.getLeft(), n2.getLeft())) {
      return false;
    }
    if (n.getOperator() != n2.getOperator()) {
      return false;
    }
    if (!nodeEquals(n.getRight(), n2.getRight())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(CastExpr n, Node arg) {
    if (!(arg instanceof CastExpr)) {
      return false;
    }
    CastExpr n2 = (CastExpr) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ClassExpr n, Node arg) {
    if (!(arg instanceof ClassExpr)) {
      return false;
    }
    ClassExpr n2 = (ClassExpr) arg;
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ConditionalExpr n, Node arg) {
    if (!(arg instanceof ConditionalExpr)) {
      return false;
    }
    ConditionalExpr n2 = (ConditionalExpr) arg;
    if (!nodeEquals(n.getCondition(), n2.getCondition())) {
      return false;
    }
    if (!nodeEquals(n.getElseExpr(), n2.getElseExpr())) {
      return false;
    }
    if (!nodeEquals(n.getThenExpr(), n2.getThenExpr())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(EnclosedExpr n, Node arg) {
    if (!(arg instanceof EnclosedExpr)) {
      return false;
    }
    EnclosedExpr n2 = (EnclosedExpr) arg;
    if (!nodeEquals(n.getInner(), n2.getInner())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(FieldAccessExpr n, Node arg) {
    if (!(arg instanceof FieldAccessExpr)) {
      return false;
    }
    FieldAccessExpr n2 = (FieldAccessExpr) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getScope(), n2.getScope())) {
      return false;
    }
    if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(InstanceOfExpr n, Node arg) {
    if (!(arg instanceof InstanceOfExpr)) {
      return false;
    }
    InstanceOfExpr n2 = (InstanceOfExpr) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(StringLiteralExpr n, Node arg) {
    if (!(arg instanceof StringLiteralExpr)) {
      return false;
    }
    StringLiteralExpr n2 = (StringLiteralExpr) arg;
    if (!n.getValue().equals(n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(IntegerLiteralExpr n, Node arg) {
    if (!(arg instanceof IntegerLiteralExpr)) {
      return false;
    }
    IntegerLiteralExpr n2 = (IntegerLiteralExpr) arg;
    if (!n.getValue().equals(n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(LongLiteralExpr n, Node arg) {
    if (!(arg instanceof LongLiteralExpr)) {
      return false;
    }
    LongLiteralExpr n2 = (LongLiteralExpr) arg;
    if (!n.getValue().equals(n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(CharLiteralExpr n, Node arg) {
    if (!(arg instanceof CharLiteralExpr)) {
      return false;
    }
    CharLiteralExpr n2 = (CharLiteralExpr) arg;
    if (!n.getValue().equals(n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(DoubleLiteralExpr n, Node arg) {
    if (!(arg instanceof DoubleLiteralExpr)) {
      return false;
    }
    DoubleLiteralExpr n2 = (DoubleLiteralExpr) arg;
    if (!n.getValue().equals(n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(BooleanLiteralExpr n, Node arg) {
    if (!(arg instanceof BooleanLiteralExpr)) {
      return false;
    }
    BooleanLiteralExpr n2 = (BooleanLiteralExpr) arg;
    if (n.getValue() != n2.getValue()) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(NullLiteralExpr n, Node arg) {
    return arg instanceof NullLiteralExpr;
  }

  @Override
  public Boolean visit(MethodCallExpr n, Node arg) {
    if (!(arg instanceof MethodCallExpr)) {
      return false;
    }
    MethodCallExpr n2 = (MethodCallExpr) arg;
    if (!nodesEquals(n.getArguments(), n2.getArguments())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getScope(), n2.getScope())) {
      return false;
    }
    if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(NameExpr n, Node arg) {
    if (!(arg instanceof NameExpr)) {
      return false;
    }
    NameExpr n2 = (NameExpr) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ObjectCreationExpr n, Node arg) {
    if (!(arg instanceof ObjectCreationExpr)) {
      return false;
    }
    ObjectCreationExpr n2 = (ObjectCreationExpr) arg;
    if (!nodesEquals(n.getAnonymousClassBody(), n2.getAnonymousClassBody())) {
      return false;
    }
    if (!nodesEquals(n.getArguments(), n2.getArguments())) {
      return false;
    }
    if (!nodeEquals(n.getScope(), n2.getScope())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(Name n, Node arg) {
    if (!(arg instanceof Name)) {
      return false;
    }
    Name n2 = (Name) arg;
    if (!n.getIdentifier().equals(n2.getIdentifier())) {
      return false;
    }
    if (!nodeEquals(n.getQualifier(), n2.getQualifier())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SimpleName n, Node arg) {
    if (!(arg instanceof SimpleName)) {
      return false;
    }
    SimpleName n2 = (SimpleName) arg;
    if (!n.getIdentifier().equals(n2.getIdentifier())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ThisExpr n, Node arg) {
    if (!(arg instanceof ThisExpr)) {
      return false;
    }
    ThisExpr n2 = (ThisExpr) arg;
    if (!nodeEquals(n.getTypeName(), n2.getTypeName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SuperExpr n, Node arg) {
    if (!(arg instanceof SuperExpr)) {
      return false;
    }
    SuperExpr n2 = (SuperExpr) arg;
    if (!nodeEquals(n.getTypeName(), n2.getTypeName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(UnaryExpr n, Node arg) {
    if (!(arg instanceof UnaryExpr)) {
      return false;
    }
    UnaryExpr n2 = (UnaryExpr) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    if (n.getOperator() != n2.getOperator()) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(VariableDeclarationExpr n, Node arg) {
    if (!(arg instanceof VariableDeclarationExpr)) {
      return false;
    }
    VariableDeclarationExpr n2 = (VariableDeclarationExpr) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodesEquals(n.getVariables(), n2.getVariables())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(MarkerAnnotationExpr n, Node arg) {
    if (!(arg instanceof MarkerAnnotationExpr)) {
      return false;
    }
    MarkerAnnotationExpr n2 = (MarkerAnnotationExpr) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SingleMemberAnnotationExpr n, Node arg) {
    if (!(arg instanceof SingleMemberAnnotationExpr)) {
      return false;
    }
    SingleMemberAnnotationExpr n2 = (SingleMemberAnnotationExpr) arg;
    if (!nodeEquals(n.getMemberValue(), n2.getMemberValue())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(NormalAnnotationExpr n, Node arg) {
    if (!(arg instanceof NormalAnnotationExpr)) {
      return false;
    }
    NormalAnnotationExpr n2 = (NormalAnnotationExpr) arg;
    if (!nodesEquals(n.getPairs(), n2.getPairs())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(MemberValuePair n, Node arg) {
    if (!(arg instanceof MemberValuePair)) {
      return false;
    }
    MemberValuePair n2 = (MemberValuePair) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getValue(), n2.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ExplicitConstructorInvocationStmt n, Node arg) {
    if (!(arg instanceof ExplicitConstructorInvocationStmt)) {
      return false;
    }
    ExplicitConstructorInvocationStmt n2 = (ExplicitConstructorInvocationStmt) arg;
    if (!nodesEquals(n.getArguments(), n2.getArguments())) {
      return false;
    }
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    if (n.isThis() != n2.isThis()) {
      return false;
    }
    if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(LocalClassDeclarationStmt n, Node arg) {
    if (!(arg instanceof LocalClassDeclarationStmt)) {
      return false;
    }
    LocalClassDeclarationStmt n2 = (LocalClassDeclarationStmt) arg;
    if (!nodeEquals(n.getClassDeclaration(), n2.getClassDeclaration())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(AssertStmt n, Node arg) {
    if (!(arg instanceof AssertStmt)) {
      return false;
    }
    AssertStmt n2 = (AssertStmt) arg;
    if (!nodeEquals(n.getCheck(), n2.getCheck())) {
      return false;
    }
    if (!nodeEquals(n.getMessage(), n2.getMessage())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(BlockStmt n, Node arg) {
    if (!(arg instanceof BlockStmt)) {
      return false;
    }
    BlockStmt n2 = (BlockStmt) arg;
    if (!nodesEquals(n.getStatements(), n2.getStatements())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(LabeledStmt n, Node arg) {
    if (!(arg instanceof LabeledStmt)) {
      return false;
    }
    LabeledStmt n2 = (LabeledStmt) arg;
    if (!nodeEquals(n.getLabel(), n2.getLabel())) {
      return false;
    }
    if (!nodeEquals(n.getStatement(), n2.getStatement())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(EmptyStmt n, Node arg) {
    return arg instanceof EmptyStmt;
  }

  @Override
  public Boolean visit(ExpressionStmt n, Node arg) {
    if (!(arg instanceof ExpressionStmt)) {
      return false;
    }
    ExpressionStmt n2 = (ExpressionStmt) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SwitchStmt n, Node arg) {
    if (!(arg instanceof SwitchStmt)) {
      return false;
    }
    SwitchStmt n2 = (SwitchStmt) arg;
    if (!nodesEquals(n.getEntries(), n2.getEntries())) {
      return false;
    }
    if (!nodeEquals(n.getSelector(), n2.getSelector())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SwitchEntry n, Node arg) {
    if (!(arg instanceof SwitchEntry)) {
      return false;
    }
    SwitchEntry n2 = (SwitchEntry) arg;
    if (!nodesEquals(n.getLabels(), n2.getLabels())) {
      return false;
    }
    if (!nodesEquals(n.getStatements(), n2.getStatements())) {
      return false;
    }
    if (n.getType() != n2.getType()) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(BreakStmt n, Node arg) {
    if (!(arg instanceof BreakStmt)) {
      return false;
    }
    BreakStmt n2 = (BreakStmt) arg;
    if (!nodeEquals(n.getLabel(), n2.getLabel())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ReturnStmt n, Node arg) {
    if (!(arg instanceof ReturnStmt)) {
      return false;
    }
    ReturnStmt n2 = (ReturnStmt) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(IfStmt n, Node arg) {
    if (!(arg instanceof IfStmt)) {
      return false;
    }
    IfStmt n2 = (IfStmt) arg;
    if (!nodeEquals(n.getCondition(), n2.getCondition())) {
      return false;
    }
    if (!nodeEquals(n.getElseStmt(), n2.getElseStmt())) {
      return false;
    }
    if (!nodeEquals(n.getThenStmt(), n2.getThenStmt())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(WhileStmt n, Node arg) {
    if (!(arg instanceof WhileStmt)) {
      return false;
    }
    WhileStmt n2 = (WhileStmt) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getCondition(), n2.getCondition())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ContinueStmt n, Node arg) {
    if (!(arg instanceof ContinueStmt)) {
      return false;
    }
    ContinueStmt n2 = (ContinueStmt) arg;
    if (!nodeEquals(n.getLabel(), n2.getLabel())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(DoStmt n, Node arg) {
    if (!(arg instanceof DoStmt)) {
      return false;
    }
    DoStmt n2 = (DoStmt) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getCondition(), n2.getCondition())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ForEachStmt n, Node arg) {
    if (!(arg instanceof ForEachStmt)) {
      return false;
    }
    ForEachStmt n2 = (ForEachStmt) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getIterable(), n2.getIterable())) {
      return false;
    }
    if (!nodeEquals(n.getVariable(), n2.getVariable())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ForStmt n, Node arg) {
    if (!(arg instanceof ForStmt)) {
      return false;
    }
    ForStmt n2 = (ForStmt) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getCompare(), n2.getCompare())) {
      return false;
    }
    if (!nodesEquals(n.getInitialization(), n2.getInitialization())) {
      return false;
    }
    if (!nodesEquals(n.getUpdate(), n2.getUpdate())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ThrowStmt n, Node arg) {
    if (!(arg instanceof ThrowStmt)) {
      return false;
    }
    ThrowStmt n2 = (ThrowStmt) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SynchronizedStmt n, Node arg) {
    if (!(arg instanceof SynchronizedStmt)) {
      return false;
    }
    SynchronizedStmt n2 = (SynchronizedStmt) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(TryStmt n, Node arg) {
    if (!(arg instanceof TryStmt)) {
      return false;
    }
    TryStmt n2 = (TryStmt) arg;
    if (!nodesEquals(n.getCatchClauses(), n2.getCatchClauses())) {
      return false;
    }
    if (!nodeEquals(n.getFinallyBlock(), n2.getFinallyBlock())) {
      return false;
    }
    if (!nodesEquals(n.getResources(), n2.getResources())) {
      return false;
    }
    if (!nodeEquals(n.getTryBlock(), n2.getTryBlock())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(CatchClause n, Node arg) {
    if (!(arg instanceof CatchClause)) {
      return false;
    }
    CatchClause n2 = (CatchClause) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (!nodeEquals(n.getParameter(), n2.getParameter())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(LambdaExpr n, Node arg) {
    if (!(arg instanceof LambdaExpr)) {
      return false;
    }
    LambdaExpr n2 = (LambdaExpr) arg;
    if (!nodeEquals(n.getBody(), n2.getBody())) {
      return false;
    }
    if (n.isEnclosingParameters() != n2.isEnclosingParameters()) {
      return false;
    }
    if (!nodesEquals(n.getParameters(), n2.getParameters())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(MethodReferenceExpr n, Node arg) {
    if (!(arg instanceof MethodReferenceExpr)) {
      return false;
    }
    MethodReferenceExpr n2 = (MethodReferenceExpr) arg;
    if (!n.getIdentifier().equals(n2.getIdentifier())) {
      return false;
    }
    if (!nodeEquals(n.getScope(), n2.getScope())) {
      return false;
    }
    if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(TypeExpr n, Node arg) {
    if (!(arg instanceof TypeExpr)) {
      return false;
    }
    TypeExpr n2 = (TypeExpr) arg;
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ImportDeclaration n, Node arg) {
    if (!(arg instanceof ImportDeclaration)) {
      return false;
    }
    ImportDeclaration n2 = (ImportDeclaration) arg;
    if (n.isAsterisk() != n2.isAsterisk()) {
      return false;
    }
    if (n.isStatic() != n2.isStatic()) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(NodeList n, Node arg) {
    return false;
  }

  @Override
  public Boolean visit(ModuleDeclaration n, Node arg) {
    if (!(arg instanceof ModuleDeclaration)) {
      return false;
    }
    ModuleDeclaration n2 = (ModuleDeclaration) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    if (!nodesEquals(n.getDirectives(), n2.getDirectives())) {
      return false;
    }
    if (n.isOpen() != n2.isOpen()) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ModuleRequiresDirective n, Node arg) {
    if (!(arg instanceof ModuleRequiresDirective)) {
      return false;
    }
    ModuleRequiresDirective n2 = (ModuleRequiresDirective) arg;
    if (!nodesEquals(n.getModifiers(), n2.getModifiers())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override()
  public Boolean visit(ModuleExportsDirective n, Node arg) {
    if (!(arg instanceof ModuleExportsDirective)) {
      return false;
    }
    ModuleExportsDirective n2 = (ModuleExportsDirective) arg;
    if (!nodesEquals(n.getModuleNames(), n2.getModuleNames())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override()
  public Boolean visit(ModuleProvidesDirective n, Node arg) {
    if (!(arg instanceof ModuleProvidesDirective)) {
      return false;
    }
    ModuleProvidesDirective n2 = (ModuleProvidesDirective) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodesEquals(n.getWith(), n2.getWith())) {
      return false;
    }
    return true;
  }

  @Override()
  public Boolean visit(ModuleUsesDirective n, Node arg) {
    if (!(arg instanceof ModuleUsesDirective)) {
      return false;
    }
    ModuleUsesDirective n2 = (ModuleUsesDirective) arg;
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(ModuleOpensDirective n, Node arg) {
    if (!(arg instanceof ModuleOpensDirective)) {
      return false;
    }
    ModuleOpensDirective n2 = (ModuleOpensDirective) arg;
    if (!nodesEquals(n.getModuleNames(), n2.getModuleNames())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(UnparsableStmt n, Node arg) {
    return false;
  }

  @Override
  public Boolean visit(ReceiverParameter n, Node arg) {
    if (!(arg instanceof ReceiverParameter)) {
      return false;
    }
    ReceiverParameter n2 = (ReceiverParameter) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    if (!nodeEquals(n.getName(), n2.getName())) {
      return false;
    }
    if (!nodeEquals(n.getType(), n2.getType())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(VarType n, Node arg) {
    if (!(arg instanceof VarType)) {
      return false;
    }
    VarType n2 = (VarType) arg;
    if (!nodesEquals(n.getAnnotations(), n2.getAnnotations())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(Modifier n, Node arg) {
    if (!(arg instanceof Modifier)) {
      return false;
    }
    Modifier n2 = (Modifier) arg;
    if (n.getKeyword() != n2.getKeyword()) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(SwitchExpr n, Node arg) {
    if (!(arg instanceof SwitchExpr)) {
      return false;
    }
    SwitchExpr n2 = (SwitchExpr) arg;
    if (!nodesEquals(n.getEntries(), n2.getEntries())) {
      return false;
    }
    if (!nodeEquals(n.getSelector(), n2.getSelector())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(YieldStmt n, Node arg) {
    if (!(arg instanceof YieldStmt)) {
      return false;
    }
    YieldStmt n2 = (YieldStmt) arg;
    if (!nodeEquals(n.getExpression(), n2.getExpression())) {
      return false;
    }
    return true;
  }

  @Override
  public Boolean visit(TextBlockLiteralExpr n, Node arg) {
    if (!(arg instanceof TextBlockLiteralExpr)) {
      return false;
    }
    TextBlockLiteralExpr n2 = (TextBlockLiteralExpr) arg;
    if (!n.getValue().equals(n2.getValue())) {
      return false;
    }
    return true;
  }
}
