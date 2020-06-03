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
package io.deephaven.plugins.report;

import com.fishlib.io.logger.Logger;
import java.io.File;
import java.time.Duration;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable
public abstract class SaveFigure implements Figure.Visitor {

  public static class Builder extends ImmutableSaveFigure.Builder {}

  public static Builder builder() {
    return new Builder();
  }

  public abstract File file();

  @Default
  public Logger log() {
    return Logger.NULL;
  }

  @Default
  public Duration timeout() {
    return Duration.ofSeconds(10);
  }

  @Override
  public void visit(FigureLocal figure) {
    saveLocal(figure);
  }

  @Override
  public void visit(FigurePQ figure) {
    final FigureLocal local;
    try {
      local = figure.toLocal(log(), timeout());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    saveLocal(local);
  }

  private void saveLocal(FigureLocal figure) {
    if (figure.size().isPresent()) {
      figure
          .figure()
          .save(
              file().getAbsolutePath(),
              figure.size().get().width(),
              figure.size().get().height(),
              true,
              timeout().getSeconds());
    } else {
      figure.figure().save(file().getAbsolutePath(), true, timeout().getSeconds());
    }
  }
}
