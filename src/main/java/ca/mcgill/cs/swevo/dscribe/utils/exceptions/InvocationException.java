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
package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class InvocationException extends RuntimeException
{
	/** Automatically generated */
	private static final long serialVersionUID = -2588400211554024832L;

	public InvocationException(InvocationError error)
	{
		this(error, null);
	}

	public InvocationException(InvocationError error, Throwable cause)
	{
		super("Template invocation error due to " + error.name(), cause);
	}

	public static enum InvocationError
	{
		EXTERNAL_IO, BAD_FORMAT
	}
}
