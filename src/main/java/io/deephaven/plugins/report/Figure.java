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

/**
 * A figure.
 *
 * @param <Self> the figure type
 */
public interface Figure<Self extends Figure<Self>> extends Item<Self> {

  /** The visitor-pattern visitor. */
  interface Visitor {
    void visit(FigureLocal figure);

    void visit(FigurePQ figure);
  }

  /** @return the {@code size} attribute */
  Attribute<Size2D, Self> size();

  /**
   * Sets the {@code size} attribute.
   *
   * @param width the width
   * @param height the height
   * @return the new figure
   */
  Self withSize(int width, int height);

  /**
   * Sets the {@code size} attribute.
   *
   * @param size the size
   * @return the new figure
   */
  Self withSize(Size2D size);

  /**
   * The visitor-pattern dispatcher.
   *
   * @param visitor the visitor
   * @param <V> the visitor type
   * @return the visitor
   */
  <V extends Visitor> V walk(V visitor);
}
