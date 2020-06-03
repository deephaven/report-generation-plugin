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

import io.deephaven.plugins.report.Item.Visitor;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringEscapeUtils;

class ItemToGroovyish implements Visitor, Table.Visitor, Figure.Visitor {

  static String toString(Item<?> item) {
    return wrapName(item, item.walk(new ItemToGroovyish()).getOut());
  }

  static String toString(String s) {
    return String.format("\"%s\"", StringEscapeUtils.escapeJava(s));
  }

  private String out;

  public String getOut() {
    return Objects.requireNonNull(out);
  }

  @Override
  public void visit(Figure<?> figure) {
    figure.walk((Figure.Visitor) this);
  }

  @Override
  public void visit(FigureLocal figure) {
    if (figure.size().isPresent()) {
      out =
          String.format(
              "figure(<plot>).withSize(%d, %d)",
              figure.size().get().width(), figure.size().get().height());
    } else {
      out = "<plot>";
    }
  }

  @Override
  public void visit(FigurePQ figure) {
    if (figure.size().isPresent()) {
      out =
          String.format(
              "figure(%s, %s).withSize(%d, %d)",
              PQToGroovyish.toString(figure.pq()),
              toString(figure.figureName()),
              figure.size().get().width(),
              figure.size().get().height());
    } else {
      out =
          String.format(
              "figure(%s, %s)", PQToGroovyish.toString(figure.pq()), toString(figure.figureName()));
    }
  }

  @Override
  public void visit(Group group) {
    out =
        group.items().stream()
            .map(ItemToGroovyish::toString)
            .collect(Collectors.joining(", ", "[", "]"));
  }

  @Override
  public void visit(Table<?> table) {
    table.walk((Table.Visitor) this);
  }

  @Override
  public void visit(TableLocal table) {
    out = "<table>";
  }

  @Override
  public void visit(TablePQ table) {
    out =
        String.format(
            "table(%s, %s)", PQToGroovyish.toString(table.pq()), toString(table.tableName()));
  }

  @Override
  public void visit(Text text) {
    out = toString(text.value());
  }

  private static String wrapName(Item<?> item, String out) {
    if (item.name().isPresent()) {
      return String.format("named(%s, %s)", toString(item.name().get()), out);
    } else {
      return out;
    }
  }
}
