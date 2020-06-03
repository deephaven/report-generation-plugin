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

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/** A persistent-query by serial id. */
@Immutable
public abstract class PQSerialId implements PQ {

  /**
   * Construct a new instance.
   *
   * @param serialId the serial id
   * @return the pq
   */
  public static PQSerialId of(long serialId) {
    return ImmutablePQSerialId.of(serialId);
  }

  /**
   * The serial id.
   *
   * @return the id
   */
  @Parameter
  public abstract long serialId();

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
