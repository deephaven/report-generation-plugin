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
import io.deephaven.plugins.slack.ClientImpl.LockType;

public class Functions {

  public static NonStatic nonStatic() {
    return NonStatic.INSTANCE;
  }

  public static Config config(String token, String channel) {
    return Config.of(token, channel);
  }

  public static ClientImpl client(Config config) {
    return ClientImpl.of(config, Slack.getInstance().methods(config.token()));
  }

  public static LockType noLock() {
    return LockType.NONE;
  }

  public static LockType sharedLock() {
    return LockType.SHARED;
  }

  public static LockType exclusiveLock() {
    return LockType.EXCLUSIVE;
  }

  private Functions() {}

  public enum NonStatic {
    INSTANCE;

    public Config config(String token, String channel) {
      return Functions.config(token, channel);
    }

    public ClientImpl client(Config config) {
      return Functions.client(config);
    }

    public LockType noLock() {
      return Functions.noLock();
    }

    public LockType sharedLock() {
      return Functions.sharedLock();
    }

    public LockType exclusiveLock() {
      return Functions.exclusiveLock();
    }
  }
}
