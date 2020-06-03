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
package com.illumon.iris.db.tables.remote;

import com.fishlib.io.logger.Logger;
import com.illumon.iris.db.tables.Table;
import com.illumon.iris.db.tables.TableDefinition;
import com.illumon.iris.db.tables.databases.Database;
import com.illumon.iris.db.tables.remote.ReportTableCARQ.ReportTableCARQResult;
import com.illumon.iris.db.tables.remotequery.ContextAwareRemoteQuery;
import com.illumon.iris.db.tables.remotequery.RemoteQueryProcessor;
import com.illumon.iris.db.tables.select.QueryScope;
import com.illumon.iris.db.util.liveness.LivenessScopeStack;
import com.illumon.iris.db.v2.BaseTable;
import com.illumon.iris.db.v2.remote.ConstructSnapshot;
import com.illumon.iris.db.v2.remote.InitialSnapshot;
import com.illumon.iris.db.v2.remote.InitialSnapshotTable;
import com.illumon.util.SafeCloseable;
import io.deephaven.plugins.annotations.MinimalStyle;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * Fetches a limited view and constructs a {@link InitialSnapshot} of the given table.
 *
 * @see io.deephaven.plugins.report.TablePQ#toLocal(Logger, Duration)
 */
@MinimalStyle
@Immutable
public abstract class ReportTableCARQ extends ContextAwareRemoteQuery<ReportTableCARQResult> {

  private static final long serialVersionUID = 1234906716116716235L;

  @MinimalStyle
  @Immutable
  public abstract static class ReportTableCARQResult implements Serializable {

    private static final long serialVersionUID = 7628161265106106150L;

    @Parameter
    public abstract TableDefinition definition();

    @Parameter
    public abstract InitialSnapshot snapshot();

    public final Table toTable() {
      return InitialSnapshotTable.setupInitialSnapshotTable(definition(), snapshot());
    }
  }

  /**
   * Constructs a new instance.
   *
   * @param name the name
   * @param maxRows the maxRows
   * @param columns the columns
   * @return the new instance
   */
  public static ReportTableCARQ of(String name, long maxRows, List<String> columns) {
    return ImmutableReportTableCARQ.of(name, maxRows, columns);
  }

  /**
   * The variable name for the table.
   *
   * @return the name
   */
  @Parameter
  public abstract String name();

  /**
   * The maximum size for the report.
   *
   * @return the maximum size
   */
  @Parameter
  public abstract long maxRows();

  /**
   * The columns to include in the report. An empty list indicates all columns.
   *
   * @return the columns
   */
  @Parameter
  public abstract List<String> columns();

  public final Table execute(RemoteDatabase db) throws Exception {
    return db.executeConcurrentQuery(this).toTable();
  }

  @Override
  @SuppressWarnings("try")
  public final ReportTableCARQResult execute(Database database) {
    try (final SafeCloseable ignored = LivenessScopeStack.open()) {
      Table table = QueryScope.getDefaultInstance().readParamValue(name());

      // todo: this logic should be consolidated b/c it's used in a couple of different places.
      table = RemoteDatabase.applyAcls(table, getUserContext(), getRemoteQueryProcessor(), name());
      table = RemoteDatabase.applyInputTableChecks(database, table, getUserContext(), name());

      final RemoteQueryProcessor remoteQueryProcessor = getRemoteQueryProcessor();
      if (remoteQueryProcessor.auditTableAccess()) {
        final TableDefinition tableDefinition = table.getDefinition();
        final ExportedObjectClient client = getClient();
        final String queryDescription = getRemoteQueryProcessor().getQueryDescription();
        remoteQueryProcessor.logTableAccessAuditEvent(
            getUserContext(),
            client.getRemoteHostName(),
            client.getRemotePort(),
            tableDefinition.getNamespace(),
            tableDefinition.getName(),
            queryDescription == null
                ? "ReportTableCARQ, Name=" + name()
                : "ReportTableCARQ, Query=" + queryDescription + ", Name=" + name());
      }

      table = table.head(maxRows()).view(columns());
      final InitialSnapshot snapshot =
          ConstructSnapshot.constructInitialSnapshot(this, (BaseTable) table);
      return ImmutableReportTableCARQResult.of(table.getDefinition(), snapshot);
    }
  }
}
