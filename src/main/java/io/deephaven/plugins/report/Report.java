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
import java.time.Duration;
import java.time.Instant;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/**
 * A report contains a top-level {@link #item() item}, a {@link #title() title}, and a {@link
 * #timestamp() timestamp}.
 */
@Immutable
public abstract class Report {

  /**
   * Equivalent to {@code of(title, item, Instant.now())}.
   *
   * @param title the non-empty title
   * @param item the item
   * @return the report
   */
  public static Report of(String title, Item<?> item) {
    return of(title, item, Instant.now());
  }

  /**
   * Constructs a report.
   *
   * @param title the non-empty title
   * @param item the item
   * @param timestamp the timestamp
   * @return the report
   */
  public static Report of(String title, Item<?> item, Instant timestamp) {
    return ImmutableReport.of(title, item, timestamp);
  }

  /**
   * The non-empty title of the report.
   *
   * @return the title
   */
  @Parameter
  public abstract String title();

  /**
   * The item of the report.
   *
   * @return the item
   */
  @Parameter
  public abstract Item<?> item();

  /**
   * The timestamp of the report.
   *
   * @return the timestamp
   */
  @Parameter
  public abstract Instant timestamp();

  public final String toGroovyishDebug() {
    return String.format(
        "report(%s, %s)", ItemToGroovyish.toString(title()), ItemToGroovyish.toString(item()));
  }

  public final Report toLocal(Logger log, Duration duration) {
    return of(title(), ToLocalVisitor.toLocal(item(), log, duration), timestamp());
  }

  @Check
  final void check() {
    if (title().isEmpty()) {
      throw new IllegalArgumentException("A report title may not be empty");
    }
  }
}
