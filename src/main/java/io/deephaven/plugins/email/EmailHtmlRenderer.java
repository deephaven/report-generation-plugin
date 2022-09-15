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

import io.deephaven.plugins.html.InlineHtmlRenderer;
import java.util.Objects;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;

class EmailHtmlRenderer {

  private final EmailSendingConfig config;

  EmailHtmlRenderer(EmailSendingConfig reports) {
    this.config = Objects.requireNonNull(reports);
  }

  ImageHtmlEmail render() throws EmailException {
    final String html = createHtml();

    final Server server = config.server();
    final Header header = config.header();

    final ImageHtmlEmail out = new ImageHtmlEmail();
    out.setHostName(server.hostName());
    server.smtpPort().ifPresent(out::setSmtpPort);
    out.setSSLOnConnect(server.sslOnConnect());
    server
        .auth()
        .walk(
            new Authentication.Visitor() {
              @Override
              public void visit(AuthenticationBasic auth) {
                out.setAuthenticator(new DefaultAuthenticator(auth.username(), auth.password()));
              }

              @Override
              public void visit(AuthenticationNone auth) {
                // do nothing
              }
            });
    out.setFrom(header.sender());
    out.setSubject(header.subject());
    for (String recipient : header.recipients()) {
      out.addTo(recipient);
    }
    for (String cc : header.recipientsCC()) {
      out.addCc(cc);
    }
    for (String bcc : header.recipientsBCC()) {
      out.addBcc(bcc);
    }
    out.setDataSourceResolver(new DataSourceFileResolver());
    out.setHtmlMsg(html);
    out.setTextMsg("Your email client does not support HTML messages");
    return out;
  }

  private String createHtml() {
    return InlineHtmlRenderer.from(config).render();
  }
}
