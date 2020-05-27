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
package io.deephaven.plugins.report.styling;

import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/** An {@link io.deephaven.plugins.report.Attribute} target for 2-dimensional sizing information. */
@Immutable(builder = false)
public abstract class Size2D {

  /**
   * Constructs a new size 2d.
   *
   * @param width the width
   * @param height the height
   * @return the size 2d
   */
  public static Size2D of(int width, int height) {
    return ImmutableSize2D.of(width, height);
  }

  /**
   * The height.
   *
   * @return the height
   */
  @Parameter
  public abstract int width();

  /**
   * The height.
   *
   * @return the height
   */
  @Parameter
  public abstract int height();

  @Check
  final void check() {
    if (width() <= 0 || height() <= 0) {
      throw new IllegalArgumentException("width and height must be positive");
    }
  }
}
