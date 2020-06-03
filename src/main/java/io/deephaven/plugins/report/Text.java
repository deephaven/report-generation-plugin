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

import io.deephaven.plugins.report.styling.Markdown;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/** A text is an {@link Item} that represents a raw {@link String}. */
@Immutable(builder = true, copy = false)
public abstract class Text extends ItemBase<Text> {

  /**
   * Constructs the text item.
   *
   * @param text the non-empty text
   * @return the item
   */
  public static Text of(String text) {
    return ImmutableText.of(text);
  }

  /**
   * The underlying non-empty value.
   *
   * @return the value
   */
  @Parameter
  public abstract String value();

  /** @return the {@code markdown} attribute */
  public final Attribute<Markdown, Text> markdown() {
    return attribute("markdown", Markdown.class);
  }

  /**
   * Sets the {@code markdown} attribute.
   *
   * @param markdown the markdown
   * @return the new text
   */
  public final Text withMarkdown(String markdown) {
    return markdown().with(Markdown.of(markdown));
  }

  @Override
  public final Text withAttribute(String key, Object value) {
    return ImmutableText.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  final Text self() {
    return this;
  }

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }

  @Check
  final void check() {
    if (value().isEmpty()) {
      throw new IllegalArgumentException("Text value must be non-empty");
    }
  }
}
