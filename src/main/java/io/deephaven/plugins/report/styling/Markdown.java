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

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * An {@link io.deephaven.plugins.report.Attribute} for annotating an {@link
 * io.deephaven.plugins.report.Item} with markdown.
 */
@Immutable
public abstract class Markdown {

  @Parameter
  public abstract String value();

  /**
   * Constructs a new markdown
   *
   * @param value the value
   * @return the size 2d
   */
  public static Markdown of(String value) {
    return ImmutableMarkdown.of(value);
  }
}
