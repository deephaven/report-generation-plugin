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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

class Resources {
  static String toString(Class<?> clazz, String resource) throws IOException {
    try (final InputStream in = clazz.getResourceAsStream("inline.css")) {
      if (in == null) {
        throw new NullPointerException(String.format("Unable to find resource '%s'.", resource));
      }
      final byte[] buffer = new byte[8192];
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      int len;
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      return out.toString(StandardCharsets.UTF_8.name());
    }
  }

  static String toStringUnchecked(Class<?> clazz, String resource) {
    try {
      return toString(clazz, resource);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
