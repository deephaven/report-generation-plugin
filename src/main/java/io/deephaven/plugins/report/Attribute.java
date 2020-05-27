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

import java.util.Objects;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * An attribute is an additional piece of information that can be attached to a {@link Report}.
 *
 * @param <T> the attribute value's type
 * @param <I> the item type this attribute belongs to
 */
@Immutable(builder = false)
public abstract class Attribute<T, I extends Item<I>> {

  /**
   * The item of the attribute.
   *
   * @return the item
   */
  @Parameter
  public abstract Item<I> item();

  /**
   * The key of the attribute.
   *
   * @return the key
   */
  @Parameter
  public abstract String key();

  /**
   * The class of the attribute.
   *
   * @return the class
   */
  @Parameter
  public abstract Class<T> clazz();

  /**
   * Equivalent to {@code item().attributes().containsKey(key())}.
   *
   * @return true iff the attribute value is present
   */
  public final boolean isPresent() {
    return item().attributes().containsKey(key());
  }

  /**
   * Get the attribute's value.
   *
   * <p>Equivalent to {@code item().attributes().get(key())}.
   *
   * @return the value
   * @throws NullPointerException if the value is not present
   */
  public final T get() {
    return Objects.requireNonNull(clazz().cast(item().attributes().get(key())));
  }

  /**
   * Get the attribute's value, or else return the appropriate value.
   *
   * @param orElse the value to return if no attribute value is found
   * @return the value, or else
   */
  public final T orElse(T orElse) {
    final T existing = clazz().cast(item().attributes().get(key()));
    if (existing != null) {
      return existing;
    }
    return orElse;
  }

  /**
   * Set the attribute's value.
   *
   * <p>Equivalent to {@code item().withAttribute(key(), value)}.
   *
   * @param value the value
   * @return the new item
   */
  public final I with(T value) {
    return item().withAttribute(key(), value);
  }
}
