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

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

/** Basic authentication via username and password. */
@Immutable(builder = false)
public abstract class AuthenticationBasic implements Authentication {

  /**
   * Constructs a new instance.
   *
   * @param username the username
   * @param password the password
   * @return the new instance
   */
  public static AuthenticationBasic of(String username, String password) {
    return ImmutableAuthenticationBasic.of(username, password);
  }

  /**
   * The username.
   *
   * @return the username
   */
  @Parameter
  public abstract String username();

  /**
   * The password or api key.
   *
   * @return the password
   */
  @Parameter
  public abstract String password();

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
