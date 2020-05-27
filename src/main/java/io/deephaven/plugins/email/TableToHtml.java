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

import com.illumon.iris.db.tables.Table;
import com.illumon.iris.db.tables.utils.DBDateTime;
import com.illumon.iris.db.tables.utils.DBTimeZone;
import com.illumon.iris.db.v2.sources.ColumnSource;
import com.illumon.iris.db.v2.utils.Index;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;

class TableToHtml {
  public static String html(Table source) {
    List<String> columnNames = source.getDefinition().getColumnNames();

    StringBuilder out = new StringBuilder();
    out.append("<table border=\"1\">\n");

    out.append("<thead>\n");
    out.append("<tr>\n");
    for (String column : columnNames) {
      out.append("<th>").append(column).append("</th>\n");
    }
    out.append("</tr>\n");
    out.append("</thead>\n");

    out.append("<tbody>\n");

    @SuppressWarnings("rawtypes")
    final Collection<? extends ColumnSource> columnSources = source.getColumnSources();
    for (final Index.Iterator ii = source.getIndex().iterator(); ii.hasNext(); ) {
      out.append("<tr>");
      final long key = ii.nextLong();
      for (ColumnSource<?> columnSource : columnSources) {
        out.append("<td>");
        final Object value = columnSource.get(key);
        if (value instanceof String) {
          out.append(StringEscapeUtils.escapeHtml((String) value));
        } else if (value instanceof DBDateTime) {
          final DBDateTime dbDateTime = (DBDateTime) value;
          out.append(StringEscapeUtils.escapeHtml(dbDateTime.toString(DBTimeZone.TZ_DEFAULT)));
        } else if (value != null) {
          out.append(StringEscapeUtils.escapeHtml(value.toString()));
        } else {
          // For now, don't output anything for null values.
        }
        out.append("</td>");
      }
      out.append("</tr>\n");
    }
    out.append("</tbody>\n");
    out.append("</table>\n");
    return out.toString();
  }
}
