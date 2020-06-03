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

import org.immutables.value.Value.Check;
import org.immutables.value.Value.Immutable;

/**
 * The email-message specific configuration, except for the body of the email itself.
 *
 * @see Server
 */
@Immutable(builder = true, copy = false)
public abstract class Header {

  /** The builder. */
  public static class Builder extends ImmutableHeader.Builder {}

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The non-empty sender.
   *
   * @return the sender
   */
  public abstract String sender();

  /**
   * The subject line.
   *
   * @return the subject
   */
  public abstract String subject();

  /**
   * The TO recipients.
   *
   * @return the TO recipients
   */
  public abstract java.util.List<String> recipients();

  /**
   * The CC recipients.
   *
   * @return the CC recipients
   */
  public abstract java.util.List<String> recipientsCC();

  /**
   * The BCC recipients.
   *
   * @return the BCC recipients
   */
  public abstract java.util.List<String> recipientsBCC();

  @Check
  final void check() {
    if (sender().isEmpty()) {
      throw new IllegalArgumentException("from must be non-empty");
    }
    // todo: can recipients be empty of CC or BCC is non-empty?
  }
}
