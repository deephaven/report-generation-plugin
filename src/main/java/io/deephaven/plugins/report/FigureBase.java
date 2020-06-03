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

public abstract class FigureBase<Self extends FigureBase<Self>> extends ItemBase<Self>
    implements Figure<Self> {

  @Override
  public final Attribute<Size2D, Self> size() {
    return attribute("size", Size2D.class);
  }

  @Override
  public final Self withSize(int width, int height) {
    return withSize(Size2D.of(width, height));
  }

  @Override
  public final Self withSize(Size2D size) {
    return size().with(size);
  }

  @Override
  public final <V extends Item.Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
