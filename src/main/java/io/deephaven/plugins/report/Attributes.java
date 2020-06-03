/*
 * Copyright 2020 Deephaven Data Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.deephaven.plugins.report;

public interface Attributes<Self extends Attributes<Self>> extends Iterable<Attribute<?, Self>> {

  <T> Attribute<T, Self> attribute(String key, Class<T> clazz);

  /**
   * Creates a new self with the additional attribute.
   *
   * @param key the attribute key
   * @param value the attribute value
   * @return the new item
   */
  Self withAttribute(String key, Object value);
}
