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
package ca.mcgill.cs.swevo.dscribe.instance;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ca.mcgill.cs.swevo.dscribe.instance.PlaceholderValue.ParameterAdapter;

@JsonAdapter(ParameterAdapter.class)
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

  public static class ParameterAdapter extends TypeAdapter<PlaceholderValue> {
    private final Gson gson = new Gson();

    @Override
    public PlaceholderValue read(JsonReader in) throws IOException {
      JsonToken token = in.peek();
      switch (token) {
        case STRING:
          return new PlaceholderValue(in.nextString());
        case BEGIN_ARRAY:
          List<String> values = gson.fromJson(in, List.class);
          return new PlaceholderValue(values.toArray(String[]::new));
        // $CASES-OMITTED$
        default:
          throw new JsonParseException("Unexpected token: " + token);
      }
    }

    @Override
    public void write(JsonWriter out, PlaceholderValue instance) throws IOException {
      if (!instance.isList()) {
        out.value(instance.getValue());
        return;
      }
      out.beginArray();
      for (String val : instance.getValueAsList()) {
        out.value(val);
      }
      out.endArray();
    }
  }
}
