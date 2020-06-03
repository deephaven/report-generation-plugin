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

import com.fishlib.io.logger.Logger;
import com.illumon.iris.db.tables.live.LiveTableMonitor;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import io.deephaven.plugins.report.Report;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Collections;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

@Immutable(builder = true, copy = true)
public abstract class ClientImpl implements Client {

  public static ClientImpl of(Config config, MethodsClient client) {
    return ImmutableClientImpl.builder().config(config).client(client).build();
  }

  /** The lock type. */
  public enum LockType {
    /** Do not acquire a lock to render the email. */
    NONE {
      @Override
      void doVisit(SlackMessagePerItemRenderer renderer, Report report) {
        renderer.visit(report);
      }
    },

    /** Acquire a {@link LiveTableMonitor#sharedLock()} to render the slack report. */
    SHARED {
      @Override
      void doVisit(SlackMessagePerItemRenderer renderer, Report report) {
        LiveTableMonitor.DEFAULT.sharedLock().doLocked(() -> renderer.visit(report));
      }
    },

    /** Acquire an {@link LiveTableMonitor#exclusiveLock()} ()} to render the slack report. */
    EXCLUSIVE {
      @Override
      void doVisit(SlackMessagePerItemRenderer renderer, Report report) {
        LiveTableMonitor.DEFAULT.exclusiveLock().doLocked(() -> renderer.visit(report));
      }
    };

    abstract void doVisit(SlackMessagePerItemRenderer renderer, Report report);
  }

  public abstract Config config();

  public abstract MethodsClient client();

  /**
   * The lock type. Defaults to {@link LockType#SHARED}.
   *
   * @return the lock type
   */
  @Default
  public LockType lockType() {
    return LockType.SHARED;
  }

  /**
   * The timeout. Defaults to {@code Duration.ofSeconds(5)}.
   *
   * @return the timeout
   */
  @Default
  public Duration timeout() {
    return Duration.ofSeconds(5);
  }

  /**
   * Creates a copy of this config but with the new value for {@link #lockType()}.
   *
   * @param lockType the lock type
   * @return the new instance
   */
  public abstract ClientImpl withLockType(LockType lockType);

  /**
   * Creates a copy of this config but with the new value for {@link #timeout()}.
   *
   * @param timeout the timeout
   * @return the new instance
   */
  public abstract ClientImpl withTimeout(Duration timeout);

  @Override
  public void send(String message) {
    final Slack slack = Slack.getInstance();
    final ChatPostMessageRequest request =
        ChatPostMessageRequest.builder()
            .channel(config().channel())
            .text(message)
            .blocks(
                Collections.singletonList(
                    SectionBlock.builder()
                        .text(MarkdownTextObject.builder().text(message).build())
                        .build()))
            .build();
    final ChatPostMessageResponse response;
    try {
      response = slack.methods(config().token()).chatPostMessage(request);
    } catch (SlackApiException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void send(Report report) {
    final SlackMessagePerItemRenderer renderer =
        ImmutableSlackMessagePerItemRenderer.builder().config(config()).client(client()).build();
    final Report local = report.toLocal(Logger.NULL, timeout());
    lockType().doVisit(renderer, local);
  }
}
