/*
 * Copyright 2016 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** {@link InfiniteRecursion}Test */
@RunWith(JUnit4.class)
public class InfiniteRecursionTest {

  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(InfiniteRecursion.class, getClass());

  @Test
  public void positive() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  void f(int x) {}",
            "  void f() {",
            "    // BUG: Diagnostic contains:",
            "    f();",
            "  }",
            "  int g() {",
            "    return 0;",
            "  }",
            "  int g(int x) {",
            "    // BUG: Diagnostic contains:",
            "    return g(x);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void positiveMultipleStatements() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  Test f() {",
            "    // BUG: Diagnostic contains:",
            "    f();",
            "    return this;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void positiveStatic() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  static void f(int x) {}",
            "  static void f() {",
            "    // BUG: Diagnostic contains:",
            "    Test.f();",
            "  }",
            "  static void instanceF() {",
            "    // BUG: Diagnostic contains:",
            "    new Test().instanceF();",
            "  }",
            "  static void subclassF() {",
            "    // BUG: Diagnostic contains:",
            "    Subclass.subclassF();",
            "  }",
            "  static int g() {",
            "    return 0;",
            "  }",
            "  static int g(int x) {",
            "    // BUG: Diagnostic contains:",
            "    return Test.g(x);",
            "  }",
            "",
            "  class Subclass extends Test {}",
            "}")
        .doTest();
  }

  @Test
  public void negative() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  void f(int x) {}",
            "  void f() {",
            "    f(42);",
            "  }",
            "  int g() {",
            "    return 0;",
            "  }",
            "  int g(int x) {",
            "    return x == 0 ? g() : g(x - 1);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void negativeForNowMultipleStatements() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  Test f() {",
            "    new Test();",
            // TODO(b/264529494): Match this.
            "    f();",
            "    return this;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void negativeDelegate() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  Test test;",
            "  void f() {",
            "    test.f();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void positiveGeneric() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test<X> {",
            "  <T> void f(T x) {",
            "    // BUG: Diagnostic contains:",
            "    this.<String>f(\"\");",
            "  }",
            "  void g(X x) {",
            "    // BUG: Diagnostic contains:",
            "    g(null);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void positiveCast() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test<X> {",
            "  String overrideOfSomeMethodThatReturnsObject() {",
            "    // BUG: Diagnostic contains:",
            "    return (String) overrideOfSomeMethodThatReturnsObject();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void positiveCastWithParens() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test<X> {",
            "  String overrideOfSomeMethodThatReturnsObject() {",
            "    // BUG: Diagnostic contains:",
            "    return (String) (overrideOfSomeMethodThatReturnsObject());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void overload() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  public void f (String s) {",
            "    f((Object) s);",
            "  }",
            "  public void f (Object o) {",
            "    // BUG: Diagnostic contains:",
            "    f(o);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void abstractMethod() {
    compilationHelper
        .addSourceLines(
            "Test.java", //
            "abstract class Test {",
            "  abstract void f ();",
            "}")
        .doTest();
  }
}
