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

import io.deephaven.plugins.report.styling.Size2D;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/** A figure is an {@link Item} wrapping an underlying {@link com.illumon.iris.db.plot.Figure}. */
@Immutable
public abstract class Figure extends ItemBase<Figure> {

  /**
   * Constructs a new figure item.
   *
   * @param figure the figure
   * @return the figure item
   */
  public static Figure of(com.illumon.iris.db.plot.Figure figure) {
    return ImmutableFigure.of(figure);
  }

  /**
   * The underlying figure.
   *
   * @return the figure
   */
  @Parameter
  public abstract com.illumon.iris.db.plot.Figure figure();

  /** @return the {@code size} attribute */
  public final Attribute<Size2D, Figure> size() {
    return attribute("size", Size2D.class);
  }

  /**
   * Sets the {@code size} attribute.
   *
   * @param width the width
   * @param height the height
   * @return the new figure
   */
  public final Figure withSize(int width, int height) {
    return size().with(Size2D.of(width, height));
  }

  @Override
  public final Figure withAttribute(String key, Object value) {
    return ImmutableFigure.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
