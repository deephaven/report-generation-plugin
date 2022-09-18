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

import io.deephaven.plugins.report.Figure;
import io.deephaven.plugins.report.Report;
import java.io.File;
import java.util.List;
import java.util.Objects;

class InlineHtmlFileRenderer extends InlineHtmlRenderer {

  private final HTMLFile htmlFile;
  private final File saveDirectory;
  private int count = 0;

  InlineHtmlFileRenderer(final HTMLFile htmlFile) {
    this.htmlFile = Objects.requireNonNull(htmlFile);
    this.saveDirectory = new File(htmlFile.filePath()).getParentFile();
  }

  @Override
  protected Trailer trailer() {
    return htmlFile.trailer();
  }

  @Override
  protected List<Report> reports() {
    return htmlFile.reports();
  }

  @Override
  protected File createFigureFile(Figure<?> figure) {
    final File figureFile =
        new File(saveDirectory, figure.name().orElse("figure") + "-" + count + ".png");
    count++;
    return figureFile;
  }
}
