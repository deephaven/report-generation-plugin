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
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.files.FilesSharedPublicURLRequest;
import com.slack.api.methods.request.files.FilesUploadRequest;
import com.slack.api.methods.response.files.FilesSharedPublicURLResponse;
import com.slack.api.methods.response.files.FilesUploadResponse;
import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.ImageBlock;
import com.slack.api.model.block.ImageBlock.ImageBlockBuilder;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.PlainTextObject;
import io.deephaven.plugins.report.Figure;
import io.deephaven.plugins.report.Group;
import io.deephaven.plugins.report.Item;
import io.deephaven.plugins.report.Report;
import io.deephaven.plugins.report.SaveFigure;
import io.deephaven.plugins.report.Table;
import io.deephaven.plugins.report.Text;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// note: not in use ATM...
class SlackRenderer implements Item.Visitor {

  public static ChatPostMessageRequest build(ReportMessage message, Slack slack) {
    final SlackRenderer renderer = new SlackRenderer(message.config(), slack);
    for (Report report : message.reports()) {
      renderer.visit(report);
    }
    return renderer.asRequest();
  }

  private final Config config;
  private final Slack slack;
  private final List<LayoutBlock> out = new ArrayList<>();

  SlackRenderer(Config config, Slack slack) {
    this.config = Objects.requireNonNull(config);
    this.slack = Objects.requireNonNull(slack);
  }

  ChatPostMessageRequest asRequest() {
    return ChatPostMessageRequest.builder()
        .channel(config.channel())
        .text("Reports")
        .blocks(out)
        .build();
  }

  void visit(Report report) {
    if (!out.isEmpty()) {
      out.add(DividerBlock.builder().build());
    }
    out.add(
        SectionBlock.builder()
            .text(PlainTextObject.builder().text(report.title()).build())
            .build());
    report.item().walk(this);
  }

  @Override
  public void visit(Table<?> table) {
    addNameContextBlock(table);
    out.add(
        SectionBlock.builder()
            .text(PlainTextObject.builder().text("<TODO RENDER TABLE>").build())
            .build());
  }

  @Override
  public void visit(Figure<?> figure) {
    final com.slack.api.model.File file = uploadFigure(figure);
    final ImageBlockBuilder builder = ImageBlock.builder().imageUrl(getUrl(file));
    if (figure.name().isPresent()) {
      builder.title(PlainTextObject.builder().text(figure.name().get()).build());
      builder.altText(figure.name().get());
    } else {
      builder.altText("<figure>");
    }

    /* slack api doesn't recognize these...?
    if (figure.size().isPresent()) {
        builder.imageWidth(figure.size().get().width());
        builder.imageHeight(figure.size().get().height());
    }*/
    out.add(builder.build());
  }

  @Override
  public void visit(Text text) {
    addNameContextBlock(text);
    addContextString(text.value());
  }

  @Override
  public void visit(Group group) {
    addNameContextBlock(group);
    for (Item<?> item : group.items()) {
      item.walk(this);
    }
  }

  private static String getUrl(com.slack.api.model.File file) {
    // very hacky :s
    final String[] parts = file.getPermalinkPublic().split("-");
    final String pubSecret = parts[parts.length - 1];
    final String urlPrivateDownload = file.getUrlPrivateDownload();
    return urlPrivateDownload + "?pub_secret=" + pubSecret;
  }

  private void addNameContextBlock(Item<?> item) {
    if (item.name().isPresent()) {
      addContextString(item.name().get());
    }
  }

  private void addContextString(String value) {
    out.add(
        ContextBlock.builder()
            .elements(Collections.singletonList(PlainTextObject.builder().text(value).build()))
            .build());
  }

  private com.slack.api.model.File uploadFigure(Figure<?> figure) {
    final File file;
    try {
      file = File.createTempFile(figure.name().orElse("figure") + "-", ".png");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    figure.walk(SaveFigure.builder().file(file).build());

    final FilesUploadRequest request =
        FilesUploadRequest.builder()
            // .channels(Collections.singletonList(config.channel()))
            .filename(file.getName())
            .file(file)
            .build();

    final FilesUploadResponse response;
    try {
      response = slack.methods(config.token()).filesUpload(request);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SlackApiException e) {
      throw new RuntimeException(e);
    }

    final FilesSharedPublicURLResponse publicResponse;
    try {
      publicResponse =
          slack
              .methods(config.token())
              .filesSharedPublicURL(
                  FilesSharedPublicURLRequest.builder().file(response.getFile().getId()).build());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SlackApiException e) {
      throw new RuntimeException(e);
    }

    return publicResponse.getFile();
  }
}
