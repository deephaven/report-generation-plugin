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
package io.deephaven.plugins.slack;

import com.slack.api.Slack;
import io.deephaven.plugins.report.Report;
import java.util.List;
import org.immutables.value.Value.Immutable;

@Immutable
public abstract class ReportMessage {

  public static class Builder extends ImmutableReportMessage.Builder {}

  public static Builder builder() {
    return new Builder();
  }

  public abstract Config config();

  public abstract List<Report> reports();

  public final void send() {
    final Slack slack = Slack.getInstance();
    final SlackMessagePerItemRenderer renderer =
        ImmutableSlackMessagePerItemRenderer.builder()
            .config(config())
            .client(slack.methods(config().token()))
            .build();
    for (Report report : reports()) {
      renderer.visit(report);
    }
  }
}
