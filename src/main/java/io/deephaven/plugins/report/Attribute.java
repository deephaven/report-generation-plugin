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

/**
 * An attribute is an additional piece of information that can be attached to an {@link Attributes}.
 *
 * @param <T> the attribute value type
 * @param <Parent> the parent type
 */
public interface Attribute<T, Parent extends Attributes<Parent>> {

  /**
   * The key of the attribute.
   *
   * @return the key
   */
  String key();

  /**
   * The class of the attribute.
   *
   * @return the class
   */
  Class<T> clazz();

  /**
   * Get if the attribute is present.
   *
   * @return true iff the attribute value is present
   */
  boolean isPresent();

  /**
   * Get the attribute's value.
   *
   * @return the value
   * @throws NullPointerException if the value is not present
   */
  T get();

  /**
   * Get the attribute's value, or else return the appropriate value.
   *
   * @param orElse the value to return if no attribute value is found
   * @return the value, or else
   */
  T orElse(T orElse);

  /**
   * Set the attribute's value.
   *
   * @param value the value
   * @return the new item
   */
  Parent with(T value);
}
