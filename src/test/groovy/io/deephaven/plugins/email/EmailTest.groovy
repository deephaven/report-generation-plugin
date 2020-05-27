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
package io.deephaven.plugins.email

import io.deephaven.plugins.email.Functions as email
import org.junit.jupiter.api.Test

class EmailTest {

	@Test
	void gmailCreate() {
		email.gmail("user", "pass")
	}

	@Test
	void localhostCreate() {
		email.localhost()
	}

	@Test
	void serverCreate() {
		email.server()
				.hostName("the-hostname.example.com")
				.smtpPort(31337)
				.sslOnConnect(true)
				.auth(email.auth("user", "password"))
				.build()
	}

	@Test
	void headerCreate() {
		email.header()
				.sender("somebody@example.com")
				.addRecipients("thelist@example.com")
				.addRecipientsCC("admin@example.com")
				.subject("You are the 1,000,000 visitor, open this email to win your prize!")
				.build()
	}
}
