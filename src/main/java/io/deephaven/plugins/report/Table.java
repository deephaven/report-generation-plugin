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

/** A table is an {@link Item} wrapping an underlying {@link com.illumon.iris.db.tables.Table}. */
@Immutable
public abstract class Table extends ItemBase<Table> {

  /**
   * Constructs a new table item.
   *
   * @param table the table
   * @return the table item
   */
  public static Table of(com.illumon.iris.db.tables.Table table) {
    return ImmutableTable.of(table);
  }

  /**
   * The underlying table.
   *
   * @return the table
   */
  @Parameter
  public abstract com.illumon.iris.db.tables.Table value();

  @Override
  public final Table withAttribute(String key, Object value) {
    return ImmutableTable.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
