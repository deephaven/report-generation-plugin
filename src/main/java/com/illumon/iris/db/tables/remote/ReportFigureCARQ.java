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
import com.illumon.iris.db.exceptions.TableAccessException;
import com.illumon.iris.db.plot.FigureWidget;
import com.illumon.iris.db.tables.databases.Database;
import com.illumon.iris.db.tables.remotequery.ContextAwareRemoteQuery;
import com.illumon.iris.db.tables.remotequery.RemoteQueryProcessor;
import com.illumon.iris.db.tables.select.QueryScope;
import com.illumon.iris.db.tables.utils.LiveWidget;
import com.illumon.iris.db.util.liveness.LivenessScopeStack;
import com.illumon.util.SafeCloseable;
import io.deephaven.plugins.annotations.MinimalStyle;
import java.time.Duration;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * Fetches a given {@link FigureWidget}.
 *
 * @see io.deephaven.plugins.report.FigurePQ#toLocal(Logger, Duration)
 */
@MinimalStyle
@Immutable
public abstract class ReportFigureCARQ extends ContextAwareRemoteQuery<Inflatable<FigureWidget>> {

  private static final long serialVersionUID = 1050567606016701232L;

  /**
   * Constructs a new instance.
   *
   * @param name the name
   * @return the new instance
   */
  public static ReportFigureCARQ of(String name) {
    return ImmutableReportFigureCARQ.of(name);
  }

  /**
   * The name of the figure.
   *
   * @return the name
   */
  @Parameter
  public abstract String name();

  @Override
  @SuppressWarnings("try")
  public final Inflatable<FigureWidget> execute(Database database) {
    try (final SafeCloseable ignored = LivenessScopeStack.open()) {
      final FigureWidget figure = QueryScope.getDefaultInstance().readParamValue(name());
      final RemoteQueryProcessor remoteQueryProcessor = getRemoteQueryProcessor();
      if (remoteQueryProcessor.auditTableAccess()) {
        final String widgetInfo = figure.getClass().getName();
        final ExportedObjectClient client = getClient();
        final String queryDescription = getRemoteQueryProcessor().getQueryDescription();
        remoteQueryProcessor.logWidgetAccessAuditEvent(
            getUserContext(),
            client.getRemoteHostName(),
            client.getRemotePort(),
            null,
            null,
            queryDescription == null
                ? "ReportFigureCARQ, Name=" + name() + ", Widget class=" + widgetInfo
                : "ReportFigureCARQ, Query="
                    + queryDescription
                    + ", Name="
                    + name()
                    + ", Widget class="
                    + widgetInfo);
      }
      final LiveWidget<?> postACL =
          RemoteDatabase.applyAcls(figure, getUserContext(), remoteQueryProcessor, name());
      if (postACL != figure) {
        throw new TableAccessException("Unable to save figure that has ACLs applied");
      }
      final LiveWidget<?> postVisiblity =
          RemoteDatabase.checkWidgetVisibility(
              figure, getUserContext(), getRemoteQueryProcessor(), name());
      if (postVisiblity != figure) {
        throw new TableAccessException("Unable to save figure that is not visible");
      }
      return figure.deflate(getClient());
    }
  }
}
