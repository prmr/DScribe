/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.utils;

import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;

public class TypeNameResolver
{
	public static Class<?> resolve(String typeName) throws ClassNotFoundException
	{
		String clean = typeName;
		clean = clean.replaceAll("\\s", "");
		clean = clean.replaceAll("\\<.*\\>", "");
		// TODO add common packages, e.g., java.lang, java.util, ...?
		return resolveCleanName(clean);
	}

	private static Class<?> resolveCleanName(String name) throws ClassNotFoundException
	{
		if (name.endsWith("[]"))
		{
			String innerName = name.substring(0, name.length() - 2);
			Class<?> innerType = resolveCleanName(innerName);
			return innerType.arrayType();
		}
		else
		{
			return getType(name);
		}
	}

	private static Class<?> getType(String typeName) throws ClassNotFoundException
	{
		Class<?> type;
		switch (typeName)
		{
		case "boolean":
			type = boolean.class;
			break;
		case "char":
			type = char.class;
			break;
		case "byte":
			type = byte.class;
			break;
		case "short":
			type = short.class;
			break;
		case "int":
			type = int.class;
			break;
		case "long":
			type = long.class;
			break;
		case "float":
			type = float.class;
			break;
		case "double":
			type = double.class;
			break;
		case "void":
			type = void.class;
			break;
		default:
			typeName = typeName.replace(".class", "");
			type = Class.forName(typeName, false, TypeNameResolver.class.getClassLoader());
			break;
		}
		return type;
	}

	public static String canonicalName(ResolvedType type)
	{
		if (type.isArray())
		{
			return canonicalArrayName(type.asArrayType());
		}
		if (type.isPrimitive())
		{
			return canonicalPrimitiveName(type.asPrimitive());
		}
		if (type.isReferenceType())
		{
			return canonicalReferenceName(type.asReferenceType());
		}
		if (type.isTypeVariable())
		{
			return canonicalTypeVariableName(type.asTypeVariable());
		}
		throw new IllegalArgumentException("Unexpected resolved type: " + type.getClass() + "\t" + type.describe());
	}

	private static String canonicalArrayName(ResolvedArrayType type)
	{
		return canonicalName(type.getComponentType()) + "[]";
	}

	private static String canonicalPrimitiveName(ResolvedPrimitiveType type)
	{
		return type.describe();
	}

	private static String canonicalReferenceName(ResolvedReferenceType type)
	{
		return type.getQualifiedName();
	}

	private static String canonicalTypeVariableName(ResolvedTypeVariable type)
	{
		ResolvedTypeParameterDeclaration decl = type.asTypeParameter();
		if (decl.hasLowerBound())
		{
			return canonicalName(decl.getLowerBound());
		}
		else
		{
			return "java.lang.Object";
		}
	}
}
