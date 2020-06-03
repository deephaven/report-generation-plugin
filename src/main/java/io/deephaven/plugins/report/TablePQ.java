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
import com.illumon.iris.controller.PersistentQueryClient;
import com.illumon.iris.controller.utils.PersistentQueryTableHelper.HelperPersistentQueryClient;
import com.illumon.iris.db.tables.remote.RemoteDatabase;
import com.illumon.iris.db.tables.remote.ReportTableCARQ;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

/** A table represented via a persistent-query. */
@Immutable(builder = true, copy = false)
public abstract class TablePQ extends TableBase<TablePQ> {

  /**
   * Constructs a new instance.
   *
   * @param pq the pq
   * @param tableName the table name
   * @return the pq
   */
  public static TablePQ of(PQ pq, String tableName) {
    return builder().pq(pq).tableName(tableName).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableTablePQ.Builder {}

  /**
   * The persistent-query.
   *
   * @return the pq
   */
  public abstract PQ pq();

  /**
   * The variable name for the table.
   *
   * @return the table name
   */
  public abstract String tableName();

  /**
   * The columns to view. An empty list represents all columns, and is the default.
   *
   * @return the columns to view
   */
  public List<String> columns() {
    return Collections.emptyList();
  }

  /**
   * The maximum number of rows to represent.
   *
   * @return the limit
   */
  @Default
  public int maxRows() {
    return 100;
  }

  @Override
  public final TablePQ withAttribute(String key, Object value) {
    return ImmutableTablePQ.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  final TablePQ self() {
    return this;
  }

  @Override
  public final <V extends Table.Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }

  public final TableLocal toLocal(Logger log, Duration timeout) throws Exception {
    final com.illumon.iris.db.tables.Table actualTable = executeInternal(log, timeout);
    final boolean truncated =
        actualTable.size() > maxRows(); // note: we perform the query as limit() + 1
    return ImmutableTableLocal.builder()
        .value(actualTable.head(maxRows()))
        .attributes(attributes())
        .putAttributes("tablePQ", this)
        .putAttributes("truncated", truncated)
        .build();
  }

  @Check
  final void check() {
    if (tableName().isEmpty()) {
      throw new IllegalArgumentException("tableName must be non-empty");
    }
    if (maxRows() <= 0) {
      throw new IllegalArgumentException("limit must be positive");
    }
  }

  private com.illumon.iris.db.tables.Table executeInternal(Logger log, Duration timeout)
      throws Exception {
    final HelperPersistentQueryClient helperClient = PQToHelperClient.of(pq(), log, timeout);
    final PersistentQueryClient client = helperClient.getPersistentQueryClient();
    final RemoteDatabase db = client.getRemoteDatabase();
    // note: doing +1 so we can now if the table is over size, and is represented in a truncated
    // form
    return ReportTableCARQ.of(tableName(), maxRows() + 1, columns()).execute(db);
    // todo: shutdown stuff?
  }
}
