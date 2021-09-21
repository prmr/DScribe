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
package ca.mcgill.cs.swevo.dscribe.template;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;

import ca.mcgill.cs.swevo.dscribe.template.invocation.InstanceContext;
import ca.mcgill.cs.swevo.dscribe.template.invocation.PlaceholderValue;
import ca.mcgill.cs.swevo.dscribe.utils.TypeNameResolver;

public enum PlaceholderType
{
	TYPE(single(PlaceholderType::isType)), //
	EXCEPTION(single(PlaceholderType::isException)), //
	METHOD(single(PlaceholderType::isMethod)), //
	FIELD(single(PlaceholderType::isIdentifier)), //
	EXPR(single(PlaceholderType::isExpr)), //
	EXPR_LIST(list(PlaceholderType::isExpr));

	private final BiPredicate<PlaceholderValue, InstanceContext> valid;

	private PlaceholderType(BiPredicate<PlaceholderValue, InstanceContext> check)
	{
		valid = check;
	}

	private PlaceholderType(Predicate<PlaceholderValue> check)
	{
		valid = (val, ctx) -> check.test(val);
	}

	public boolean typeCheck(PlaceholderValue instance, InstanceContext context)
	{
		return valid.test(instance, context);
	}

	private static Predicate<PlaceholderValue> single(Predicate<String> check)
	{
		return v -> !v.isList() && check.test(v.getValue());
	}

	private static BiPredicate<PlaceholderValue, InstanceContext> single(BiPredicate<String, InstanceContext> check)
	{
		return (v, c) -> !v.isList() && check.test(v.getValue(), c);
	}

	private static Predicate<PlaceholderValue> list(Predicate<String> check)
	{
		return v -> v.getValueAsList().stream().allMatch(check);
	}

	private static boolean isType(String value, InstanceContext context)
	{
		try
		{
			System.out.println(value);
			TypeNameResolver.resolve(value);
			return true;
		}
		catch (ClassNotFoundException e)
		{
			try
			{
				TypeNameResolver.resolve(context.getPackageName() + "." + value);
				return true;
			}
			catch (ClassNotFoundException e2)
			{
				return false;
			}
		}
	}

	private static boolean isException(String value)
	{
		try
		{
			Class<?> ex = TypeNameResolver.resolve(value);
			return Throwable.class.isAssignableFrom(ex);
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
	}

	private static boolean isMethod(String value)
	{
		if (value.equals("new"))
		{
			return true;
		}
		if (value.startsWith("new "))
		{
			return isIdentifier(value.substring(4));
		}
		return isIdentifier(value);
	}

	private static boolean isIdentifier(String value)
	{
		var parser = new JavaParser();
		ParseResult<Name> res = parser.parseName(value);
		return res.isSuccessful();
	}

	private static boolean isExpr(String value)
	{
		var parser = new JavaParser();
		ParseResult<Expression> res = parser.parseExpression(value);
		return res.isSuccessful();
	}
}
