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

import io.deephaven.plugins.report.Figure;
import io.deephaven.plugins.report.Group;
import io.deephaven.plugins.report.Item;
import io.deephaven.plugins.report.Item.Visitor;
import io.deephaven.plugins.report.Report;
import io.deephaven.plugins.report.Table;
import io.deephaven.plugins.report.Text;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;

class InlineHtmlRenderer implements Visitor {

  private static final String INLINE_CSS =
      Resources.toStringUnchecked(InlineHtmlRenderer.class, "inline.css");

  private final EmailSendingConfig config;
  private final StringBuilder html;
  private final java.util.List<Item<?>> context;
  private int depth;

  InlineHtmlRenderer(EmailSendingConfig reports) {
    this.config = Objects.requireNonNull(reports);
    this.html = new StringBuilder();
    this.context = new ArrayList<>();
    this.depth = 0;
  }

  void header() {
    sameLine("<html>");
    depth += 1;
    nextLine("<head>");
    depth += 1;
    nextLine("<style>");
    depth += 1;

    for (String cssPart : splitNewline(INLINE_CSS)) {
      nextLine(cssPart);
    }

    depth -= 1;
    nextLine("</style>");
    depth -= 1;
    nextLine("</head>");
    nextLine("<body>");
    depth += 1;
  }

  void tailer() {
    if (config.trailer().html().isPresent()) {
      nextLine("<div data-deephaven-type=\"trailer\">");
      sameLine(config.trailer().html().get());
      sameLine("</div>");
    }
    depth -= 1;
    nextLine("</body>");
    depth -= 1;
    nextLine("</html>");
  }

  private void addReport(Report report) {
    startReport(report);
    report.item().walk(this);
    endReport(report);
  }

  ImageHtmlEmail render() throws EmailException {
    createHtml();

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
    out.setHtmlMsg(html.toString());
    out.setTextMsg("Your email client does not support HTML messages");
    return out;
  }

  private void createHtml() {
    header();
    for (Report report : config.reports()) {
      addReport(report);
    }
    tailer();
  }

  // for testing
  String getHtml() {
    return html.toString();
  }

  @Override
  public void visit(Figure figure) {
    startItem(figure);

    // todo: could be a bit nicer if figure presented us an inputstream, and we could use a
    // custom DataSourceResolver w/ the emailer.

    final File file;
    try {
      file =
          File.createTempFile(figure.name().orElse("figure") + "-", ".png", config.tmpDirectory());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    final String absolutePath = file.getAbsolutePath();

    final int timeoutSeconds = 10;
    if (figure.size().isPresent()) {
      final int width = figure.size().get().width();
      final int height = figure.size().get().height();
      figure.figure().save(absolutePath, width, height, true, timeoutSeconds);

      sameLine(
          String.format(
              "<img src=\"%s\" width=\"%d\" height=\"%d\" style=\"display: block;\" />",
              absolutePath, width, height));
    } else {
      figure.figure().save(absolutePath, true, timeoutSeconds);
      sameLine(String.format("<img src=\"%s\" style=\"display: block;\" />", absolutePath));
    }

    endItem(figure);
  }

  @Override
  public void visit(Group group) {
    startItem(group);
    nextLine("<ul>");
    for (Item<?> item : group.items()) {
      nextLine("<li>");
      item.walk(this);
      sameLine("</li>");
    }
    nextLine("</ul>");
    endItem(group);
  }

  @Override
  public void visit(Table table) {
    startItem(table);
    sameLine(TableToHtml.html(table.value()));
    endItem(table);
  }

  @Override
  public void visit(Text text) {
    startItem(text);
    sameLine(escape(text.value()));
    endItem(text);
  }

  private static String escape(String raw) {
    return StringEscapeUtils.escapeHtml(raw);
  }

  private void indent() {
    for (int i = 0; i < depth; ++i) {
      html.append(' ');
    }
  }

  private static List<String> splitNewline(String value) {
    return Arrays.asList(value.split(System.lineSeparator()));
  }

  private void nextLine(String value) {
    newline();
    indent();
    html.append(value);
  }

  private void sameLine(String value) {
    html.append(value);
  }

  private void newline() {
    html.append('\n');
  }

  private void startItem(Item<?> item) {
    final String type = GetDeephavenDataType.getType(item);
    nextLine(String.format("<div data-deephaven-type=\"%s\">", type));
    if (item.name().isPresent()) {
      int hNumber = Math.max(2, Math.min(6, depth - 1));
      nextLine(String.format("<h%d>%s</h%d>", hNumber, escape(item.name().get()), hNumber));
    }

    depth += 1;
    context.add(item);
  }

  private void endItem(Item<?> item) {
    if (item != context.remove(context.size() - 1)) {
      throw new IllegalStateException("Expected to pop the same context");
    }
    depth -= 1;
    sameLine("</div>");
  }

  private void startReport(Report report) {
    nextLine("<div data-deephaven-type=\"report\">");
    depth += 1;

    nextLine(String.format("<!-- generated at %s -->", escape(report.timestamp().toString())));
    nextLine(String.format("<!-- %s -->", escape(report.toString())));
    nextLine(String.format("<!-- %s -->", escape(report.toGroovyishDebug())));
    nextLine(String.format("<h1>%s</h1>", escape(report.title())));
  }

  private void endReport(Report report) {
    depth -= 1;
    sameLine("</div>");
  }

  private static class GetDeephavenDataType implements Visitor {

    static String getType(Item<?> item) {
      return item.walk(new GetDeephavenDataType()).getOut();
    }

    private String out;

    public String getOut() {
      return Objects.requireNonNull(out);
    }

    @Override
    public void visit(Table table) {
      out = "table";
    }

    @Override
    public void visit(Figure figure) {
      out = "figure";
    }

    @Override
    public void visit(Text text) {
      out = "text";
    }

    @Override
    public void visit(Group group) {
      out = "group";
    }
  }
}
