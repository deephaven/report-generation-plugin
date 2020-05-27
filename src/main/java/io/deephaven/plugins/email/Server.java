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

import java.util.OptionalInt;
import org.immutables.value.Value.Check;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

/**
 * The email-server configuration.
 *
 * @see Header
 */
@Immutable
public abstract class Server {

  /** The builder. */
  public static class Builder extends ImmutableServer.Builder {}

  /**
   * Creates a new builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The non-empty hostname.
   *
   * @return the hostname
   */
  public abstract String hostName();

  /**
   * The optional smtp port.
   *
   * @return the smtp port
   */
  public abstract OptionalInt smtpPort();

  /**
   * Whether SSL should be used on connect.
   *
   * @return ssl on connect
   */
  @Default
  public boolean sslOnConnect() {
    return false;
  }

  /**
   * The authentication.
   *
   * @return the authentication
   */
  @Default
  public Authentication auth() {
    return AuthenticationNone.INSTANCE;
  }

  /**
   * Creates a new instance with the given authentication.
   *
   * @param authentication the authentication
   * @return the new server
   */
  public abstract Server withAuth(Authentication authentication);

  @Check
  final void check() {
    if (hostName().isEmpty()) {
      throw new IllegalArgumentException("hostName must be non-empty");
    }
  }
}
