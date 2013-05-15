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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Comparators;
import java.util.DoubleSummaryStatistics;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * 要素をコレクションに累積する、様々な条件によって要素を要約するなど、様々な便利な簡約処理を実装した{@link Collector}の実装。
 *
 * <p>次のコードは{@link Collectors}に予め用意された{@code Collector}の実装を{@code Stream} APIと共に利用して可変的簡約作業を実行する例である。
 *
 * <pre>{@code
 *     // 要素をListに累積する
 *     List<Person> list = people.collect(Collectors.toList());
 *
 *     // 要素をTreeSetに累積する
 *     List<Person> list = people.collect(Collectors.toCollection(TreeSet::new));
 *
 *     // 要素を文字列に変換し、カンマで区切って連結する
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
 * TODO 並列コレクションの説明
 *
 * @since 1.8
 */
public final class Collectors {

    private static final Set<Collector.Characteristics> CH_CONCURRENT
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
                                                     Collector.Characteristics.STRICTLY_MUTATIVE,
                                                     Collector.Characteristics.UNORDERED));
    private static final Set<Collector.Characteristics> CH_STRICT
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.STRICTLY_MUTATIVE));
    private static final Set<Collector.Characteristics> CH_STRICT_UNORDERED
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.STRICTLY_MUTATIVE,
                                                     Collector.Characteristics.UNORDERED));

    private Collectors() { }

    /**
     * {@link Map#merge(Object, Object, BiFunction) Map.merge()}や{@link #toMap(Function, Function, BinaryOperator) toMap()}での利用に適した、常に{@code IllegalStateException}を投げる併合関数を返す。これは収集される要素が全て異なるという想定を遵守させる。
     *
     * @param <T> 併合関数の入力引数の型
     * @return 常に{@code IllegalStateException}を投げる併合関数
     *
     * @see #firstWinsMerger()
     * @see #lastWinsMerger()
     */
    public static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    /**
     * {@link Map#merge(Object, Object, BiFunction) Map.merge()}や{@link #toMap(Function, Function, BinaryOperator) toMap()}での利用に適した、「先行者優先」方針を実装する併合関数を返す。
     *
     * @param <T> 併合関数の入力引数の型
     * @return 常に第1引数を返す併合関数
     * @see #lastWinsMerger()
     * @see #throwingMerger()
     */
    public static <T> BinaryOperator<T> firstWinsMerger() {
        return (u,v) -> u;
    }

    /**
     * {@link Map#merge(Object, Object, BiFunction) Map.merge()}や{@link #toMap(Function, Function, BinaryOperator) toMap()}での利用に適した、「後行者優先」方針を実装する併合関数を返す。
     *
     * @param <T> 併合関数の入力引数の型
     * @return 常に第2引数を返す併合関数
     * @see #firstWinsMerger()
     * @see #throwingMerger()
     */
    public static <T> BinaryOperator<T> lastWinsMerger() {
        return (u,v) -> v;
    }

    /**
     * Simple implementation class for {@code Collector}.
     *
     * @param <T> the type of elements to be collected
     * @param <R> the type of the result
     */
    private static final class CollectorImpl<T, R> implements Collector<T,R> {
        private final Supplier<R> resultSupplier;
        private final BiFunction<R, T, R> accumulator;
        private final BinaryOperator<R> combiner;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<R> resultSupplier,
                      BiFunction<R, T, R> accumulator,
                      BinaryOperator<R> combiner,
                      Set<Characteristics> characteristics) {
            this.resultSupplier = resultSupplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<R> resultSupplier,
                      BiFunction<R, T, R> accumulator,
                      BinaryOperator<R> combiner) {
            this(resultSupplier, accumulator, combiner, Collections.emptySet());
        }

        @Override
        public BiFunction<R, T, R> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<R> resultSupplier() {
            return resultSupplier;
        }

        @Override
        public BinaryOperator<R> combiner() {
            return combiner;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }

    /**
     * 入力要素を新しい{@code Collection}に出現順に累積する{@code Collector}を返す。{@code Collection}は与えられたファクトリによって作成される。
     *
     * @param <T> 入力要素の型
     * @param <C> 結果の{@code Collection}の型
     * @param collectionFactory 新しく適切な型で空の{@code Collection}を返す{@code Supplier}
     * @return 全ての入力要素を出現順に{@code Collection}に収集する{@code Collector}
     */
    public static <T, C extends Collection<T>>
    Collector<T, C> toCollection(Supplier<C> collectionFactory) {
        return new CollectorImpl<>(collectionFactory,
                                   (r, t) -> { r.add(t); return r; },
                                   (r1, r2) -> { r1.addAll(r2); return r1; },
                                   CH_STRICT);
    }

    /**
     * 入力要素を新しい{@code List}に出現順に累積する{@code Collector}を返す。返される{@code List}に対する、型・可変性・直列化可能性・スレッド安全性の保証は無い。
     *
     * @param <T> 入力要素の型
     * @return 全ての入力要素を出現順に{@code List}に収集する{@code Collector}
     */
    public static <T>
    Collector<T, List<T>> toList() {
        BiFunction<List<T>, T, List<T>> accumulator = (list, t) -> {
            switch (list.size()) {
                case 0:
                    return Collections.singletonList(t);
                case 1:
                    List<T> newList = new ArrayList<>();
                    newList.add(list.get(0));
                    newList.add(t);
                    return newList;
                default:
                    list.add(t);
                    return list;
            }
        };
        BinaryOperator<List<T>> combiner = (left, right) -> {
            switch (left.size()) {
                case 0:
                    return right;
                case 1:
                    List<T> newList = new ArrayList<>(left.size() + right.size());
                    newList.addAll(left);
                    newList.addAll(right);
                    return newList;
                default:
                    left.addAll(right);
                    return left;
            }
        };
        return new CollectorImpl<>(Collections::emptyList, accumulator, combiner);
    }

    /**
     * 入力要素を新しい{@code Set}に出現順に累積する{@code Collector}を返す。返される{@code Set}に対する、型・可変性・直列化可能性・スレッド安全性の保証は無い。
     *
     * @param <T> 入力要素の型
     * @return 全ての入力要素を出現順に{@code Set}に収集する{@code Collector}
     */
    public static <T>
    Collector<T, Set<T>> toSet() {
        return new CollectorImpl<>((Supplier<Set<T>>) HashSet::new,
                                   (r, t) -> { r.add(t); return r; },
                                   (r1, r2) -> { r1.addAll(r2); return r1; },
                                   CH_STRICT_UNORDERED);
    }

    /**
     * 入力要素を新しい{@link StringBuilder}に連結する{@code Collector}を返す。
     *
     * @return 入力要素を{@link StringBuilder}に出現順に収集する{@code Collector}
     */
    public static Collector<String, StringBuilder> toStringBuilder() {
        return new CollectorImpl<>(StringBuilder::new,
                                   (r, t) -> { r.append(t); return r; },
                                   (r1, r2) -> { r1.append(r2); return r1; },
                                   CH_STRICT);
    }

    /**
     * 入力要素を指定された区切り文字を使って新しい{@link StringJoiner}に連結する{@code Collector}を返す。
     *
     * @param delimiter 各要素の間に使われる区切り文字
     * @return 入力要素を{@link StringJoiner}に出現順に収集する{@code Collector}
     */
    public static Collector<CharSequence, StringJoiner> toStringJoiner(CharSequence delimiter) {
        BinaryOperator<StringJoiner> merger = (sj, other) -> {
            if (other.length() > 0)
                sj.add(other.toString());
            return sj;
        };
        return new CollectorImpl<>(() -> new StringJoiner(delimiter),
                                   (r, t) -> { r.add(t); return r; },
                                   merger, CH_STRICT);
    }

    /**
     * 与えられた併合関数を重複したキーの処理に使い、右引数の内容を左引数に併合する{@code BinaryOperator<Map>}
     *
     * @param <K> マップのキーの型
     * @param <V> マップの値の型
     * @param <M> マップの型
     * @param mergeFunction {@link Map#merge(Object, Object, BiFunction) Map.merge()}に適した併合関数
     * @return a merge function for two maps
     */
    private static <K, V, M extends Map<K,V>>
    BinaryOperator<M> mapMerger(BinaryOperator<V> mergeFunction) {
        return (m1, m2) -> {
            for (Map.Entry<K,V> e : m2.entrySet())
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            return m1;
        };
    }

    /**
     * 入力要素に対して累積する前に写像関数を適用して{@code Collector<U,R>}を{@code Collector<T,R>}に適合させる。
     *
     * @apiNote
     * {@code mapping()}コレクタはマルチレベル簡約、つまり{@code groupingBy}や{@code partitioningBy}の下流で利用する際に最も有益である。例えば{@code Person}のストリームが与えられた際に、各市の名字の集合を累積するには次のようにする。
     * <pre>{@code
     *     Map<City, Set<String>> lastNamesByCity
     *         = people.stream().collect(groupingBy(Person::getCity,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <U> 下流のコレクタによって受理される要素の型
     * @param <R> コレクタの型
     * @param mapper 入力関数に適用される関数
     * @param downstream 写像された値を受理するコレクタ
     * @return 入力要素に写像関数を適用して写像した結果を下流のコレクタに提供するコレクタ
     */
    public static <T, U, R> Collector<T, R>
    mapping(Function<? super T, ? extends U> mapper, Collector<? super U, R> downstream) {
        BiFunction<R, ? super U, R> downstreamAccumulator = downstream.accumulator();
        return new CollectorImpl<>(downstream.resultSupplier(),
                                   (r, t) -> downstreamAccumulator.apply(r, mapper.apply(t)),
                                   downstream.combiner(), downstream.characteristics());
    }

    /**
     * 入力要素数を数える{@code Collector<T, Long>}を返す。
     *
     * @implSpec
     * これは次のコードと等価な結果を生成する。
     * <pre>{@code
     *     reducing(0L, e -> 1L, Long::sum)
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @return 入力要素数を数える{@code Collector}
     */
    public static <T> Collector<T, Long>
    counting() {
        return reducing(0L, e -> 1L, Long::sum);
    }

    /**
     * 与えられた{@code Comparator}に従って最小の要素を返す{@code Collector<T, T>}を返す。
     *
     * @implSpec
     * これは次のコードと等価な結果を生成する。
     * This produces a result equivalent to:
     * <pre>{@code
     *     reducing(Comparators.lesserOf(comparator))
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param comparator 要素を比較する{@code Comparator}
     * @return 最小要素を返す{@code Collector}
     */
    public static <T> Collector<T, T>
    minBy(Comparator<? super T> comparator) {
        return reducing(Comparators.lesserOf(comparator));
    }

    /**
     * 与えられた{@code Comparator}に従って最大の要素を返す{@code Collector<T, T>}を返す。
     *
     * @implSpec
     * これは次のコードと等価な結果を生成する。
     * This produces a result equivalent to:
     * <pre>{@code
     *     reducing(Comparators.lesserOf(comparator))
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param comparator 要素を比較する{@code Comparator}
     * @return 最大要素を返す{@code Collector}
     */
    public static <T> Collector<T, T>
    maxBy(Comparator<? super T> comparator) {
        return reducing(Comparators.greaterOf(comparator));
    }

    /**
     * long値を返す関数を入力要素に適用した結果の和を返す{@code Collector<T, Long>}を返す。
     *
     * @implSpec
     * これは次のコードと等価な結果を生成する。
     * <pre>{@code
     *     reducing(0L, mapper, Long::sum)
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param mapper 和を計算するプロパティを抽出する関数
     * @return 得られたプロパティの和を計算する{@code Collector}
     */
    public static <T> Collector<T, Long>
    sumBy(Function<? super T, Long> mapper) {
        return reducing(0L, mapper, Long::sum);
    }

    /**
     * 指定された{@code BinaryOperator}に基いて入力要素の簡約を実行する{@code Collector<T,T>}を返す。
     *
     * @apiNote
     * {@code reducing()}コレクタはマルチレベル簡約、つまり{@code groupingBy}や{@code partitioningBy}の下流で利用する際に最も有益である。ストリームに対して単純な簡約を実行する場合は代わりに{@link Stream#reduce(BinaryOperator)}を使用せよ。
     *
     * @param <T> 簡約の入力と出力の要素型
     * @param identity 簡約の単位元(また、入力要素が無い場合に返される値)
     * @param op 入力要素を簡約するために使う{@code BinaryOperator<T>}
     * @return 簡約操作を実装する{@code Collector}
     *
     * @see #reducing(BinaryOperator)
     * @see #reducing(Object, Function, BinaryOperator)
     */
    public static <T> Collector<T, T>
    reducing(T identity, BinaryOperator<T> op) {
        return new CollectorImpl<>(() -> identity, (r, t) -> (r == null ? t : op.apply(r, t)), op);
    }

    /**
     * 指定された{@code BinaryOperator}に基いて入力要素の簡約を実行する{@code Collector<T,T>}を返す。
     *
     * @apiNote
     * {@code reducing()}コレクタはマルチレベル簡約、つまり{@code groupingBy}や{@code partitioningBy}の下流で利用する際に最も有益である。ストリームに対して単純な簡約を実行する場合は代わりに{@link Stream#reduce(BinaryOperator)}を使用せよ。
     *
     * <p>例えば、{@code Person}のストリームが与えられた場合、各市の最も背が高い人を計算するには次のようにする。
     * <pre>{@code
     *     Comparator<Person> byHeight = Comparators.comparing(Person::getHeight);
     *     BinaryOperator<Person> tallerOf = Comparators.greaterOf(byHeight);
     *     Map<City, Person> tallestByCity
     *         = people.stream().collect(groupingBy(Person::getCity, reducing(tallerOf)));
     * }</pre>
     *
     * @implSpec
     * デフォルトの実装は次と等価である。
     * <pre>{@code
     *     reducing(null, op);
     * }</pre>
     *
     * @param <T> 簡約の入力と出力の要素型
     * @param op 入力要素を簡約するために使う{@code BinaryOperator<T>}
     * @return 簡約操作を実装する{@code Collector}
     *
     * @see #reducing(Object, BinaryOperator)
     * @see #reducing(Object, Function, BinaryOperator)
     */
    public static <T> Collector<T, T>
    reducing(BinaryOperator<T> op) {
        return reducing(null, op);
    }

    /**
     * 指定された写像関数と{@code BinaryOperator}に基いて入力要素の簡約を実行する{@code Collector<T,U>}を返す。これは{@link #reducing(Object, BinaryOperator)}の一般化であり、簡約の前に要素の変換を許す。
     *
     * @apiNote
     * {@code reducing()}コレクタはマルチレベル簡約、つまり{@code groupingBy}や{@code partitioningBy}の下流で利用する際に最も有益である。ストリームに対して単純な簡約を実行する場合は代わりに{@link Stream#reduce(BinaryOperator)}を使用せよ。
     *
     * <p>例えば、{@code Person}のストリームが与えられた場合、各市の最も名字が長い住民を計算するには次のようにする。
     * <pre>{@code
     *     Comparator<String> byLength = Comparators.comparing(String::length);
     *     BinaryOperator<String> longerOf = Comparators.greaterOf(byLength);
     *     Map<City, String> longestLastNameByCity
     *         = people.stream().collect(groupingBy(Person::getCity,
     *                                              reducing(Person::getLastName, longerOf)));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <U> 写像された値の型
     * @param identity 簡約の単位元(また、入力要素が無い場合に返される値)
     * @param mapper 入力関数に適用される関数
     * @param op 写像された値を簡約するために使う{@code BinaryOperator<U>}
     * @return a {@code Collector} implementing the map-reduce operation
     *
     * @see #reducing(Object, BinaryOperator)
     * @see #reducing(BinaryOperator)
     */
    public static <T, U>
    Collector<T, U> reducing(U identity,
                             Function<? super T, ? extends U> mapper,
                             BinaryOperator<U> op) {
        return new CollectorImpl<>(() -> identity,
                                   (r, t) -> (r == null ? mapper.apply(t) : op.apply(r, mapper.apply(t))),
                                   op);
    }

    /**
     * 入力要素型{@code T}に対する"group by"処理を実装し、要素を分類関数に従ってグループ化する{@code Collector}を返す。
     *
     * <p>分類関数は要素を{@code K}型の何らかのキーに写像する。このコレクタは、入力要素に分類関数を適用した結果の値をキーとして持ち、そのキーに分類関数が写像するような入力要素を含む{@code List}を対応する値として持つような{@code Map<K, List<T>>}を生成する。
     *
     * <p>返される{@code Map}や{@code List}オブジェクトに対する、型・可変性・直列化可能性・スレッド安全性の保証は無い。
     * @implSpec
     * これは次のコードに類似した結果を生成する。
     * <pre>{@code
     *     groupingBy(classifier, toList());
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーの型
     * @param classifier 入力要素をキーに写像する分類関数
     * @return group-by処理を実装した{@code Collector}
     *
     * @see #groupingBy(Function, Collector)
     * @see #groupingBy(Function, Supplier, Collector)
     * @see #groupingByConcurrent(Function)
     */
    public static <T, K>
    Collector<T, Map<K, List<T>>> groupingBy(Function<? super T, ? extends K> classifier) {
        return groupingBy(classifier, HashMap::new, toList());
    }

    /**
     * 入力要素型{@code T}に対するカスケードされた"group by"処理を実装し、要素を分類関数に従ってグループ化した後に下流の{@code Collector}を使って与えられたキーに対応する値の簡約処理を実行する{@code Collector}を返す。
     *
     * <p>分類関数は要素を{@code K}型の何らかのキーに写像する。下流のコレクタは{@code T}型の要素に対して動作し、{@code D}型の結果を生成する。結果であるコレクタは{@code Map<K, D>}を生成する。
     *
     * <p>返される{@code Map}に対する、型・可変性・直列化可能性・スレッド安全性の保証は無い。
     *
     * <p>例えば、各市の人の名字の集合を計算するには次のようにする。
     * <pre>{@code
     *     Map<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingBy(Person::getCity,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーの型
     * @param <D> 下流の簡約の結果型
     * @param classifier 入力要素をキーに写像する分類関数
     * @param downstream 下流の簡約を実装した{@code Collector}
     * @return カスケードされたgroup-by処理を実装した{@code Collector}
     * @see #groupingBy(Function)
     *
     * @see #groupingBy(Function, Supplier, Collector)
     * @see #groupingByConcurrent(Function, Collector)
     */
    public static <T, K, D>
    Collector<T, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier,
                                       Collector<? super T, D> downstream) {
        return groupingBy(classifier, HashMap::new, downstream);
    }

    /**
     * 入力要素型{@code T}に対するカスケードされた"group by"処理を実装し、要素を分類関数に従ってグループ化した後に下流の{@code Collector}を使って与えられたキーに対応する値の簡約処理を実行する{@code Collector}を返す。このコレクタによって生成される{@code Map}は与えられたファクトリ関数によって生成される。
     *
     * <p>分類関数は要素を{@code K}型の何らかのキーに写像する。下流のコレクタは{@code T}型の要素に対して動作し、{@code D}型の結果を生成する。結果であるコレクタは{@code Map<K, D>}を生成する。
     * <p>例えば、各市の人の名字の集合を計算するには次のようにする。ただし結果のマップにおいて市の名前は整列されているとする。
     * <pre>{@code
     *     Map<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingBy(Person::getCity, TreeMap::new,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーの型
     * @param <D> 下流の簡約の結果型
     * @param <M> 結果の{@code Map}の型
     * @param classifier 入力要素をキーに写像する分類関数
     * @param downstream 下流の簡約を実装した{@code Collector}
     * @param mapFactory 呼ばれると要求された型の空の{@code Map}を生成する関数
     * @return カスケードされたgroup-by処理を実装した{@code Collector}
     *
     * @see #groupingBy(Function, Collector)
     * @see #groupingBy(Function)
     * @see #groupingByConcurrent(Function, Supplier, Collector)
     */
    public static <T, K, D, M extends Map<K, D>>
    Collector<T, M> groupingBy(Function<? super T, ? extends K> classifier,
                               Supplier<M> mapFactory,
                               Collector<? super T, D> downstream) {
        Supplier<D> downstreamSupplier = downstream.resultSupplier();
        BiFunction<D, ? super T, D> downstreamAccumulator = downstream.accumulator();
        BiFunction<M, T, M> accumulator = (m, t) -> {
            K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
            D oldContainer = m.computeIfAbsent(key, k -> downstreamSupplier.get());
            D newContainer = downstreamAccumulator.apply(oldContainer, t);
            if (newContainer != oldContainer)
                m.put(key, newContainer);
            return m;
        };
        return new CollectorImpl<>(mapFactory, accumulator, mapMerger(downstream.combiner()), CH_STRICT);
    }

    /**
     * 入力要素型{@code T}に対する並行"group by"処理を実装し、要素を分類関数に従ってグループ化する{@code Collector}を返す。
     *
     * <p>これは{@link Collector.Characteristics#CONCURRENT 並行的}であり、{@link Collector.Characteristics#UNORDERED 順序付けられていない}Collectorである。
     *
     * <p>分類関数は要素を{@code K}型の何らかのキーに写像する。このコレクタは、入力要素に分類関数を適用した結果の値をキーとして持ち、そのキーに分類関数が写像するような入力要素を含む{@code List}を対応する値として持つような{@code ConcurrentMap<K, List<T>>}を生成する。
     *
     * <p>返される{@code Map}や{@code List}オブジェクトに対する、型・可変性・直列化可能性、および返される{@code List}オブジェクトに対するスレッド安全性の保証は無い。
     * @implSpec
     * これは次のコードに類似した結果を生成する。
     * <pre>{@code
     *     groupingByConcurrent(classifier, toList());
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーの型
     * @param classifier 入力要素をキーに写像する分類関数
     * @return group-by処理を実装した{@code Collector}
     *
     * @see #groupingBy(Function)
     * @see #groupingByConcurrent(Function, Collector)
     * @see #groupingByConcurrent(Function, Supplier, Collector)
     */
    public static <T, K>
    Collector<T, ConcurrentMap<K, List<T>>> groupingByConcurrent(Function<? super T, ? extends K> classifier) {
        return groupingByConcurrent(classifier, ConcurrentHashMap::new, toList());
    }

    /**
     * 入力要素型{@code T}に対するカスケードされた並行"group by"処理を実装し、要素を分類関数に従ってグループ化した後に下流の{@code Collector}を使って与えられたキーに対応する値の簡約処理を実行する{@code Collector}を返す。
     *
     * <p>これは{@link Collector.Characteristics#CONCURRENT 並行的}であり、{@link Collector.Characteristics#UNORDERED 順序付けられていない}Collectorである。
     *
     * <p>分類関数は要素を{@code K}型の何らかのキーに写像する。下流のコレクタは{@code T}型の要素に対して動作し、{@code D}型の結果を生成する。結果であるコレクタは{@code Map<K, D>}を生成する。
     *
     * <p>例えば、各市の人の名字の集合を計算するには次のようにする。ただし結果のマップにおいて市の名前は整列されているとする。
     * <pre>{@code
     *     ConcurrentMap<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingByConcurrent(Person::getCity, TreeMap::new,
     *                                                        mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーの型
     * @param <D> 下流の簡約の結果型
     * @param classifier 入力要素をキーに写像する分類関数
     * @param downstream 下流の簡約を実装した{@code Collector}
     * @return カスケードされたgroup-by処理を実装した{@code Collector}
     *
     * @see #groupingBy(Function, Collector)
     * @see #groupingByConcurrent(Function)
     * @see #groupingByConcurrent(Function, Supplier, Collector)
     */
    public static <T, K, D>
    Collector<T, ConcurrentMap<K, D>> groupingByConcurrent(Function<? super T, ? extends K> classifier,
                                                           Collector<? super T, D> downstream) {
        return groupingByConcurrent(classifier, ConcurrentHashMap::new, downstream);
    }

    /**
     * 入力要素型{@code T}に対する並行"group by"処理を実装し、要素を分類関数に従ってグループ化した後に下流の{@code Collector}を使って与えられたキーに対応する値の簡約処理を実行する並行{@code Collector}を返す。このコレクタによって生成される{@code ConcurrentMap}は与えられたファクトリ関数によって生成される。
     *
     * <p>これは{@link Collector.Characteristics#CONCURRENT 並行的}であり、{@link Collector.Characteristics#UNORDERED 順序付けられていない}Collectorである。
     *
     * <p>分類関数は要素を{@code K}型の何らかのキーに写像する。下流のコレクタは{@code T}型の要素に対して動作し、{@code D}型の結果を生成する。結果であるコレクタは{@code Map<K, D>}を生成する。
     *
     * <p>例えば、各市の人の名字の集合を計算するには次のようにする。ただし結果のマップにおいて市の名前は整列されているとする。
     * <pre>{@code
     *     ConcurrentMap<City, Set<String>> namesByCity
     *         = people.stream().collect(groupingBy(Person::getCity, ConcurrentSkipListMap::new,
     *                                              mapping(Person::getLastName, toSet())));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーの型
     * @param <D> 下流の簡約の結果型
     * @param <M> the type of the resulting {@code ConcurrentMap}
     * @param classifier 入力要素をキーに写像する分類関数
     * @param downstream 下流の簡約を実装した{@code Collector}
     * @param mapFactory 呼ばれると要求された型の空の{@code ConcurrentMap}を生成する関数
     * @return カスケードされたgroup-by処理を実装した{@code Collector}
     *
     * @see #groupingByConcurrent(Function)
     * @see #groupingByConcurrent(Function, Collector)
     * @see #groupingBy(Function, Supplier, Collector)
     */
    public static <T, K, D, M extends ConcurrentMap<K, D>>
    Collector<T, M> groupingByConcurrent(Function<? super T, ? extends K> classifier,
                                         Supplier<M> mapFactory,
                                         Collector<? super T, D> downstream) {
        Supplier<D> downstreamSupplier = downstream.resultSupplier();
        BiFunction<D, ? super T, D> downstreamAccumulator = downstream.accumulator();
        BinaryOperator<M> combiner = mapMerger(downstream.combiner());
        if (downstream.characteristics().contains(Collector.Characteristics.CONCURRENT)) {
            BiFunction<M, T, M> accumulator = (m, t) -> {
                K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
                downstreamAccumulator.apply(m.computeIfAbsent(key, k -> downstreamSupplier.get()), t);
                return m;
            };
            return new CollectorImpl<>(mapFactory, accumulator, combiner, CH_CONCURRENT);
        } else if (downstream.characteristics().contains(Collector.Characteristics.STRICTLY_MUTATIVE)) {
            BiFunction<M, T, M> accumulator = (m, t) -> {
                K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
                D resultContainer = m.computeIfAbsent(key, k -> downstreamSupplier.get());
                synchronized (resultContainer) {
                    downstreamAccumulator.apply(resultContainer, t);
                }
                return m;
            };
            return new CollectorImpl<>(mapFactory, accumulator, combiner, CH_CONCURRENT);
        } else {
            BiFunction<M, T, M> accumulator = (m, t) -> {
                K key = Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key");
                do {
                    D oldResult = m.computeIfAbsent(key, k -> downstreamSupplier.get());
                    if (oldResult == null) {
                        if (m.putIfAbsent(key, downstreamAccumulator.apply(null, t)) == null)
                            return m;
                    } else {
                        synchronized (oldResult) {
                            if (m.get(key) != oldResult)
                                continue;
                            D newResult = downstreamAccumulator.apply(oldResult, t);
                            if (oldResult != newResult)
                                m.put(key, newResult);
                            return m;
                        }
                    }
                } while (true);
            };
            return new CollectorImpl<>(mapFactory, accumulator, combiner, CH_CONCURRENT);
        }
    }

    /**
     * 入力要素を{@code Predicate}に従って分割して{@code Map<Boolean, List<T>>}に編成する{@code Collector}を返す。
     *
     * 返される{@code Map}に対する、型・可変性・直列化可能性・スレッド安全性の保証は無い。
     *
     * @param <T> 入力要素の型
     * @param predicate 入力要素を分類する述語
     * @return 分割処理を実装した{@code Collector}
     *
     * @see #partitioningBy(Predicate, Collector)
     */
    public static <T>
    Collector<T, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate) {
        return partitioningBy(predicate, toList());
    }

    /**
     * 入力要素を{@code Predicate}に従って分割した後に他の{@code Collector}を使って各パーティションの値を簡約して{@code Map<Boolean, D>}に編成する{@code Collector}を返す。
     *
     * <p>返される{@code Map}に対する、型・可変性・直列化可能性・スレッド安全性の保証は無い。
     *
     * @param <T> 入力要素の型
     * @param <D> 下流の簡約の結果型
     * @param predicate 入力要素を分類する述語
     * @param downstream 下流の簡約を実装した{@code Collector}
     * @return カスケードされた分割処理をじっそうした{@code Collector}
     *
     * @see #partitioningBy(Predicate)
     */
    public static <T, D>
    Collector<T, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate,
                                                 Collector<? super T, D> downstream) {
        BiFunction<D, ? super T, D> downstreamAccumulator = downstream.accumulator();
        BiFunction<Map<Boolean, D>, T, Map<Boolean, D>> accumulator = (result, t) -> {
            Partition<D> asPartition = ((Partition<D>) result);
            if (predicate.test(t)) {
                D newResult = downstreamAccumulator.apply(asPartition.forTrue, t);
                if (newResult != asPartition.forTrue)
                    asPartition.forTrue = newResult;
            } else {
                D newResult = downstreamAccumulator.apply(asPartition.forFalse, t);
                if (newResult != asPartition.forFalse)
                    asPartition.forFalse = newResult;
            }
            return result;
        };
        return new CollectorImpl<>(() -> new Partition<>(downstream.resultSupplier().get(),
                                                         downstream.resultSupplier().get()),
                                   accumulator, partitionMerger(downstream.combiner()), CH_STRICT);
    }

    /**
     * Merge function for two partitions, given a merge function for the
     * elements.
     */
    private static <D> BinaryOperator<Map<Boolean, D>> partitionMerger(BinaryOperator<D> op) {
        return (m1, m2) -> {
            Partition<D> left = (Partition<D>) m1;
            Partition<D> right = (Partition<D>) m2;
            if (left.forFalse == null)
                left.forFalse = right.forFalse;
            else if (right.forFalse != null)
                left.forFalse = op.apply(left.forFalse, right.forFalse);
            if (left.forTrue == null)
                left.forTrue = right.forTrue;
            else if (right.forTrue != null)
                left.forTrue = op.apply(left.forTrue, right.forTrue);
            return left;
        };
    }

    /**
     * 写像関数の結果をキーと値として、要素を{@code Map}に累積する。もし写像されたキーが({@link Object#equals(Object)}に従って)重複を含む場合は、収集処理が実行される際に{@code IllegalStateException}が発生する。写像されたキーが重複を含む場合は代わりに{@link #toMap(Function, Function, BinaryOperator)}を使用せよ。
     *
     * @apiNote
     * キーまたは値が入力要素そのものである場合は多い。その場合、ユーティリティメソッド{@link java.util.function.Function#identity()}が役に立つだろう。例えば、次のコードは生徒を平均点に写像する{@code Map}を生成する。
     * <pre>{@code
     *     Map<Student, Double> studentToGPA
     *         students.stream().collect(toMap(Functions.identity(),
     *                                         student -> computeGPA(student)));
     * }</pre>
     * そして次のコードは一意的な識別子から生徒に写像する{@code Map}を生成する。
     * <pre>{@code
     *     Map<String, Student> studentIdToStudent
     *         students.stream().collect(toMap(Student::getId,
     *                                         Functions.identity());
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーに写像する関数の出力型
     * @param <U> 値に写像する関数の出力型
     * @param keyMapper キーを生成する写像関数
     * @param valueMapper 値を生成する写像関数
     * @return 写像関数の結果をキーと値として、入力要素を{@code Map}に累積する{@code Collector}
     *
     * @see #toMap(Function, Function, BinaryOperator)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     * @see #toConcurrentMap(Function, Function)
     */
    public static <T, K, U>
    Collector<T, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                 Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, throwingMerger(), HashMap::new);
    }

    /**
     * 写像関数の結果をキーと値として、要素を{@code Map}に累積する。もし写像されたキーが({@link Object#equals(Object)}に従って)重複を含む場合は、等価な各要素に対して値に写像する関数が適用され、用意された併合関数により結果が併合される。
     *
     * @apiNote
     * 同じキーに写像される要素間の衝突を処理する方法は複数ある。{@link #throwingMerger()}や{@link #firstWinsMerger()}や{@link #lastWinsMerger()}といった、よくある方針を実装した併合関数も予め用意されているし、カスタム方針の実装も簡単にできる。例えば、{@code Person}があり、名前から住所を結び付ける「電話帳」を作りたいとする。しかし別の人が同じ名前を持つこともあるため、次のように、上品にそういった衝突を処理し、名前から住所を連結したリストを結び付けた{@code Map}を生成できる。
     * <pre>{@code
     *     Map<String, String> phoneBook
     *         people.stream().collect(toMap(Person::getName,
     *                                       Person::getAddress,
     *                                       (s, a) -> s + ", " + a));
     * }</pre>
     *
     * @param <T> 入力要素の型
     * @param <K> キーに写像する関数の出力型
     * @param <U> 値に写像する関数の出力型
     * @param keyMapper キーを生成する写像関数
     * @param valueMapper 値を生成する写像関数
     * @param mergeFunction {@link Map#merge(Object, Object, BiFunction)}に与えられる関数と同様に、同じキーに割り当てられた値の衝突を解決する併合関数。
     * @return キーに写像する関数の結果をキーとし、値に写像する関数の値の結果を、キーが同じになる値全てを併合関数で併合した結果を値として、要素を{@code Map}に集める{@code Collector}
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     */
    public static <T, K, U>
    Collector<T, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                 Function<? super T, ? extends U> valueMapper,
                                 BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * 写像関数の結果をキーと値として、要素を{@code Map}に累積する。もし写像されたキーが({@link Object#equals(Object)}に従って)重複を含む場合は、等価な各要素に対して値に写像する関数が適用され、用意された併合関数により結果が併合される。{@code Map}は与えらえたファクトリ関数によって作成される。
     *
     * @param <T> 入力要素の型
     * @param <K> キーに写像する関数の出力型
     * @param <U> 値に写像する関数の出力型
     * @param <M> 結果の{@code Map}の型
     * @param keyMapper キーを生成する写像関数
     * @param valueMapper 値を生成する写像関数
     * @param mergeFunction {@link Map#merge(Object, Object, BiFunction)}に与えられる関数と同様に、同じキーに割り当てられた値の衝突を解決する併合関数。
     * @param mapSupplier 結果を挿入するための、新しい空の{@code Map}を返す関数
     * @return キーに写像する関数の結果をキーとし、値に写像する関数の値の結果を、キーが同じになる値全てを併合関数で併合した結果を値として、要素を{@code Map}に集める{@code Collector}
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U, M extends Map<K, U>>
    Collector<T, M> toMap(Function<? super T, ? extends K> keyMapper,
                          Function<? super T, ? extends U> valueMapper,
                          BinaryOperator<U> mergeFunction,
                          Supplier<M> mapSupplier) {
        BiFunction<M, T, M> accumulator
                = (map, element) -> {
                      map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
                      return map;
                  };
        return new CollectorImpl<>(mapSupplier, accumulator, mapMerger(mergeFunction), CH_STRICT);
    }

    /**
     * 写像関数の結果をキーと値として、要素を{@code ConcurrentMap}に累積する。もし写像されたキーが({@link Object#equals(Object)}に従って)重複を含む場合は、収集処理が実行される際に{@code IllegalStateException}が発生する。写像されたキーが重複を含む場合は代わりに{@link #toConcurrentMap(Function, Function, BinaryOperator)}を使用せよ。
     *
     * @apiNote
     * キーまたは値が入力要素そのものである場合は多い。その場合、ユーティリティメソッド{@link java.util.function.Function#identity()}が役に立つだろう。例えば、次のコードは生徒を平均点に写像する{@code Map}を生成する。
     * <pre>{@code
     *     Map<Student, Double> studentToGPA
     *         students.stream().collect(toMap(Functions.identity(),
     *                                         student -> computeGPA(student)));
     * }</pre>
     * そして次のコードは一意的な識別子から生徒に写像する{@code Map}を生成する。
     * <pre>{@code
     *     Map<String, Student> studentIdToStudent
     *         students.stream().collect(toConcurrentMap(Student::getId,
     *                                                   Functions.identity());
     * }</pre>
     *
     * <p>これは{@link Collector.Characteristics#CONCURRENT 並行的}で{@link Collector.Characteristics#UNORDERED 順序付けられていない}Collectorである。
     *
     * @param <T> 入力要素の型
     * @param <K> キーに写像する関数の出力型
     * @param <U> 値に写像する関数の出力型
     * @param keyMapper キーを生成する写像関数
     * @param valueMapper 値を生成する写像関数
     * @return キーに写像する関数の結果をキーとし、値に写像する関数の値の結果を値として、入力要素を{@code ConcurrentMap}に集める並行{@code Collector}
     *
     * @see #toMap(Function, Function)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U>
    Collector<T, ConcurrentMap<K,U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                                                     Function<? super T, ? extends U> valueMapper) {
        return toConcurrentMap(keyMapper, valueMapper, throwingMerger(), ConcurrentHashMap::new);
    }

    /**
     * 写像関数の結果をキーと値として、要素を{@code ConcurrentMap}に累積する。もし写像されたキーが({@link Object#equals(Object)}に従って)重複を含む場合は、等価な各要素に対して値に写像する関数が適用され、用意された併合関数により結果が併合される。
     *
     * @apiNote
     * 同じキーに写像される要素間の衝突を処理する方法は複数ある。{@link #throwingMerger()}や{@link #firstWinsMerger()}や{@link #lastWinsMerger()}といった、よくある方針を実装した併合関数も予め用意されているし、カスタム方針の実装も簡単にできる。例えば、{@code Person}があり、名前から住所を結び付ける「電話帳」を作りたいとする。しかし別の人が同じ名前を持つこともあるため、次のように、上品にそういった衝突を処理し、名前から住所を連結したリストを結び付けた{@code Map}を生成できる。
     * <pre>{@code
     *     Map<String, String> phoneBook
     *         people.stream().collect(toConcurrentMap(Person::getName,
     *                                                 Person::getAddress,
     *                                                 (s, a) -> s + ", " + a));
     * }</pre>
     *
     * <p>これは{@link Collector.Characteristics#CONCURRENT 並行的}で{@link Collector.Characteristics#UNORDERED 順序付けられていない}Collectorである。
     *
     * @param <T> 入力要素の型
     * @param <K> キーに写像する関数の出力型
     * @param <U> 値に写像する関数の出力型
     * @param keyMapper キーを生成する写像関数
     * @param valueMapper 値を生成する写像関数
     * @param mergeFunction {@link Map#merge(Object, Object, BiFunction)}に与えられる関数と同様に、同じキーに割り当てられた値の衝突を解決する併合関数。
     * @return キーに写像する関数の結果をキーとし、値に写像する関数の値の結果を、キーが同じになる値全てを併合関数で併合した結果を値として、要素を{@code ConcurrentMap}に集める並行{@code Collector}
     *
     * @see #toConcurrentMap(Function, Function)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     * @see #toMap(Function, Function, BinaryOperator)
     */
    public static <T, K, U>
    Collector<T, ConcurrentMap<K,U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                                                     Function<? super T, ? extends U> valueMapper,
                                                     BinaryOperator<U> mergeFunction) {
        return toConcurrentMap(keyMapper, valueMapper, mergeFunction, ConcurrentHashMap::new);
    }

    /**
     * 写像関数の結果をキーと値として、要素を{@code ConcurrentMap}に累積する。もし写像されたキーが({@link Object#equals(Object)}に従って)重複を含む場合は、等価な各要素に対して値に写像する関数が適用され、用意された併合関数により結果が併合される。{@code Map}は与えらえたファクトリ関数によって作成される。
     *
     * <p>これは{@link Collector.Characteristics#CONCURRENT 並行的}で{@link Collector.Characteristics#UNORDERED 順序付けられていない}Collectorである。
     *
     * @param <T> 入力要素の型
     * @param <K> キーに写像する関数の出力型
     * @param <U> 値に写像する関数の出力型
     * @param <M> the type of the resulting {@code ConcurrentMap}
     * @param keyMapper キーを生成する写像関数
     * @param valueMapper 値を生成する写像関数
     * @param mergeFunction {@link Map#merge(Object, Object, BiFunction)}に与えられる関数と同様に、同じキーに割り当てられた値の衝突を解決する併合関数。
     * @param mapSupplier 結果を挿入するための、新しい空の{@code Map}を返す関数
     * @return キーに写像する関数の結果をキーとし、値に写像する関数の値の結果を、キーが同じになる値全てを併合関数で併合した結果を値として、要素を{@code ConcurrentMap}に集める並行{@code Collector}
     *
     * @see #toConcurrentMap(Function, Function)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <T, K, U, M extends ConcurrentMap<K, U>>
    Collector<T, M> toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper,
                                    BinaryOperator<U> mergeFunction,
                                    Supplier<M> mapSupplier) {
        BiFunction<M, T, M> accumulator = (map, element) -> {
            map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
            return map;
        };
        return new CollectorImpl<>(mapSupplier, accumulator, mapMerger(mergeFunction), CH_CONCURRENT);
    }

    /**
     * 各入力要素に{@code int}を返す関数を適用し、結果の値に対する要約統計量を返す{@code Collector}を返す。
     *
     * @param <T> 入力要素の型
     * @param mapper 各要素に適用する写像関数
     * @return 要約統計簡約を実装する{@code Collector}
     *
     * @see #toDoubleSummaryStatistics(ToDoubleFunction)
     * @see #toLongSummaryStatistics(ToLongFunction)
     */
    public static <T>
    Collector<T, IntSummaryStatistics> toIntSummaryStatistics(ToIntFunction<? super T> mapper) {
        return new CollectorImpl<>(IntSummaryStatistics::new,
                                   (r, t) -> { r.accept(mapper.applyAsInt(t)); return r; },
                                   (l, r) -> { l.combine(r); return l; }, CH_STRICT);
    }

    /**
     * 各入力要素に{@code long}を返す関数を適用し、結果の値に対する要約統計量を返す{@code Collector}を返す。
     *
     * @param <T> 入力要素の型
     * @param mapper 各要素に適用する写像関数
     * @return 要約統計簡約を実装する{@code Collector}
     *
     * @see #toDoubleSummaryStatistics(ToDoubleFunction)
     * @see #toIntSummaryStatistics(ToIntFunction)
     */
    public static <T>
    Collector<T, LongSummaryStatistics> toLongSummaryStatistics(ToLongFunction<? super T> mapper) {
        return new CollectorImpl<>(LongSummaryStatistics::new,
                                   (r, t) -> { r.accept(mapper.applyAsLong(t)); return r; },
                                   (l, r) -> { l.combine(r); return l; }, CH_STRICT);
    }

    /**
     * 各入力要素に{@code double}を返す関数を適用し、結果の値に対する要約統計量を返す{@code Collector}を返す。
     *
     * @param <T> 入力要素の型
     * @param mapper 各要素に適用する写像関数
     * @return 要約統計簡約を実装する{@code Collector}
     *
     * @see #toLongSummaryStatistics(ToLongFunction)
     * @see #toIntSummaryStatistics(ToIntFunction)
     */
    public static <T>
    Collector<T, DoubleSummaryStatistics> toDoubleSummaryStatistics(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl<>(DoubleSummaryStatistics::new,
                                   (r, t) -> { r.accept(mapper.applyAsDouble(t)); return r; },
                                   (l, r) -> { l.combine(r); return l; }, CH_STRICT);
    }

    /**
     * Implementation class used by partitioningBy.
     */
    private static final class Partition<T>
            extends AbstractMap<Boolean, T>
            implements Map<Boolean, T> {
        T forTrue;
        T forFalse;

        Partition(T forTrue, T forFalse) {
            this.forTrue = forTrue;
            this.forFalse = forFalse;
        }

        @Override
        public Set<Map.Entry<Boolean, T>> entrySet() {
            return new AbstractSet<Map.Entry<Boolean, T>>() {
                @Override
                public Iterator<Map.Entry<Boolean, T>> iterator() {

                    return new Iterator<Map.Entry<Boolean, T>>() {
                        int state = 0;

                        @Override
                        public boolean hasNext() {
                            return state < 2;
                        }

                        @Override
                        public Map.Entry<Boolean, T> next() {
                            if (state >= 2)
                                throw new NoSuchElementException();
                            return (state++ == 0)
                                   ? new SimpleImmutableEntry<>(false, forFalse)
                                   : new SimpleImmutableEntry<>(true, forTrue);
                        }
                    };
                }

                @Override
                public int size() {
                    return 2;
                }
            };
        }
    }
}
