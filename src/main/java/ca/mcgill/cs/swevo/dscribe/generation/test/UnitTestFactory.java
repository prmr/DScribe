/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe.generation.test;

import com.github.javaparser.ast.body.MethodDeclaration;

import ca.mcgill.cs.swevo.dscribe.instance.TemplateInstance;

public class UnitTestFactory
{
	private final MethodDeclaration prototype;

	public UnitTestFactory(MethodDeclaration template)
	{
		prototype = template.clone();
	}

	public MethodDeclaration create(TemplateInstance instance)
	{
		MethodDeclaration clone = prototype.clone();
		clone.accept(new TemplateInstantiator(), instance);
		return clone;
	}
}
