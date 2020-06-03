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
import com.illumon.iris.controller.utils.PersistentQueryTableHelper;
import com.illumon.iris.controller.utils.PersistentQueryTableHelper.HelperPersistentQueryClient;
import io.deephaven.plugins.report.PQ.Visitor;
import java.time.Duration;
import java.util.Objects;

class PQToHelperClient implements Visitor {

  public static HelperPersistentQueryClient of(PQ pq, Logger log, Duration timeout) {
    return pq.walk(new PQToHelperClient(log, timeout)).getOut();
  }

  private final Logger log;
  private final Duration timeout;
  private HelperPersistentQueryClient out;

  private PQToHelperClient(Logger log, Duration timeout) {
    this.log = Objects.requireNonNull(log);
    this.timeout = Objects.requireNonNull(timeout);
  }

  public HelperPersistentQueryClient getOut() {
    return Objects.requireNonNull(out);
  }

  @Override
  public void visit(PQName pq) {
    out =
        PersistentQueryTableHelper.getClientForPersistentQuery(
            log, pq.owner(), pq.name(), timeout.toMillis());
  }

  @Override
  public void visit(PQSerialId pq) {
    out =
        PersistentQueryTableHelper.getClientForPersistentQuery(
            log, pq.serialId(), timeout.toMillis());
  }
}
