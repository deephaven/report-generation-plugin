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

import com.fishlib.io.logger.Logger;
import io.deephaven.plugins.report.Group.Builder;
import java.time.Duration;
import java.util.Objects;

class ToLocalVisitor implements Item.Visitor, Table.Visitor, Figure.Visitor {

  public static Item<?> toLocal(Item<?> item, Logger log, Duration duration) {
    return item.walk(new ToLocalVisitor(log, duration)).getOut();
  }

  private final Logger log;
  private final Duration duration;
  private Item<?> out;

  private ToLocalVisitor(Logger log, Duration duration) {
    this.log = Objects.requireNonNull(log);
    this.duration = Objects.requireNonNull(duration);
  }

  public Item<?> getOut() {
    return Objects.requireNonNull(out);
  }

  @Override
  public void visit(Table<?> table) {
    table.walk((Table.Visitor) this);
  }

  @Override
  public void visit(TableLocal table) {
    out = table;
  }

  @Override
  public void visit(TablePQ table) {
    try {
      out = table.toLocal(log, duration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(Figure<?> figure) {
    figure.walk((Figure.Visitor) this);
  }

  @Override
  public void visit(FigureLocal figure) {
    out = figure;
  }

  @Override
  public void visit(FigurePQ figure) {
    try {
      out = figure.toLocal(log, duration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(Text text) {
    out = text;
  }

  @Override
  public void visit(Group group) {
    final Builder builder = Group.builder().attributes(group.attributes());
    for (Item<?> item : group.items()) {
      builder.addItems(toLocal(item, log, duration));
    }
    out = builder.build();
  }
}
