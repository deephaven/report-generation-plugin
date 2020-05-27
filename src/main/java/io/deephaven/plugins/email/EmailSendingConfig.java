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

import io.deephaven.plugins.report.Report;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import org.apache.commons.mail.EmailException;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

/** The full configuration object related to sending report-based emails. */
@Immutable
public abstract class EmailSendingConfig {

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

  /** Renders and sends the reports-based email. */
  public final void send() {
    try {
      new InlineHtmlRenderer(this).render().send();
    } catch (EmailException e) {
      throw new RuntimeException(e);
    }
  }

  @Check
  final void check() {
    if (reports().isEmpty()) {
      throw new IllegalArgumentException("reports must be non-empty");
    }
  }
}
