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
import com.illumon.iris.db.plot.FigureWidget;
import com.illumon.iris.db.tables.remote.RemoteDatabase;
import com.illumon.iris.db.tables.remote.ReportFigureCARQ;
import java.time.Duration;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

@Immutable(builder = true, copy = false)
public abstract class FigurePQ extends FigureBase<FigurePQ> {
  public static FigurePQ of(PQ pq, String figureName) {
    return ImmutableFigurePQ.of(pq, figureName);
  }

  @Parameter
  public abstract PQ pq();

  @Parameter
  public abstract String figureName();

  @Override
  public final FigurePQ withAttribute(String key, Object value) {
    return ImmutableFigurePQ.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  final FigurePQ self() {
    return this;
  }

  @Override
  public final <V extends Figure.Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }

  public final FigureLocal toLocal(Logger log, Duration timeout) throws Exception {
    final HelperPersistentQueryClient helperClient = PQToHelperClient.of(pq(), log, timeout);
    final PersistentQueryClient client = helperClient.getPersistentQueryClient();
    final RemoteDatabase db = client.getRemoteDatabase();

    final FigureWidget figure =
        db.executeConcurrentQuery(ReportFigureCARQ.of(figureName()))
            .inflate(db.getProcessorConnection());

    figure.waitForData();

    return ImmutableFigureLocal.builder()
        .figure(figure)
        .attributes(attributes())
        .putAttributes("figurePQ", this)
        .build();

    // todo: shutdown
  }
}
