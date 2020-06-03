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

/** A figure is an {@link Item} wrapping an underlying {@link com.illumon.iris.db.plot.Figure}. */
@Immutable(builder = true, copy = false)
public abstract class FigureLocal extends FigureBase<FigureLocal> {

  /**
   * Constructs a new figure item.
   *
   * @param figure the figure
   * @return the figure item
   */
  public static FigureLocal of(com.illumon.iris.db.plot.Figure figure) {
    return ImmutableFigureLocal.of(figure);
  }

  /**
   * The underlying figure.
   *
   * @return the figure
   */
  @Parameter
  public abstract com.illumon.iris.db.plot.Figure figure();

  @Override
  public final FigureLocal withAttribute(String key, Object value) {
    return ImmutableFigureLocal.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  final FigureLocal self() {
    return this;
  }

  @Override
  public final <V extends Figure.Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
