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

/**
 * The base implementation.
 *
 * @param <Self> the item's self-referential type
 */
public abstract class ItemBase<Self extends ItemBase<Self>> extends AttributesBase<Self>
    implements Item<Self> {

  @Override
  public final Attribute<String, Self> name() {
    return attribute("name", String.class);
  }

  @Override
  public final Self withName(String name) {
    return name().with(name);
  }
}
