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

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.Arrays;

/** A collection of static helper functions to aid in the creation of {@link Report reports}. */
public class Functions {

  /**
   * Returns the {@link io.deephaven.plugins.report.Functions.NonStatic} instance. Allows the caller
   * to bring the scope into a variable.
   *
   * <p>{@code def reports = io.deephaven.plugins.report.Functions.nonStatic()}
   *
   * @return the non-static instance
   */
  public static NonStatic nonStatic() {
    return NonStatic.INSTANCE;
  }

  // --------------------------------------------------------------------------------------------

  /**
   * Equivalent to {@link Report#of(String, Item)}.
   *
   * @param title the title
   * @param item the item
   * @return the report
   */
  public static Report report(String title, Item<?> item) {
    return Report.of(title, item);
  }

  /**
   * Equivalent to {@link Report#of(String, Item, Instant)}.
   *
   * @param title the title
   * @param item the item
   * @param timestamp the timestamp
   * @return the report
   */
  public static Report report(String title, Item<?> item, Instant timestamp) {
    return Report.of(title, item, timestamp);
  }

  /**
   * Equivalent to {@link Text#of(String)}.
   *
   * @param text the text
   * @return the item
   */
  public static Text text(String text) {
    return Text.of(text);
  }

  /**
   * Equivalent to {@link TableLocal#of(com.illumon.iris.db.tables.Table)}.
   *
   * @param table the table
   * @return the table item
   */
  public static TableLocal table(com.illumon.iris.db.tables.Table table) {
    return TableLocal.of(table);
  }

  /**
   * Equivalent to {@link TablePQ#of(PQ, String)}.
   *
   * @param pq the pq
   * @param tableName the table name
   * @return the table pq
   */
  public static TablePQ table(PQ pq, String tableName) {
    return TablePQ.of(pq, tableName);
  }

  /**
   * Equivalent to {@link FigureLocal#of(com.illumon.iris.db.plot.Figure)}.
   *
   * @param figure the figure
   * @return the figure item
   */
  public static FigureLocal figure(com.illumon.iris.db.plot.Figure figure) {
    return FigureLocal.of(figure);
  }

  /**
   * Equivalent to {@link FigurePQ#of(PQ, String)}.
   *
   * @param pq the pq
   * @param figureName the figure name
   * @return the figure pq
   */
  public static FigurePQ figure(PQ pq, String figureName) {
    return FigurePQ.of(pq, figureName);
  }

  /**
   * Equivalent to {@code Group.builder().addItems(items).build()}.
   *
   * @param items the items
   * @return the group item
   */
  public static Group group(Item<?>... items) {
    return Group.builder().addItems(items).build();
  }

  /**
   * Equivalent to {@link PQName#of(String, String)}.
   *
   * @param owner the pq owner
   * @param name the pq name
   * @return the pq
   */
  public static PQName pq(String owner, String name) {
    return PQName.of(owner, name);
  }

  /**
   * Equivalent to {@link PQSerialId#of(long)}.
   *
   * @param serialId the serial id
   * @return the pq
   */
  public static PQSerialId pq(long serialId) {
    return PQSerialId.of(serialId);
  }

  // --------------------------------------------------------------------------------------------

  public static <T extends Item<?>> T item(T item) {
    return item;
  }

  /**
   * Coerces the array of objects into an {@link Group}, where each item is further coerced.
   *
   * @param objects the objects
   * @return the group
   */
  public static Group item(Object... objects) {
    return item(Arrays.asList(objects));
  }

  /**
   * Coerces the iterable of objects into an {@link Group}, where each item is further coerced.
   *
   * @param objects the objects
   * @return the group
   */
  public static Group item(Iterable<?> objects) {
    final Group.Builder builder = Group.builder();
    for (Object o : objects) {
      builder.addItems(item(o));
    }
    return builder.build();
  }

  /**
   * Coerces an object into an {@link Item}.
   *
   * <p>In order, if the {@code object} is an instance of:
   *
   * <ol>
   *   <li>an {@link Item}, the item is returned
   *   <li>a {@link CharSequence}, an appropriate {@link Text} is returned
   *   <li>a {@link com.illumon.iris.db.tables.Table}, an appropriate {@link TableLocal} is returned
   *   <li>a {@link com.illumon.iris.db.plot.Figure}, an appropriate {@link FigureLocal} is returned
   *   <li>an {@link Iterable} or array, a {@link Group} is returned where each item is further
   *       coerced
   * </ol>
   *
   * If none of the above conditions are met, an {@link IllegalArgumentException} will be thrown.
   *
   * @param object the object to coerce
   * @return the item
   */
  public static Item<?> item(Object object) {
    // note: Report is *not* an Item, and should not be adapted.
    if (object instanceof Item) {
      return (Item<?>) object;
    }
    if (object instanceof CharSequence) {
      return text(((CharSequence) object).toString());
    }
    if (object instanceof com.illumon.iris.db.tables.Table) {
      return table((com.illumon.iris.db.tables.Table) object);
    }
    if (object instanceof com.illumon.iris.db.plot.Figure) {
      return figure((com.illumon.iris.db.plot.Figure) object);
    }
    if (object instanceof Iterable) {
      return item((Iterable<?>) object);
    }
    if (object.getClass().isArray()) {
      final Group.Builder builder = Group.builder();
      final int L = Array.getLength(object);
      for (int i = 0; i < L; ++i) {
        builder.addItems(item(Array.get(object, i)));
      }
      return builder.build();
    }
    throw new IllegalArgumentException(
        String.format(
            "Object of type '%s' is not adaptable into a Report item", object.getClass()));
  }

  /**
   * A coerced equivalent of {@link #report(String, Item)}.
   *
   * @param title the title
   * @param item the item
   * @return the report
   */
  public static Report report(String title, Object item) {
    return report(title, item(item));
  }

  /**
   * A coerced equivalent of {@link #report(String, Item)}.
   *
   * @param title the title
   * @param items the items
   * @return the report
   */
  public static Report report(String title, Object... items) {
    return report(title, item(items));
  }

  /**
   * Coerces the object and attaches a name.
   *
   * @param name the name to attach
   * @param object the object
   * @return the new item
   */
  public static Item<?> named(String name, Object object) {
    return item(object).withName(name);
  }

  /**
   * Coerces the objects and attaches a name.
   *
   * @param name the name
   * @param objects the objects
   * @return the new item
   */
  public static Item<?> named(String name, Object... objects) {
    return item(objects).withName(name);
  }

  /**
   * Coerces the objects and attaches an attribute.
   *
   * @param object the object
   * @param key the attribute key
   * @param value the attribute value
   * @return the new item
   */
  public static Item<?> attr(Object object, String key, Object value) {
    return item(object).withAttribute(key, value);
  }

  // --------------------------------------------------------------------------------------------

  /** Should not be instantiated. */
  private Functions() {}

  /** A wrapper that presents the static functions of {@link Functions} as non-static methods. */
  public enum NonStatic {
    INSTANCE;

    public Report report(String title, Item<?> item) {
      return Functions.report(title, item);
    }

    public Report report(String title, Item<?> item, Instant timestamp) {
      return Functions.report(title, item, timestamp);
    }

    public Text text(String text) {
      return Functions.text(text);
    }

    public TableLocal table(com.illumon.iris.db.tables.Table table) {
      return Functions.table(table);
    }

    public TablePQ table(PQ pq, String tableName) throws Exception {
      return Functions.table(pq, tableName);
    }

    public FigureLocal figure(com.illumon.iris.db.plot.Figure figure) {
      return Functions.figure(figure);
    }

    public FigurePQ figure(PQ pq, String figureName) {
      return Functions.figure(pq, figureName);
    }

    public Group group(Item<?>... items) {
      return Functions.group(items);
    }

    public <T extends Item<?>> T item(T item) {
      return Functions.item(item);
    }

    public Group item(Iterable<Object> items) {
      return Functions.item(items);
    }

    public Group item(Object... objects) {
      return Functions.item(objects);
    }

    public Item<?> item(Object object) {
      return Functions.item(object);
    }

    public Report report(String name, Object item) {
      return Functions.report(name, item);
    }

    public Report report(String name, Object... items) {
      return Functions.report(name, items);
    }

    public Item<?> named(String name, Object item) {
      return Functions.named(name, item);
    }

    public Item<?> named(String name, Object... items) {
      return Functions.named(name, items);
    }

    public Item<?> attr(Object item, String key, Object value) {
      return Functions.attr(item, key, value);
    }

    public PQName pq(String owner, String name) {
      return Functions.pq(owner, name);
    }

    public PQSerialId pq(long serialId) {
      return Functions.pq(serialId);
    }
  }
}
