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
package io.deephaven.plugins.email;

import com.fishlib.io.logger.Logger;
import com.illumon.iris.db.tables.live.LiveTableMonitor;
import com.illumon.util.FunctionalInterfaces.ThrowingSupplier;
import io.deephaven.plugins.report.Report;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.mail.ImageHtmlEmail;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

/** The full configuration object related to sending report-based emails. */
@Immutable(builder = true, copy = true)
public abstract class EmailSendingConfig {

  /** The lock type. */
  public enum LockType {
    /** Do not acquire a lock to render the email. */
    NONE {
      @Override
      ImageHtmlEmail render(InlineHtmlRenderer renderer) throws Exception {
        return renderer.render();
      }
    },

    /** Acquire a {@link LiveTableMonitor#sharedLock()} to render the email. */
    SHARED {
      @Override
      ImageHtmlEmail render(InlineHtmlRenderer renderer) throws Exception {
        return LiveTableMonitor.DEFAULT
            .sharedLock()
            .computeLocked((ThrowingSupplier<ImageHtmlEmail, Exception>) renderer::render);
      }
    },

    /** Acquire an {@link LiveTableMonitor#exclusiveLock()} ()} to render the email. */
    EXCLUSIVE {
      @Override
      ImageHtmlEmail render(InlineHtmlRenderer renderer) throws Exception {
        return LiveTableMonitor.DEFAULT
            .exclusiveLock()
            .computeLocked((ThrowingSupplier<ImageHtmlEmail, Exception>) renderer::render);
      }
    };

    abstract ImageHtmlEmail render(InlineHtmlRenderer renderer) throws Exception;
  }

  /** The builder. */
  public static class Builder extends ImmutableEmailSendingConfig.Builder {}

  /**
   * A new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The server.
   *
   * @return the server
   */
  public abstract Server server();

  /**
   * The header.
   *
   * @return the header
   */
  public abstract Header header();

  /**
   * The trailer.
   *
   * <p>Defaults to {@link Trailer#deephaven()}.
   *
   * @return the trailer
   */
  @Default
  public Trailer trailer() {
    return Trailer.deephaven();
  }

  /**
   * The temporary directory where images and other data may be written to.
   *
   * <p>Defaults to the system's temporary directory.
   *
   * @return the temporary directory
   */
  @Default
  public File tmpDirectory() {
    try {
      return Files.createTempDirectory("EmailSendingConfig").toFile();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * The reports.
   *
   * @return the reports
   */
  public abstract List<Report> reports();

  /**
   * The lock type. Defaults to {@link LockType#SHARED}.
   *
   * @return the lock type
   */
  @Default
  public LockType lockType() {
    return LockType.SHARED;
  }

  /**
   * The timeout. Defaults to {@code Duration.ofSeconds(5)}.
   *
   * @return the timeout
   */
  @Default
  public Duration timeout() {
    return Duration.ofSeconds(5);
  }

  /**
   * Creates a copy of this config but with the new value for {@link #lockType()}.
   *
   * @param lockType the lock type
   * @return the new instance
   */
  public abstract EmailSendingConfig withLockType(LockType lockType);

  /**
   * Creates a copy of this config but with the new value for {@link #timeout()}.
   *
   * @param timeout the timeout
   * @return the new instance
   */
  public abstract EmailSendingConfig withTimeout(Duration timeout);

  /** Renders and sends the reports-based email. */
  public final void send() throws Exception {
    final EmailSendingConfig local =
        EmailSendingConfig.builder()
            .from(this)
            .reports(
                reports().stream()
                    .map(r -> r.toLocal(Logger.NULL, timeout()))
                    .collect(Collectors.toList()))
            .build();
    lockType().render(new InlineHtmlRenderer(local)).send();
  }

  @Check
  final void check() {
    if (reports().isEmpty()) {
      throw new IllegalArgumentException("reports must be non-empty");
    }
  }
}
