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

/** A reference to a persistent-query */
public interface PQ {

  /** The visitor-pattern visitor. */
  interface Visitor {
    void visit(PQName pq);

    void visit(PQSerialId pq);
  }

  /**
   * The visitor-pattern dispatcher.
   *
   * @param visitor the visitor
   * @param <V> the visitor type
   * @return the same visitor
   */
  <V extends Visitor> V walk(V visitor);
}
