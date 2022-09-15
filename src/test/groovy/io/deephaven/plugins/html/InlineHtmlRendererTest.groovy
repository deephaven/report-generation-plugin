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
package io.deephaven.plugins.html

import io.deephaven.plugins.report.Item
import io.deephaven.plugins.report.Report
import org.apache.commons.mail.EmailException
import org.junit.jupiter.api.Test

import java.time.Instant

import static io.deephaven.plugins.report.Functions.*
import static org.assertj.core.api.Assertions.assertThat

class InlineHtmlRendererTest {

	@Test
	void simpleText() {
		final String html = getHtml(report("The report", item("Simple text"), Instant.EPOCH))
		assertThat(html).isEqualTo("""<html>
 <head>
  <style>
   table {
     border-collapse: collapse;
     font-variant-numeric: tabular-nums;
     border-color: #c0bfbf;
     margin: 5px 0;
   }
   
   table tbody td,
   table thead th {
     padding: 2px;
   }
   
   table thead th {
     background-color: #3a658a;
     color: #fcfcfa;
   }
   
   table tbody tr:nth-child(even) {
     background-color: #f0f0ee;
   }
   
   h1,
   h2,
   h3,
   h4,
   h5,
   h6 {
     color: #3a658a;
     margin-top: 5px;
     margin-bottom: 0;
   }
   
   ul {
     padding-top: 5px;
     padding-bottom: 10px;
   }
  </style>
 </head>
 <body>
  <div data-deephaven-type="report">
   <!-- generated at 1970-01-01T00:00:00Z -->
   <!-- Report{title=The report, item=Text{attributes={}, value=Simple text}, timestamp=1970-01-01T00:00:00Z} -->
   <!-- report(&quot;The report&quot;, &quot;Simple text&quot;) -->
   <h1>The report</h1>
   <div data-deephaven-type="text">Simple text</div></div>
  <div data-deephaven-type="trailer"><p style="color: #929192;"><em>This message was generated by the <a href="https://deephaven.io" style="color: #3a658a;">Deephaven</a> Report Plugin.</em></p></div>
 </body>
</html>""")
	}

	@Test
	void simpleNamedText() throws EmailException {
		final String html =
				getHtml(report("The report", named("The name", "Simple text"), Instant.EPOCH))
		assertThat(html).isEqualTo("""<html>
 <head>
  <style>
   table {
     border-collapse: collapse;
     font-variant-numeric: tabular-nums;
     border-color: #c0bfbf;
     margin: 5px 0;
   }
   
   table tbody td,
   table thead th {
     padding: 2px;
   }
   
   table thead th {
     background-color: #3a658a;
     color: #fcfcfa;
   }
   
   table tbody tr:nth-child(even) {
     background-color: #f0f0ee;
   }
   
   h1,
   h2,
   h3,
   h4,
   h5,
   h6 {
     color: #3a658a;
     margin-top: 5px;
     margin-bottom: 0;
   }
   
   ul {
     padding-top: 5px;
     padding-bottom: 10px;
   }
  </style>
 </head>
 <body>
  <div data-deephaven-type="report">
   <!-- generated at 1970-01-01T00:00:00Z -->
   <!-- Report{title=The report, item=Text{attributes={name=The name}, value=Simple text}, timestamp=1970-01-01T00:00:00Z} -->
   <!-- report(&quot;The report&quot;, named(&quot;The name&quot;, &quot;Simple text&quot;)) -->
   <h1>The report</h1>
   <div data-deephaven-type="text">
   <h2>The name</h2>Simple text</div></div>
  <div data-deephaven-type="trailer"><p style="color: #929192;"><em>This message was generated by the <a href="https://deephaven.io" style="color: #3a658a;">Deephaven</a> Report Plugin.</em></p></div>
 </body>
</html>""")
	}

	@Test
	void titlesGetSmallerTheMoreNestedTheyAre() throws EmailException {

		final Item<?> a = named("a", "SimpleText")
		final Item<?> b = named("b", Collections.singleton(a))
		final Item<?> c = named("c", Collections.singleton(b))
		final Item<?> d = named("d", Collections.singleton(c))
		final Item<?> e = named("e", Collections.singleton(d))
		final Item<?> f = named("f", Collections.singleton(e))

		final String html = getHtml(report("The report", f, Instant.EPOCH))

		assertThat(html).isEqualTo("""<html>
 <head>
  <style>
   table {
     border-collapse: collapse;
     font-variant-numeric: tabular-nums;
     border-color: #c0bfbf;
     margin: 5px 0;
   }
   
   table tbody td,
   table thead th {
     padding: 2px;
   }
   
   table thead th {
     background-color: #3a658a;
     color: #fcfcfa;
   }
   
   table tbody tr:nth-child(even) {
     background-color: #f0f0ee;
   }
   
   h1,
   h2,
   h3,
   h4,
   h5,
   h6 {
     color: #3a658a;
     margin-top: 5px;
     margin-bottom: 0;
   }
   
   ul {
     padding-top: 5px;
     padding-bottom: 10px;
   }
  </style>
 </head>
 <body>
  <div data-deephaven-type="report">
   <!-- generated at 1970-01-01T00:00:00Z -->
   <!-- Report{title=The report, item=Group{attributes={name=f}, items=[Group{attributes={name=e}, items=[Group{attributes={name=d}, items=[Group{attributes={name=c}, items=[Group{attributes={name=b}, items=[Text{attributes={name=a}, value=SimpleText}]}]}]}]}]}, timestamp=1970-01-01T00:00:00Z} -->
   <!-- report(&quot;The report&quot;, named(&quot;f&quot;, [named(&quot;e&quot;, [named(&quot;d&quot;, [named(&quot;c&quot;, [named(&quot;b&quot;, [named(&quot;a&quot;, &quot;SimpleText&quot;)])])])])])) -->
   <h1>The report</h1>
   <div data-deephaven-type="group">
   <h2>f</h2>
    <ul>
    <li>
    <div data-deephaven-type="group">
    <h3>e</h3>
     <ul>
     <li>
     <div data-deephaven-type="group">
     <h4>d</h4>
      <ul>
      <li>
      <div data-deephaven-type="group">
      <h5>c</h5>
       <ul>
       <li>
       <div data-deephaven-type="group">
       <h6>b</h6>
        <ul>
        <li>
        <div data-deephaven-type="text">
        <h6>a</h6>SimpleText</div></li>
        </ul></div></li>
       </ul></div></li>
      </ul></div></li>
     </ul></div></li>
    </ul></div></div>
  <div data-deephaven-type="trailer"><p style="color: #929192;"><em>This message was generated by the <a href="https://deephaven.io" style="color: #3a658a;">Deephaven</a> Report Plugin.</em></p></div>
 </body>
</html>""")
	}

	@Test
	void noTrailer() throws EmailException {
		final String html = getHtml(Functions.noTrailer(), report("The report", item("Simple text"), Instant.EPOCH))
		assertThat(html).isEqualTo("""<html>
 <head>
  <style>
   table {
     border-collapse: collapse;
     font-variant-numeric: tabular-nums;
     border-color: #c0bfbf;
     margin: 5px 0;
   }
   
   table tbody td,
   table thead th {
     padding: 2px;
   }
   
   table thead th {
     background-color: #3a658a;
     color: #fcfcfa;
   }
   
   table tbody tr:nth-child(even) {
     background-color: #f0f0ee;
   }
   
   h1,
   h2,
   h3,
   h4,
   h5,
   h6 {
     color: #3a658a;
     margin-top: 5px;
     margin-bottom: 0;
   }
   
   ul {
     padding-top: 5px;
     padding-bottom: 10px;
   }
  </style>
 </head>
 <body>
  <div data-deephaven-type="report">
   <!-- generated at 1970-01-01T00:00:00Z -->
   <!-- Report{title=The report, item=Text{attributes={}, value=Simple text}, timestamp=1970-01-01T00:00:00Z} -->
   <!-- report(&quot;The report&quot;, &quot;Simple text&quot;) -->
   <h1>The report</h1>
   <div data-deephaven-type="text">Simple text</div></div>
 </body>
</html>""")
	}

	@Test
	void customTrailer() throws EmailException {
		final String html = getHtml(Functions.customTrailer("<h1>Custom Trailer</h1>"), report("The report", item("Simple text"), Instant.EPOCH))
		assertThat(html).isEqualTo("""<html>
 <head>
  <style>
   table {
     border-collapse: collapse;
     font-variant-numeric: tabular-nums;
     border-color: #c0bfbf;
     margin: 5px 0;
   }
   
   table tbody td,
   table thead th {
     padding: 2px;
   }
   
   table thead th {
     background-color: #3a658a;
     color: #fcfcfa;
   }
   
   table tbody tr:nth-child(even) {
     background-color: #f0f0ee;
   }
   
   h1,
   h2,
   h3,
   h4,
   h5,
   h6 {
     color: #3a658a;
     margin-top: 5px;
     margin-bottom: 0;
   }
   
   ul {
     padding-top: 5px;
     padding-bottom: 10px;
   }
  </style>
 </head>
 <body>
  <div data-deephaven-type="report">
   <!-- generated at 1970-01-01T00:00:00Z -->
   <!-- Report{title=The report, item=Text{attributes={}, value=Simple text}, timestamp=1970-01-01T00:00:00Z} -->
   <!-- report(&quot;The report&quot;, &quot;Simple text&quot;) -->
   <h1>The report</h1>
   <div data-deephaven-type="text">Simple text</div></div>
  <div data-deephaven-type="trailer"><h1>Custom Trailer</h1></div>
 </body>
</html>""")
	}

	@Test
	void escaping() {
		final String html = getHtml(report("<my_report>oops</my_report>", item("<my_text>oops</my_text>"), Instant.EPOCH))
		assertThat(html).isEqualTo("""<html>
 <head>
  <style>
   table {
     border-collapse: collapse;
     font-variant-numeric: tabular-nums;
     border-color: #c0bfbf;
     margin: 5px 0;
   }
   
   table tbody td,
   table thead th {
     padding: 2px;
   }
   
   table thead th {
     background-color: #3a658a;
     color: #fcfcfa;
   }
   
   table tbody tr:nth-child(even) {
     background-color: #f0f0ee;
   }
   
   h1,
   h2,
   h3,
   h4,
   h5,
   h6 {
     color: #3a658a;
     margin-top: 5px;
     margin-bottom: 0;
   }
   
   ul {
     padding-top: 5px;
     padding-bottom: 10px;
   }
  </style>
 </head>
 <body>
  <div data-deephaven-type="report">
   <!-- generated at 1970-01-01T00:00:00Z -->
   <!-- Report{title=&lt;my_report&gt;oops&lt;/my_report&gt;, item=Text{attributes={}, value=&lt;my_text&gt;oops&lt;/my_text&gt;}, timestamp=1970-01-01T00:00:00Z} -->
   <!-- report(&quot;&lt;my_report&gt;oops&lt;/my_report&gt;&quot;, &quot;&lt;my_text&gt;oops&lt;/my_text&gt;&quot;) -->
   <h1>&lt;my_report&gt;oops&lt;/my_report&gt;</h1>
   <div data-deephaven-type="text">&lt;my_text&gt;oops&lt;/my_text&gt;</div></div>
  <div data-deephaven-type="trailer"><p style="color: #929192;"><em>This message was generated by the <a href="https://deephaven.io" style="color: #3a658a;">Deephaven</a> Report Plugin.</em></p></div>
 </body>
</html>""")
	}

	private static String getHtml(Report report) throws EmailException {
		final HTMLFile config = getConfig(report)
		getHtml(config)
	}

	private static String getHtml(Trailer trailer, Report report) throws EmailException {
		final HTMLFile config = getConfig(trailer, report)
		getHtml(config)
	}

	private static HTMLFile getConfig(Report report) {
		return HTMLFile.builder()
				.addReports(report)
				.filePath("test.html")
				.build()
	}

	private static HTMLFile getConfig(Trailer trailer, Report report) {
		return HTMLFile.builder()
				.addReports(report)
				.filePath("test.html")
				.trailer(trailer)
				.build()
	}

	private static String getHtml(HTMLFile config) throws EmailException {
		final InlineHtmlFileRenderer renderer = new InlineHtmlFileRenderer(config)
		renderer.renderHtml()
	}
}
