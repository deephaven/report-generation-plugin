# Reports Plugin
The Reports Plugin ([io.deephaven.plugins.report.*](src/main/java/io/deephaven/plugins/report/))
enables users to create structured [Reports][Report].

A [Report] is composed of a title, a timestamp, and an [Item].

An [Item] is one of the following:
 * [Text] represents a string value
 * [Table] represents a Deephaven table
 * [Figure] represents a Deephaven plot
 * [Group] represents an ordered list of [Items][Item]

A [Group] provides the ability to structure reports hierarchically.

The basic report model does not (and should not) contain styling or layout information.
Additional information can be attached to any [Item] as named attributes, which may be useful for
styling or layout information.

### Language Ergonomics
The strongly-typed object model backing [Reports][Report] and [Items][Item] is potentially verbose
to construct. A series of [Functions] and implicit coercions are provided to aid in the construction
of these objects.

#### Type coercions
* A `CharSequence` will be coerced into a [Text].
* A `com.illumon.iris.db.tables.Table` will be coerced into a [TableLocal].
* A `com.illumon.iris.db.plot.Figure` will be coerced into a [FigureLocal].
* An `Iterable` or array will be coerced into a [Group], with each element coerced.

#### Common helper methods
* `report(String name, Object... items)`
* `named(String name, Object... items)`
* `figure(Object obj)`

The full list of helper methods can be found in [Functions].

### Examples

```groovy
import static io.deephaven.plugins.report.Functions.*

def date = currentDateNy()
def pnl_table = db.i("Devins", "PNL").where("Date=$date")
def pnl_plot = plot("PNL", pnl_table, "Timestamp", "PNL").show()
def pnl_report = report("Devin's PNL Report for $date", pnl_table.tail(1), figure(pnl_plot).withSize(800, 400))
```

```groovy
...
def section_1 = named("Weeklies", weeklies_plot, weeklies_summary_table)
def section_2 = named("Monthlies", monthlies_plot, monthlies_summary_table)
def section_3a = [quarterlies_plot_a, quarterlies_summary_table_a]
def section_3b = [quarterlies_plot_b, quarterlies_summary_table_b]
def section_3 = named("Quarterlies", section_3a, section_3b)
def expiration_report = report("By Expiration Report", section_1, section_2, section_3)
```

```groovy
...
def intro = "This is some introductory text..."
def historical = [[aapl_plot_historical, spy_plot_historical], "Some commentary on historical trends."]
def recent = [[aapl_plot_recent, spy_plot_recent], "Some commentary on recent trends."]
def conclusion = "Given the above, we should plan to do X, Y, and Z."
def aapl_v_spy_report = report("AAPL v SPY", intro, historical, recent, conclusion)
```

```groovy
...
def pq1 = figure(pq("devin", "My Query"), "my_plot")
def pq2 = figure(pq(31337L), "my_other_plot")
def my_report = report("From PQs", pq1, pq2)
```

## Email

The email section ([io.deephaven.plugins.email.*](src/main/java/io/deephaven/plugins/email/)) of the
Reports Plugin allows users to create and send HTML-based emails from [Reports][Report].

### Example server configuration

```groovy
def email = io.deephaven.plugins.email.Functions.nonStatic()

def gmail_server = email.gmail("user", "pass")

def localhost_server = email.localhost()

def custom_server = email.server()
  .hostName("the-hostname.example.com")
  .smtpPort(31337)
  .sslOnConnect(true)
  .auth(email.auth("user", "password"))
  .build()
```

### Example header configuration

```groovy
def email = io.deephaven.plugins.email.Functions.nonStatic()

def email_1 = email.header()
  .sender("somebody@example.com")
  .addRecipients("list1@example.com")
  .addRecipientsCC("admin@example.com")
  .subject("[REPORT] Daily report XYZ")
  .build()

def email_2 = email.header()
  .sender("operator@example.com")
  .addRecipients("devin@example.com")
  .subject("[ALERT] Disk space quota exceeded")
  .build()
```

### Example sending report

```groovy
import static io.deephaven.plugins.report.Functions.*

def email = io.deephaven.plugins.email.Functions.nonStatic()

// construct the report
def date = currentDateNy()
def pnl_table = db.i("Devins", "PNL").where("Date=$date")
def pnl_plot = plot("PNL", pnl_table, "Timestamp", "PNL").show()
def pnl_report = report("Devin's PNL Report for $date", pnl_table.tail(1), figure(pnl_plot).withSize(800, 400))

// configure the email
def localhost_server = email.localhost()
def email_header = email.header()
  .sender("devin@example.com")
  .addRecipients("devin@example.com")
  .subject("[REPORT] Daily PNL")
  .build()

// send the email
email.send(localhost_server, email_header, pnl_report)
```

### With explicit lock type

```groovy
def email = io.deephaven.plugins.email.Functions.nonStatic()
def report = io.deephaven.plugins.report.Functions.nonStatic()

email.email(
        email.localhost(),
        email.header().sender("example@example.com").addRecipients("todo@example.com").subject("the subject").build(),
        report.report("Simple report", "Simple text"))
        .withLockType(email.noLock())
```
## HTML

### Save to file

```groovy
import static io.deephaven.plugins.report.Functions.*

def date = currentDateNy()
def pnl_table = db.i("Devins", "PNL").where("Date=$date")
def pnl_plot = plot("PNL", pnl_table, "Timestamp", "PNL").show()
def pnl_report = report("Devin's PNL Report for $date", pnl_table.tail(1), figure(pnl_plot).withSize(800, 400))
html = io.deephaven.plugins.html.Functions.nonStatic()

html.save("/db/TempFiles/dbquery/db_query_server/report.html", pnl_report)
```

## Slack

### Standard (shared lock by default)

```groovy
def slack = io.deephaven.plugins.slack.Functions.nonStatic()
def client = slack.client(slack.config("<token>", "#the_channel"))
client.send("Simple string message")
client.send(my_report)
```

### With explicit lock type
```groovy
def slack = io.deephaven.plugins.slack.Functions.nonStatic()
def config = slack.config("<token>", "#the_channel")
def client_with_no_lock = slack.client(config).withLockType(slack.noLock())
def client_with_shared_lock = slack.client(config).withLockType(slack.sharedLock())
def client_with_exclusive_lock = slack.client(config).withLockType(slack.exclusiveLock())
```

# Development

### Documentation

  * `./gradlew javadoc`
  * Open `build/docs/javadoc/index.html`

### Test

`./gradlew check`

### Build

`./gradlew build`

[Report]: src/main/java/io/deephaven/plugins/report/Report.java
[Item]: src/main/java/io/deephaven/plugins/report/Item.java
[Text]: src/main/java/io/deephaven/plugins/report/Text.java
[Table]: src/main/java/io/deephaven/plugins/report/Table.java
[TableLocal]: src/main/java/io/deephaven/plugins/report/TableLocal.java
[TablePQ]: src/main/java/io/deephaven/plugins/report/TablePQ.java
[Figure]: src/main/java/io/deephaven/plugins/report/Figure.java
[FigureLocal]: src/main/java/io/deephaven/plugins/report/FigureLocal.java
[FigurePQ]: src/main/java/io/deephaven/plugins/report/FigurePQ.java
[Group]: src/main/java/io/deephaven/plugins/report/Group.java
[Functions]: src/main/java/io/deephaven/plugins/report/Functions.java
[Email]: src/main/java/io/deephaven/plugins/email/Email.java
