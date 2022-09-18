/*
 * Copyright 2022 Deephaven Data Labs
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
package io.deephaven.plugins.html;

import com.fishlib.io.logger.Logger;
import com.illumon.iris.db.tables.live.LiveTableMonitor;
import com.illumon.util.FunctionalInterfaces;
import io.deephaven.plugins.report.Report;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
public abstract class HTMLFile {

  /** The lock type. */
  public enum LockType {
    /** Do not acquire a lock to render the email. */
    NONE {
      @Override
      String render(InlineHtmlRenderer renderer) throws Exception {
        return renderer.renderHtml();
      }
    },

    /** Acquire a {@link LiveTableMonitor#sharedLock()} to render the email. */
    SHARED {
      @Override
      String render(InlineHtmlRenderer renderer) throws Exception {
        return LiveTableMonitor.DEFAULT
            .sharedLock()
            .computeLocked(
                (FunctionalInterfaces.ThrowingSupplier<String, Exception>) renderer::renderHtml);
      }
    },

    /** Acquire an {@link LiveTableMonitor#exclusiveLock()} ()} to render the email. */
    EXCLUSIVE {
      @Override
      String render(InlineHtmlRenderer renderer) throws Exception {
        return LiveTableMonitor.DEFAULT
            .exclusiveLock()
            .computeLocked(
                (FunctionalInterfaces.ThrowingSupplier<String, Exception>) renderer::renderHtml);
      }
    };

    abstract String render(InlineHtmlRenderer renderer) throws Exception;
  }

  public final void save() throws Exception {
    final HTMLFile local =
        HTMLFile.builder()
            .from(this)
            .reports(
                reports().stream()
                    .map(r -> r.toLocal(Logger.NULL, timeout()))
                    .collect(Collectors.toList()))
            .build();
    final String htmlString = lockType().render(new InlineHtmlFileRenderer(local));
    writeToFile(htmlString);
  }

  private void writeToFile(String htmlString) throws Exception {
    String directory = new File(filePath()).getParent();
    if (null != directory) {
      Files.createDirectories(Paths.get(directory));
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath()))) {
      for (String line : htmlString.split("\\n")) {
        bw.write(line + "\n");
      }
    }
  }

  /**
   * The trailer.
   *
   * <p>Defaults to {@link Trailer#deephaven()}.
   *
   * @return the trailer
   */
  @Value.Default
  public Trailer trailer() {
    return Trailer.deephaven();
  }

  /**
   * The reports.
   *
   * @return the reports
   */
  public abstract List<Report> reports();

  /**
   * The filePath.
   *
   * @return the file path
   */
  public abstract String filePath();

  /**
   * The lock type. Defaults to {@link LockType#SHARED}.
   *
   * @return the lock type
   */
  @Value.Default
  public LockType lockType() {
    return LockType.SHARED;
  }

  /**
   * The timeout. Defaults to {@code Duration.ofSeconds(5)}.
   *
   * @return the timeout
   */
  @Value.Default
  public Duration timeout() {
    return Duration.ofSeconds(5);
  }

  /** The builder. */
  public static class Builder extends ImmutableHTMLFile.Builder {}

  /**
   * A new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }
}
