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

import io.deephaven.reporting.com.slack.api.methods.MethodsClient;
import io.deephaven.reporting.com.slack.api.methods.SlackApiException;
import io.deephaven.reporting.com.slack.api.methods.request.chat.ChatPostMessageRequest;
import io.deephaven.reporting.com.slack.api.methods.request.chat.ChatPostMessageRequest.ChatPostMessageRequestBuilder;
import io.deephaven.reporting.com.slack.api.methods.request.files.FilesUploadV2Request;
import io.deephaven.reporting.com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.deephaven.reporting.com.slack.api.methods.response.files.FilesUploadV2Response;
import io.deephaven.reporting.com.slack.api.model.block.ContextBlock;
import io.deephaven.reporting.com.slack.api.model.block.LayoutBlock;
import io.deephaven.reporting.com.slack.api.model.block.SectionBlock;
import io.deephaven.reporting.com.slack.api.model.block.composition.MarkdownTextObject;
import io.deephaven.reporting.com.slack.api.model.block.composition.PlainTextObject;
import io.deephaven.plugins.report.Figure;
import io.deephaven.plugins.report.Group;
import io.deephaven.plugins.report.Item;
import io.deephaven.plugins.report.Report;
import io.deephaven.plugins.report.SaveFigure;
import io.deephaven.plugins.report.Table;
import io.deephaven.plugins.report.TableLocal;
import io.deephaven.plugins.report.TablePQ;
import io.deephaven.plugins.report.Text;
import org.immutables.value.Value.Immutable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Immutable(builder = true, copy = false)
abstract class SlackMessagePerItemRenderer implements Item.Visitor, Table.Visitor {

  public abstract Config config();

  public abstract MethodsClient client();

  void visit(Report report) {
    final ChatPostMessageRequest message =
        ChatPostMessageRequest.builder()
            .channel(config().channel())
            .text(report.title())
            .blocks(
                Collections.singletonList(
                    SectionBlock.builder()
                        .text(MarkdownTextObject.builder().text("*" + report.title() + "*").build())
                        .build()))
            .build();
    sendMessage(message);
    report.item().walk(this);
  }

  @Override
  public void visit(Table<?> table) {
    table.walk((Table.Visitor) this);
  }

  @Override
  public void visit(TableLocal table) {
    final ChatPostMessageRequestBuilder builder =
        ChatPostMessageRequest.builder()
            .channel(config().channel())
            .text("<todo-table-not-implemented>"); // fallback from blocks

    final List<LayoutBlock> blocks = new ArrayList<>();
    if (table.name().isPresent()) {
      blocks.add(
          ContextBlock.builder()
              .elements(
                  Collections.singletonList(
                      PlainTextObject.builder().text(table.name().get()).build()))
              .build());
    }

    blocks.add(
        SectionBlock.builder()
            .text(PlainTextObject.builder().text("<todo-table-not-implemented>").build())
            .build());

    final ChatPostMessageRequest request = builder.blocks(blocks).build();
    final ChatPostMessageResponse response = sendMessage(request);
  }

  @Override
  public void visit(TablePQ table) {
    throw new UnsupportedOperationException("todo");
  }

  @Override
  public void visit(Figure<?> figure) {
    uploadFigureInChannel(figure);
  }

  @Override
  public void visit(Text text) {
    final ChatPostMessageRequestBuilder builder =
        ChatPostMessageRequest.builder()
            .channel(config().channel())
            .text(text.value()); // fallback from blocks

    final List<LayoutBlock> blocks = new ArrayList<>();
    if (text.name().isPresent()) {
      blocks.add(
          ContextBlock.builder()
              .elements(
                  Collections.singletonList(
                      PlainTextObject.builder().text(text.name().get()).build()))
              .build());
    }

    if (text.markdown().isPresent()) {
      blocks.add(
          SectionBlock.builder()
              .text(MarkdownTextObject.builder().text(text.markdown().get().value()).build())
              .build());
    } else {
      blocks.add(
          SectionBlock.builder()
              .text(PlainTextObject.builder().text(text.value()).build())
              .build());
    }

    final ChatPostMessageRequest request = builder.blocks(blocks).build();

    final ChatPostMessageResponse response = sendMessage(request);
  }

  private ChatPostMessageResponse sendMessage(ChatPostMessageRequest request) {
    final ChatPostMessageResponse response;
    try {
      response = client().chatPostMessage(request);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SlackApiException e) {
      throw new RuntimeException(e);
    }
    return response;
  }

  @Override
  public void visit(Group group) {
    // todo: group name?
    for (Item<?> item : group.items()) {
      item.walk(this);
    }
  }

  private io.deephaven.reporting.com.slack.api.model.File uploadFigureInChannel(Figure<?> figure) {
    final File file;
    try {
      file = File.createTempFile(figure.name().orElse("figure") + "-", ".png");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    figure.walk(SaveFigure.builder().file(file).build());

    final FilesUploadV2Request request =
        FilesUploadV2Request.builder()
            .title(figure.name().orElse(null))
            .channel(config().channel())
            .filename(file.getName())
            .file(file)
            .build();

    final FilesUploadV2Response response;
    try {
      response = client().filesUploadV2(request);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SlackApiException e) {
      throw new RuntimeException(e);
    }

    return response.getFile();
  }
}
