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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public abstract class AttributesBase<Self extends AttributesBase<Self>>
    implements Attributes<Self> {

  abstract Self self();

  abstract Map<String, Object> attributes();

  @Override
  public final <T> Attribute<T, Self> attribute(String key, Class<T> clazz) {
    return new AttributeImpl<>(key, clazz);
  }

  @Override
  public final Iterator<Attribute<?, Self>> iterator() {
    return new MyIterator();
  }

  class AttributeImpl<T> implements Attribute<T, Self> {

    private final String key;
    private final Class<T> clazz;

    AttributeImpl(String key, Class<T> clazz) {
      this.key = Objects.requireNonNull(key);
      this.clazz = Objects.requireNonNull(clazz);
    }

    @Override
    public final String key() {
      return key;
    }

    @Override
    public final Class<T> clazz() {
      return clazz;
    }

    @Override
    public final boolean isPresent() {
      return attributes().containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T get() {
      return (T) Objects.requireNonNull(attributes().get(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T orElse(T orElse) {
      final Object value = attributes().get(key);
      if (value != null) {
        return (T) value;
      }
      return orElse;
    }

    @Override
    public final Self with(T value) {
      return withAttribute(key, value);
    }
  }

  private class MyIterator implements Iterator<Attribute<?, Self>> {
    private final Iterator<Entry<String, Object>> it;

    MyIterator() {
      it = attributes().entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public Attribute<?, Self> next() {
      final Entry<String, Object> next = it.next();
      return attribute(next.getKey(), next.getValue().getClass());
    }
  }
}
