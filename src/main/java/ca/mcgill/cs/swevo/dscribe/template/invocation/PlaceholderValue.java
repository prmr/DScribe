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
package ca.mcgill.cs.swevo.dscribe.template.invocation;

import java.util.Collections;
import java.util.List;

public class PlaceholderValue {
  private List<String> value;

  public PlaceholderValue(String... values) {
    this.value = List.of(values);
  }

  public boolean isList() {
    return value.size() != 1;
  }

  public String getValue() {
    assert value.size() == 1;
    return value.get(0);
  }

  public List<String> getValueAsList() {
    return Collections.unmodifiableList(value);
  }

  @Override
  public String toString() {
    if (isList()) {
      return value.toString();
    } else {
      return getValue();
    }
  }
}
