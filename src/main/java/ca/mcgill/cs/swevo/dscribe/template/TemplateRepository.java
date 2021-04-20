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
package ca.mcgill.cs.swevo.dscribe.template;

import java.util.List;

public interface TemplateRepository extends Iterable<String>
{
	/**
	 * Query to verify if a specific template method exists
	 *
	 * @param templateName
	 *            the name of the template
	 * @return true if a template exists for the specified name
	 */
	public boolean contains(String templateName);

	/**
	 * Query for a template method of the scaffold using its name. The MethodDeclaration object returned is a clone to
	 * ensure the original one is untouched.
	 *
	 * @param templateName
	 *            Name of template method.
	 * @return MethodDeclaration clone of the template method.
	 */
	public List<Template> get(String templateName);
}
