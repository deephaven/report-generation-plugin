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

/** A collection of static helper functions to aid in the creation and sending of emails. */
public class Functions {

  /**
   * Returns the {@link NonStatic} instance. Allows the caller to bring the scope into a variable.
   *
   * <p>{@code def email = io.deephaven.plugins.email.Functions.nonStatic()}
   *
   * @return the non-static instance
   */
  public static NonStatic nonStatic() {
    return NonStatic.INSTANCE;
  }

  /**
   * Equivalent to {@link Server#builder()}.
   *
   * @return the server builder
   */
  public static Server.Builder server() {
    return Server.builder();
  }

  /**
   * Equivalent to {@link Header#builder()}.
   *
   * @return the header builder
   */
  public static Header.Builder header() {
    return Header.builder();
  }

  /**
   * Equivalent to {@link Trailer#none()}.
   *
   * @return the trailer
   */
  public static Trailer noTrailer() {
    return Trailer.none();
  }

  /**
   * Equivalent to {@link Trailer#of(String)}.
   *
   * @param html the html
   * @return the trailer
   */
  public static Trailer customTrailer(String html) {
    return Trailer.of(html);
  }

  /**
   * Configures the email server with the default settings for {@code smtp.googlemail.com}.
   *
   * @param username the gmail username
   * @param password the gmail password or api key
   * @return the server configuration
   */
  public static Server gmail(String username, String password) {
    return server()
        .hostName("smtp.googlemail.com")
        .smtpPort(465)
        .sslOnConnect(true)
        .auth(auth(username, password))
        .build();
  }

  /**
   * Configures the email server against the localhost.
   *
   * <p>Equivalent to {@code server().hostName("localhost").build()}.
   *
   * @return the server configuration
   */
  public static Server localhost() {
    return server().hostName("localhost").build();
  }

  /**
   * Equivalent to {@link AuthenticationBasic#of(String, String)}.
   *
   * @param username the username
   * @param password the password
   * @return the authentication
   */
  public static AuthenticationBasic auth(String username, String password) {
    return AuthenticationBasic.of(username, password);
  }

  /**
   * Constructs a new {@link EmailSendingConfig} with the default {@link Trailer}.
   *
   * @param server the server
   * @param header the header
   * @param reports the reports
   * @return the config
   */
  public static EmailSendingConfig email(Server server, Header header, Report... reports) {
    return EmailSendingConfig.builder().server(server).header(header).addReports(reports).build();
  }

  /**
   * Constructs a new {@link EmailSendingConfig}.
   *
   * @param server the server
   * @param header the header
   * @param trailer the trailer
   * @param reports the reports
   * @return the config
   */
  public static EmailSendingConfig email(
      Server server, Header header, Trailer trailer, Report... reports) {
    return EmailSendingConfig.builder()
        .server(server)
        .header(header)
        .addReports(reports)
        .trailer(trailer)
        .build();
  }

  /**
   * Creates and sends an email for the {@code reports}, with the default {@link Trailer}.
   *
   * @param server the email server configuration
   * @param header the email header configuration
   * @param reports the reports to send
   * @see EmailSendingConfig#send()
   */
  public static void send(Server server, Header header, Report... reports) {
    email(server, header, reports).send();
  }

  /**
   * Creates and sends an email for the {@code reports}.
   *
   * @param server the email server configuration
   * @param header the email header configuration
   * @param trailer the trailer to use
   * @param reports the reports to send
   * @see EmailSendingConfig#send()
   */
  public static void send(Server server, Header header, Trailer trailer, Report... reports) {
    email(server, header, trailer, reports).send();
  }

  private Functions() {}

  /** A wrapper that presents the static functions of {@link Functions} as non-static methods. */
  public enum NonStatic {
    INSTANCE;

    public Server.Builder server() {
      return Functions.server();
    }

    public Header.Builder header() {
      return Functions.header();
    }

    public Trailer noTrailer() {
      return Functions.noTrailer();
    }

    public Trailer customTrailer(String html) {
      return Functions.customTrailer(html);
    }

    public Server gmail(String username, String password) {
      return Functions.gmail(username, password);
    }

    public Server localhost() {
      return Functions.localhost();
    }

    public AuthenticationBasic auth(String username, String password) {
      return Functions.auth(username, password);
    }

    public EmailSendingConfig email(Server server, Header header, Report... reports) {
      return Functions.email(server, header, reports);
    }

    public EmailSendingConfig email(
        Server server, Header header, Trailer trailer, Report... reports) {
      return Functions.email(server, header, trailer, reports);
    }

    public void send(Server server, Header header, Report... reports) {
      Functions.send(server, header, reports);
    }

    public void send(Server server, Header header, Trailer trailer, Report... reports) {
      Functions.send(server, header, trailer, reports);
    }
  }
}
