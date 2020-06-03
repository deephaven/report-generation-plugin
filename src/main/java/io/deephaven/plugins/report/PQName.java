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

import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/** A reference to a persistent-query by owner and name. */
@Immutable
public abstract class PQName implements PQ {

  /**
   * Construct a new instance.
   *
   * @param owner the pq owner
   * @param name the pq name
   * @return the pq
   */
  public static PQName of(String owner, String name) {
    return ImmutablePQName.of(owner, name);
  }

  /**
   * The persistent-query owner.
   *
   * @return the owner
   */
  @Parameter
  public abstract String owner();

  /**
   * The persistent-query name.
   *
   * @return the name
   */
  @Parameter
  public abstract String name();

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }

  @Check
  final void check() {
    if (owner().isEmpty()) {
      throw new IllegalArgumentException("owner must be non-empty");
    }
    if (name().isEmpty()) {
      throw new IllegalArgumentException("name must be non-empty");
    }
  }
}
