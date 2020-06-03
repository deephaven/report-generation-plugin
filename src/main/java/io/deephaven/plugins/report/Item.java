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

/**
 * An item is the basic building blocks of a {@link Report}.
 *
 * @param <Self> the item's self-referential type
 */
public interface Item<Self extends Item<Self>> extends Attributes<Self> {

  /** The visitor-pattern visitor. */
  interface Visitor {
    void visit(Table<?> table);

    void visit(Figure<?> figure);

    void visit(Text text);

    void visit(Group group);
  }

  /**
   * Equivalent to {@code name().with(name)}.
   *
   * @param name the name
   * @return the new item
   */
  Self withName(String name);

  /**
   * Equivalent to {@code attribute("name", String.class)}.
   *
   * @return the name attribute
   */
  Attribute<String, Self> name();

  /**
   * The visitor-pattern dispatcher.
   *
   * @param visitor the visitor
   * @param <V> the visitor type
   * @return the same visitor
   */
  <V extends Visitor> V walk(V visitor);
}
