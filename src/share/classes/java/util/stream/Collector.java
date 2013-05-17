/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.util.stream;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * 累積的な結果に値を畳み込む<a href="package-summary.html#Reduction">簡約処理</a>。
 * 結果は値であるか、可変的な結果コンテナである。可変的な結果コンテナに結果を累積する例としては次のようなものが挙げられる。結果を{@code Collection}に累積する、{@code StringBuilder}に文字列を連結する、要素について合計・最小値・最大値・平均値といった要約情報を計算する、「販売者ごとの最も大きな取り引き」といったような「ピボットテーブル」要約を計算する。簡約処理は逐次的にも並列的にも実行できる。
 *
 * <p>次のコードは予め定義された{@code Collector}の実装を{@code Stream} APIと使って可変的簡約作業を実行する例である。
 *
 * <pre>{@code
 *     // 要素をListに累積する
 *     List<String> list = stream.collect(Collectors.toList());
 *
 *     // 要素をTreeSetに累積する
 *     Set<String> list = stream.collect(Collectors.toCollection(TreeSet::new));
 *
 *     // 要素も文字列に変換し、カンマで区切って連結する
 *     String joined = stream.map(Object::toString)
 *                           .collect(Collectors.toStringJoiner(", "))
 *                           .toString();
 *
 *     // 最も給料が高い従業員を求める
 *     Employee highestPaid = employees.stream()
 *                                     .collect(Collectors.maxBy(Comparators.comparing(Employee::getSalary)));
 *
 *     // 部署ごとに従業員をグループ化する
 *     Map<Department, List<Employee>> byDept
 *         = employees.stream()
 *                    .collect(Collectors.groupingBy(Employee::getDepartment));
 *
 *     // 部署ごとに最も給料が高い従業員を求める
 *     Map<Department, Employee> highestPaidByDept
 *         = employees.stream()
 *                    .collect(Collectors.groupingBy(Employee::getDepartment,
 *                                                   Collectors.maxBy(Comparators.comparing(Employee::getSalary))));
 *
 *     // 学生を合格者と不合格者に分ける
 *     Map<Boolean, List<Student>> passingFailing =
 *         students.stream()
 *                 .collect(Collectors.partitioningBy(s -> s.getGrade() >= PASS_THRESHOLD);
 *
 * }</pre>
 *
 * <p>{@code Collector}は結果や結果コンテナを協調して扱う3つの関数によって記述される。つまり、初期結果の生成・新しいデータ要素の結果への取り込み・2つの結果の1つへの統合である。最後の関数(2つの結果の1つへの統合)は並列処理の際に使われる。入力の部分部分が並列に累積され、その後に各部分の結果が併合され統合された結果となる。結果は可変的なコンテナや値である。もし結果が可変的であれば、累積関数や統合関数は左の引数を変更(要素をコレクションに追加するなど)してそれを返してもよいし、変更を加えるべきでない場合は新しい値を返してもよい。
 *
 * <p>Collectorは{@link Characteristics#CONCURRENT}や{@link Characteristics#STRICTLY_MUTATIVE}といった特性の集合を備える。これらの特性は簡約の実装がより良い性能を出すためのヒントとして扱われる。
 *
 * <p>{@link Stream#collect(Collector)}のように、{@code Collector}に基いて簡約を実装するライブラリは次の制約を遵守する必要がある。
 * <ul>
 *     <li>累積関数に与えられる第1引数と、統合関数に与えられる両方の引数は{@link #resultSupplier()}や{@link #accumulator()}や{@link #combiner()}に対する以前の呼び出しの結果である必要がある。</li>
 *     <li>実装は、ファクトリ関数・累積関数・統合関数の結果に対して、累積関数や統合関数に再び渡すか簡約処理の呼び出し側へ返すか以外してはいけない。</li>
 *     <li>もし結果が累積関数か統合関数に渡され、同じオブジェクトがそれらの関数から返されなかった場合は、それを再び利用してはいけない。</li>
 *     <li>並行的でないコレクタに対しては、結果のファクトリ関数・累積関数・統合関数から返された結果は、逐次的にスレッドに閉じ込められている(serially thread-confined)必要がある。これにより{@code Collector}が余分な同期処理を実装しなくても収集を並列的に実行できるようになる。簡約の実装は、入力が重なりを持たずに分割され、分割処理は他の処理から分離して進行され、統合処理は累積処理が完了した後にのみ起きるようにやりくりする必要がある。</li>
 *     <li>並行的なコレクタに対しては、簡約の実装は簡約を並行的に実装してもよい(しかし必ずする必要はない)。並行的な簡約とは、累積中に結果を隔離するのではなく、並列的に変更可能な同じ結果コンテナを使って累積関数が複数のスレッドから並行的に呼ばれる簡約である。並行的簡約はコレクタが{@link Characteristics#UNORDERED}特性を持つか、元のデータが順序を持たないときにのみ適用されるべきである。</li>
 * </ul>
 *
 * @apiNote
 * {@code Collector}を使った簡約処理は次のコードと同じ結果を生成するべきである。
 * <pre>{@code
 *     BiFunction<R,T,R> accumulator = collector.accumulator();
 *     R result = collector.resultSupplier().get();
 *     for (T t : data)
 *         result = accumulator.apply(result, t);
 *     return result;
 * }</pre>
 *
 * <p>しかし、ライブラリは入力を分割し、部分部分に簡約を適用し、統合関数を使って部分的な結果を統合して、並列簡約を実現してもよい。 具体的な簡約処理によっては、これはより性能が良い場合もあれば悪い場合もある。これは累積関数と統合関数の相対的なコストに依存する。
 *
 * <p>{@code Collector}によって簡単にモデル化できる処理の例は、{@code TreeSet}に要素を累積する処理である。この場合、{@code resultSupplier()}関数は{@code () -> new Treeset<T>()}であり、{@code accumulator}関数は{@code (set, element) -> { set.add(element); return set; }}であり、統合関数は{@code (left, right) -> { left.addAll(right); return left; }}である(この挙動は{@code Collectors.toCollection(TreeSet::new)}によって実装されている)。
 *
 * TODO  結合性と可換性
 *
 * @see Stream#collect(Collector)
 * @see Collectors
 *
 * @param <T> 収集処理の入力要素の型
 * @param <R> 収集処理の結果型
 * @since 1.8
 */
public interface Collector<T, R> {
    /**
     * 「値が無い」状態を表す新しい結果を作成して返す関数。もし累積関数や統合関数がその引数を変更する場合、これは新しい空の結果コンテナである必要がある。
     *
     * @return 呼び出された際に「値が無い」状態を表す結果を返す関数。
     */
    Supplier<R> resultSupplier();

    /**
     * 累積結果に新しい値を畳み込む関数。結果は可変な結果コンテナでもよいし、値でもよい。累積関数は可変的なコンテナを変更してそれを返してもよいし、新しい結果を作成してそれを返してもよいが、新しい結果オブジェクトを返した場合はどの引数も変更してはならない。
     *
     * <p>もしコレクタが{@link Characteristics#STRICTLY_MUTATIVE}特性を持つ場合、累積関数は第1引数の状態を変更(またはそのままに)した後に、第1引数を<em>必ず返す必要がある</em>。
     *
     * @return 累積結果に新しい値を畳み込む関数
     */
    BiFunction<R, T, R> accumulator();

    /**
     * 部分的な結果を2つ取ってそれらを併合する関数。統合関数は片方の引数をもう片方に畳み込んでそれを返してもよいし、新しい結果オブジェクトを返してもよいが、新しい結果オブジェクトを返す場合は、どちらの引数の状態も変更してはならない。
     * 
     * <p>もしコレクタが{@link Characteristics#STRICTLY_MUTATIVE}特性を持つ場合、統合関数は第1引数の状態を変更(またはそのままに)した後に、第1引数を<em>必ず返す必要がある</em>。
     *
     * @return 2つの部分的な結果を累積結果に統合する関数
     */
    BinaryOperator<R> combiner();

    /**
     * このCollectorの特性を表す{@code Collector.Characteristics}の{@code Set}を返す。この集合は不変であるべきである。
     *
     * @return コレクタの特性の不変な集合
     */
    Set<Characteristics> characteristics();

    /**
     * 簡約処理の実装の最適化に利用できる、{@code Collector}の性質を表す特性。
     */
    enum Characteristics {
        /**
         * このコレクタが<em>並行的</em>であるという特性を表す。つまり結果コンテナは累積関数がその結果コンテナに対して複数のスレッドから並行的に呼べるような仕様になっているという特性を表す。並行的なコレクタは{@code STRICTLY_MUTATIVE}特性を常に持つ必要がある。
         *
         * <p>もし{@code CONCURRENT}コレクタが{@code UNORDERED}でない場合、そのコレクタは順序を持たないデータ源に対してのみ並行的に評価されるべきである。
         */
        CONCURRENT,

        /**
         * {@link Set}のように、結果コンテナは本質的な順序を持たないという特性を表す。
         */
        UNORDERED,

        /**
         * このコレクタが厳密に結果コンテナに対する変更によって動作するという特性を示す。つまり、{@link #accumulator()}関数と{@link #combiner()}関数は、異なる結果コンテナを返すのではなく、常に第1引数の状態を変更してそれを返すという特性を表す。
         */
        STRICTLY_MUTATIVE
    }
}
