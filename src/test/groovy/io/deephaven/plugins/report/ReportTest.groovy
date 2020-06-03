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
package io.deephaven.plugins.report

import org.junit.jupiter.api.Test

import static io.deephaven.plugins.report.Functions.*
import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.mock

class ReportTest {
	private static com.illumon.iris.db.plot.Figure mockPlot() {
		mock(com.illumon.iris.db.plot.Figure.class)
	}

	private static com.illumon.iris.db.tables.Table mockTable() {
		mock(com.illumon.iris.db.tables.Table.class)
	}

	@Test
	void itemStringIsText() {
		assertThat(item("A plain text report")).isInstanceOf(Text.class)
	}

	@Test
	void multipleItemsIsList() {
		assertThat(item("A", "B", "C")).isInstanceOf(Group.class)
	}

	@Test
	void itemOfItemIsSame() {
		Text x = text("A")
		assertThat(item(x)).isSameAs(x)
	}

	@Test
	void example_1() {
		def date = "2020-04-01"
		def pnl_table = mockTable()
		def pnl_plot = mockPlot()

		def pnl_report = report("Devin's PNL Report for $date", pnl_table, figure(pnl_plot).withSize(800, 400))
		assertThat(pnl_report.toGroovyishDebug()).isEqualTo("""report("Devin's PNL Report for 2020-04-01", [<table>, figure(<plot>).withSize(800, 400)])""")
	}

	@Test
	void example_2() {
		def weeklies_plot = mockPlot()
		def monthlies_plot = mockPlot()
		def quarterlies_plot_a = mockPlot()
		def quarterlies_plot_b = mockPlot()

		def weeklies_summary_table = mockTable()
		def monthlies_summary_table = mockTable()
		def quarterlies_summary_table_a = mockTable()
		def quarterlies_summary_table_b = mockTable()

		def section_1 = named("Weeklies", weeklies_plot, weeklies_summary_table)
		def section_2 = named("Monthlies", monthlies_plot, monthlies_summary_table)
		def section_3a = [
			quarterlies_plot_a,
			quarterlies_summary_table_a
		]
		def section_3b = [
			quarterlies_plot_b,
			quarterlies_summary_table_b
		]
		def section_3 = named("Quarterlies", section_3a, section_3b)

		def expiration_report = report("By Expiration Report", section_1, section_2, section_3)
		assertThat(expiration_report.toGroovyishDebug()).isEqualTo("""report("By Expiration Report", [named("Weeklies", [<plot>, <table>]), named("Monthlies", [<plot>, <table>]), named("Quarterlies", [[<plot>, <table>], [<plot>, <table>]])])""")
	}

	@Test
	void example_3() {
		def aapl_plot_historical = mockPlot()
		def spy_plot_historical = mockPlot()

		def aapl_plot_recent = mockPlot()
		def spy_plot_recent = mockPlot()

		def intro = "This is some introductory text..."
		def historical = [
			[
				aapl_plot_historical,
				spy_plot_historical
			],
			"Some commentary on historical trends."
		]
		def recent = [
			[
				aapl_plot_recent,
				spy_plot_recent
			],
			"Some commentary on recent trends."
		]
		def conclusion = "Given the above, we should plan to do X, Y, and Z."

		def aapl_v_spy_report = report("AAPL v SPY", intro, historical, recent, conclusion)
		assertThat(aapl_v_spy_report.toGroovyishDebug()).isEqualTo("""report("AAPL v SPY", ["This is some introductory text...", [[<plot>, <plot>], "Some commentary on historical trends."], [[<plot>, <plot>], "Some commentary on recent trends."], "Given the above, we should plan to do X, Y, and Z."])""")
	}

	@Test
	void example_4() {
		def pq1 = figure(pq("devin", "My Query"), "my_plot")
		def pq2 = figure(pq(31337L), "my_other_plot")
		def my_report = report("From PQs", pq1, pq2)
		assertThat(my_report.toGroovyishDebug()).isEqualTo("""report("From PQs", [figure(pq("devin", "My Query"), "my_plot"), figure(pq(31337L), "my_other_plot")])""");
	}
}
