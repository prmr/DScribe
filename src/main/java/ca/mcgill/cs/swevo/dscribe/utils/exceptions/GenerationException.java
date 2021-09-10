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
package ca.mcgill.cs.swevo.dscribe.utils.exceptions;

public class GenerationException extends RuntimeException {
  /** Automatically generated */
  private static final long serialVersionUID = 5251901280853682051L;

  public GenerationException(GenerationError error) {
    this(error, null);
  }

  public GenerationException(GenerationError error, Throwable cause) {
    super("Generation error due to " + error.name(), cause);
  }

  public enum GenerationError {
    MISSING_SOURCE_FILE, INVALID_SOURCE_FILE, IO_ERROR
  }
}
