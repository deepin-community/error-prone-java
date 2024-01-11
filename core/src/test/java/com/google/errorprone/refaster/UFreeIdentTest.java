/*
 * Copyright 2013 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refaster;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.SerializableTester;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link UFreeIdent}.
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@RunWith(JUnit4.class)
public class UFreeIdentTest extends AbstractUTreeTest {
  @Test
  public void inlinesExpression() {
    bind(new UFreeIdent.Key("foo"), parseExpression("\"abcdefg\".charAt(x + 1)"));
    assertInlines(
        parseExpression("\"abcdefg\".charAt(x + 1)").toString(), UFreeIdent.create("foo"));
  }

  @Test
  public void binds() {
    JCExpression expr = parseExpression("\"abcdefg\".charAt(x + 1)");
    UFreeIdent ident = UFreeIdent.create("foo");
    assertThat(ident.unify(expr, unifier)).isNotNull();
    assertThat(unifier.getBindings()).containsExactly(new UFreeIdent.Key("foo"), expr);
  }

  @Test
  public void equality() {
    new EqualsTester()
        .addEqualityGroup(UFreeIdent.create("foo"))
        .addEqualityGroup(UFreeIdent.create("bar"))
        .testEquals();
  }

  @Test
  public void serialization() {
    SerializableTester.reserializeAndAssert(UFreeIdent.create("foo"));
  }
}
