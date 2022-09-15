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

import io.deephaven.plugins.email.EmailSendingConfig;
import io.deephaven.plugins.report.Report;

/** A collection of static helper functions to aid in the creation of {@link Report reports}. */
public class Functions {

  /**
   * Returns the {@link Functions.NonStatic} instance. Allows the caller to bring the scope into a
   * variable.
   *
   * <p>{@code def reports = io.deephaven.plugins.report.Functions.nonStatic()}
   *
   * @return the non-static instance
   */
  public static NonStatic nonStatic() {
    return NonStatic.INSTANCE;
  }

  /**
   * Creates an {@link HTMLFile} for the {@code report}, then saves it to {@code filePath}.
   *
   * @param filePath the full path to the file that will be written
   * @param report the report to save
   * @see HTMLFile#save(String)
   */
  public static void save(String filePath, Report report) throws Exception {
    HTMLFile.builder().addReports(report).build().save(filePath);
  }

  /**
   * Creates an {@link HTMLFile} an email for the {@code reports}, with the default {@link Trailer}.
   *
   * @param filePath the full path to the file that will be written
   * @param htmlFile the {@link HTMLFile} to save
   * @see HTMLFile#save(String)
   */
  public static void save(String filePath, HTMLFile htmlFile) throws Exception {
    htmlFile.save(filePath);
  }

  public static EmailSendingConfig.LockType noLock() {
    return EmailSendingConfig.LockType.NONE;
  }

  public static EmailSendingConfig.LockType sharedLock() {
    return EmailSendingConfig.LockType.SHARED;
  }

  public static EmailSendingConfig.LockType exclusiveLock() {
    return EmailSendingConfig.LockType.EXCLUSIVE;
  }

  /** A wrapper that presents the static functions of {@link Functions} as non-static methods. */
  public enum NonStatic {
    INSTANCE;

    public void save(String filePath, Report report) throws Exception {
      Functions.save(filePath, report);
    }

    public void save(String filePath, HTMLFile htmlFile) throws Exception {
      Functions.save(filePath, htmlFile);
    }

    public EmailSendingConfig.LockType noLock() {
      return Functions.noLock();
    }

    public EmailSendingConfig.LockType sharedLock() {
      return Functions.sharedLock();
    }

    public EmailSendingConfig.LockType exclusiveLock() {
      return Functions.exclusiveLock();
    }
  }
}
