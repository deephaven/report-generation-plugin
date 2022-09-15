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
package io.deephaven.plugins.html;

import com.fishlib.io.logger.Logger;
import io.deephaven.plugins.email.EmailSendingConfig;
import io.deephaven.plugins.email.Resources;
import io.deephaven.plugins.report.Figure;
import io.deephaven.plugins.report.Group;
import io.deephaven.plugins.report.Item;
import io.deephaven.plugins.report.Item.Visitor;
import io.deephaven.plugins.report.Report;
import io.deephaven.plugins.report.SaveFigure;
import io.deephaven.plugins.report.Table;
import io.deephaven.plugins.report.TableLocal;
import io.deephaven.plugins.report.TablePQ;
import io.deephaven.plugins.report.Text;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringEscapeUtils;

public class InlineHtmlRenderer implements Visitor, Table.Visitor {

  private static final String INLINE_CSS =
      Resources.toStringUnchecked(InlineHtmlRenderer.class, "inline.css");

  private final List<Report> reports;
  private final Trailer trailer;
  private final StringBuilder html;
  private final java.util.List<Item<?>> context;
  private int depth;

  private final Logger logger;
  private final Duration timeout;

  public InlineHtmlRenderer(final List<Report> reports, final Trailer trailer) {
    this.reports = Objects.requireNonNull(reports);
    this.trailer = Objects.requireNonNull(trailer);
    this.html = new StringBuilder();
    this.context = new ArrayList<>();
    this.depth = 0;

    this.logger = Logger.NULL;
    this.timeout = Duration.ofSeconds(5); // todo
  }

  public static InlineHtmlRenderer from(final HTMLFile htmlFile) {
    Objects.requireNonNull(htmlFile);

    return new InlineHtmlRenderer(htmlFile.reports(), htmlFile.trailer());
  }

  public static InlineHtmlRenderer from(final EmailSendingConfig emailSendingConfig) {
    if (emailSendingConfig == null) {
      throw new IllegalArgumentException("emailSendingConfig must not be null");
    }

    return new InlineHtmlRenderer(emailSendingConfig.reports(), emailSendingConfig.trailer());
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
    if (trailer.html().isPresent()) {
      nextLine("<div data-deephaven-type=\"trailer\">");
      sameLine(trailer.html().get());
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

  public String render() {
    createHtml();
    return html.toString();
  }

  private void createHtml() {
    header();
    for (Report report : reports) {
      addReport(report);
    }
    tailer();
  }

  // for testing
  String getHtml() {
    return html.toString();
  }

  @Override
  public void visit(Figure<?> figure) {
    startItem(figure);

    // todo: could be a bit nicer if figure presented us an inputstream, and we could use a
    // custom DataSourceResolver w/ the emailer.

    final File file;
    try {
      file = File.createTempFile(figure.name().orElse("figure") + "-", ".png", tmpDirectory());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    figure.walk(SaveFigure.builder().file(file).log(logger).timeout(timeout).build());

    final String absolutePath = file.getAbsolutePath();

    if (figure.size().isPresent()) {
      final int width = figure.size().get().width();
      final int height = figure.size().get().height();
      sameLine(
          String.format(
              "<img src=\"%s\" width=\"%d\" height=\"%d\" style=\"display: block;\" />",
              absolutePath, width, height));
    } else {
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
  public void visit(Table<?> table) {
    table.walk((Table.Visitor) this);
  }

  @Override
  public void visit(TableLocal table) {
    startItem(table);
    sameLine(TableToHtml.html(table.value()));
    endItem(table);
  }

  @Override
  public void visit(TablePQ table) {
    startItem(table);
    final TableLocal local;
    try {
      local = table.toLocal(logger, timeout);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    sameLine(TableToHtml.html(local.value()));
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

  /**
   * The temporary directory where images and other data may be written to.
   *
   * <p>Defaults to the system's temporary directory.
   *
   * @return the temporary directory
   */
  public File tmpDirectory() {
    try {
      return Files.createTempDirectory("HTMLRenderer").toFile();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
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
    public void visit(Table<?> table) {
      out = "table";
    }

    @Override
    public void visit(Figure<?> figure) {
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
