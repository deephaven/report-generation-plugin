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
package io.deephaven.plugins.slack

import io.deephaven.plugins.slack.Functions as slack
import org.junit.jupiter.api.Test

class SlackTest {
	@Test
	void simpleClient() {
		slack.client(slack.config("faketoken", "#fakechannel"))
	}

	@Test
	void noLockClient() {
		slack.client(slack.config("faketoken", "#fakechannel")).withLockType(slack.noLock())
	}

	@Test
	void lockTypeExampleFromReadme() {
		def config = slack.config("<token>", "#the_channel")
		def client_with_no_lock = slack.client(config).withLockType(slack.noLock())
		def client_with_shared_lock = slack.client(config).withLockType(slack.sharedLock())
		def client_with_exclusive_lock = slack.client(config).withLockType(slack.exclusiveLock())
	}
}
