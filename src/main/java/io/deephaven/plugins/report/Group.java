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
package io.deephaven.plugins.report;

import org.immutables.value.Value.Immutable;

/** A group is an ordered collection of {@link #items() Items}. */
@Immutable
public abstract class Group extends ItemBase<Group> {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableGroup.Builder {}

  public abstract java.util.List<Item<?>> items();

  @Override
  public final Group withAttribute(String key, Object value) {
    return Group.builder().from(this).putAttributes(key, value).build();
  }

  @Override
  public final <V extends Visitor> V walk(V visitor) {
    visitor.visit(this);
    return visitor;
  }
}
