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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

// @@@ Specification to-do list @@@
// - Describe the difference between sequential and parallel streams
// - More general information about reduce, better definitions for associativity, more description of
//   how reduce employs parallelism, more examples
// - Role of stream flags in various operations, specifically ordering
//   - Whether each op preserves encounter order
// @@@ Specification to-do list @@@

/**
 * 要素の列であり、逐次的および並列的バルク処理を備える。
 * ストリームは{@code filter}や{@code map}といった遅延的な中間処理(ストリームを他のストリームに変換する)と、{@code forEach}や{@code findFirst}や{@codeiterator}といった末端処理(ストリームの内容を消費して結果や副作用を生じる)を備える。ストリームに対して処理が実行されると、ストリームは<em>消費された</em>とみなされ、他の処理には使えなくなる。
 *
 * <p>逐次的なストリームパイプラインの場合、パイプラインのデータ源に<a href="package-summary.html#Ordering">出現順順序</a>が定義されていれば、全ての処理はその出現順順序に従って実行される。
 *
 * <p>並列的なストリームパイプラインの場合、特に明記されていないかぎり、パイプラインのデータ源に<a href="package-summary.html#Ordering">出現順順序</a>が定義されていれば、中間ストリーム処理はデータ源の出現順順序を保存し、末端処理はデータ源の出現順順序を尊重する。ストリーム処理に対する引数が<a href="package-summary.html#NonInterference">非干渉性要求</a>を満たす場合、出現順順序が無い場合に起きる違いを除いて、同じデータ源に対して同じ処理を複数実行しても結果は変化しない。しかし、ストリームパイプラインの並列実行において、({@link #forEach(Consumer)}のような副作用を起こしてもよい処理から)副作用が生じるタイミングとスレッドは明示的に非決定的である。
 *
 * <p>特に明記していない限り、ストリームメソッドに対して{@code null}引数を与えると{@link NullPointerException}となる場合がある。
 *
 * @apiNote
 * ストリームはデータ構造でない。ストリームは要素のために保存領域を管理しないし、個々の要素へのアクセス手段を用意しない。しかし、{@link #iterator()}や{@link #spliterator()}を使えば制御された走査はできる。
 *
 * @param <T> 要素の型
 * @since 1.8
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface Stream<T> extends BaseStream<T, Stream<T>> {

    /**
     * このストリームの要素のうち、与えられた述語に適合する要素からなるストリームを返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param predicate 各要素に適用して、要素を含むべきかどうか決めるための<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return 新しいストリーム
     */
    Stream<T> filter(Predicate<? super T> predicate);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなるストリームを返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param <R> 新しいストリームの要素型
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    <R> Stream<R> map(Function<? super T, ? extends R> mapper);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなる{@code IntStream}を返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    IntStream mapToInt(ToIntFunction<? super T> mapper);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなる{@code LongStream}を返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    LongStream mapToLong(ToLongFunction<? super T> mapper);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなる{@code DoubleStream}を返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

    /**
     * 与えられた写像関数をこのストリームの各要素に適用して生成したストリームの内容で各要素を置き換えた結果からなるストリームを返す。もし写像関数の結果が{@code null}であれば、それは結果が空のストリームであるかのように扱われる。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @apiNote
     * {@code flatMap()}処理は1対多変換をストリームの各要素に適用し、結果の要素を新しいストリームに平坦化する効果を持つ。例えば{@code orders}が注文書のストリームであり、各注文書が勘定項目の集合からなる場合、以下のコードは勘定項目のストリームを生成する。
     * <pre>{@code
     *     orderStream.flatMap(order -> order.getLineItems().stream())...
     * }</pre>
     *
     * @param <R> 新しいストリームの要素型
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たず</a>新しい値のストリームを生成する関数
     * @return 新しいストリーム
     */
    <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    /**
     * 与えられた写像関数をこのストリームの各要素に適用して生成したストリームの内容で各要素を置き換えた結果からなる{@code IntStream}を返す。もし写像関数の結果が{@code null}であれば、それは結果が空のストリームであるかのように扱われる。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たず</a>新しい値のストリームを生成する関数
     * @return 新しいストリーム
     */
    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

    /**
     * 与えられた写像関数をこのストリームの各要素に適用して生成したストリームの内容で各要素を置き換えた結果からなる{@code LongStream}を返す。もし写像関数の結果が{@code null}であれば、それは結果が空のストリームであるかのように扱われる。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たず</a>新しい値のストリームを生成する関数
     * @return 新しいストリーム
     */
    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

    /**
     * 与えられた写像関数をこのストリームの各要素に適用して生成したストリームの内容で各要素を置き換えた結果からなる{@code DoubleStream}を返す。もし写像関数の結果が{@code null}であれば、それは結果が空のストリームであるかのように扱われる。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たず</a>新しい値のストリームを生成する関数
     * @return 新しいストリーム
     */
    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

    /**
     * このストリームの要素のうち({@link Object#equals(Object)}に従って)重複を除いた要素からなるストリームを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。
     *
     * @return 新しいストリーム
     */
    Stream<T> distinct();

    /**
     * このストリームの要素を、自然順にソートした結果からなるストリームを返す。もし要素が{@code Comparable}でなければ、ストリームパイプラインを実行した際に{@code java.lang.ClassCastException}が投げられる場合がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。 
     *
     * @return 新しいストリーム
     */
    Stream<T> sorted();

    /**
     * このストリームの要素を、与えられた{@code Comparator}に従ってソートした結果からなるストリームを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。 
     *
     * @param comparator ストリームの要素の比較に使われる<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>{@code Comparator}
     * @return 新しいストリーム
     */
    Stream<T> sorted(Comparator<? super T> comparator);

    /**
     * このストリームの要素からなり、加えて要素が消費されるごとにその要素にアクションを実行するストリームを返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * <p>並列パイプラインの場合、上流の処理によって要素が利用可能になる任意の時間とスレッドでアクションは呼ばれる。もしアクションが共有状態を変更するならば、アクションは必要な同期処理を用意する責任を負う。
     *
     * @apiNote このメソッドは主にデバッグの補助のために、パイプラインのある点を通過する要素を調べたいときのために存在する。
     * <pre>{@code
     *     list.stream()
     *         .filter(filteringFunction)
     *         .peek(e -> {System.out.println("Filtered value: " + e); });
     *         .map(mappingFunction)
     *         .peek(e -> {System.out.println("Mapped value: " + e); });
     *         .collect(Collectors.intoList());
     * }</pre>
     *
     * @param consumer このストリームから要素が消費される際にその要素に適用される、<a href="package-summary.html#NonInterference">非干渉的</a>なアクション
     * @return 新しいストリーム
     */
    Stream<T> peek(Consumer<? super T> consumer);

    /**
     * このストリームの要素からなり、長さが{@code maxSize}より長くならないように切り詰められたストリームを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的で状態を持つ中間処理</a>である。
     *
     * @param maxSize ストリームを制限する要素数
     * @return 新しいストリーム
     * @throws IllegalArgumentException {@code maxSize}が負の場合
     */
    Stream<T> limit(long maxSize);

    /**
     * このストリームの{@code startInclusive}個の要素を取り除いた残りの要素からなるストリームを返す。もし{@code startInclusive}がこのストリームの終わりの後にあるならば、空のストリームが返される。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。
     *
     * @param startInclusive スキップする先頭の要素数
     * @return 新しいストリーム
     * @throws IllegalArgumentException {@code startInclusive}が負の場合
     */
    Stream<T> substream(long startInclusive);

    /**
     * このストリームの{@code startInclusive}個の要素を取り除いた残りの要素からなり、{@code endExclusive - startInclusive}個より多くの要素を含まないように切り詰められたストリームを返す。もし{@code startInclusive}がこのストリームの終わりの後にあるならば、空のストリームが返される。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的で状態を持つ中間処理</a>である。
     *
     * @param startInclusive 子ストリームの開始位置。この位置を含む。
     * @param endExclusive 子ストリームの終了位置。この位置を含まない。
     * @return 新しいストリーム
     * @throws IllegalArgumentException {@code startInclusive}や{@code endExclusive}が負である場合や、{@code startInclusive}が{@code endExclusive}より大きい場合
     */
    Stream<T> substream(long startInclusive, long endExclusive);

    /**
     * このストリームの各要素にアクションを適用する。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * <p>並列ストリームパイプラインの場合、この処理はストリームの出現順順序を尊重する<em>とは限らない</em>。そのようにしてしまうと並列処理の利点を犠牲にしてしまうためである。与えられた要素に対して、アクションはライブラリが選んだ任意の時間とスレッドで実行される。もしアクションが共有状態を変更するならば、アクションは必要な同期処理を用意する責任を負う。
     *
     * @param action 各要素に適用される<a href="package-summary.html#NonInterference">非干渉的</a>なアクション
     */
    void forEach(Consumer<? super T> action);

    /**
     * このストリームの各要素にアクションを適用する。出現順順序を持つストリームに対しては、各要素は出現順順序で処理されると保証される。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param action 各要素に適用される<a href="package-summary.html#NonInterference">非干渉的</a>なアクション
     * @see #forEach(Consumer)
     */
    void forEachOrdered(Consumer<? super T> action);

    /**
     * このストリームの要素からなる配列を返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの要素からなる配列
     */
    Object[] toArray();

    /**
     * このストリームの要素からなる配列を返す。与えられた{@code generator}関数を使って返り値の配列を確保する。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param <A> 結果の配列の要素型
     * @param generator 与えられた長さの要求された型の配列を生成する関数。
     * @return このストリームの要素からなる配列
     * @throws ArrayStoreException 配列生成関数から返された配列の実行時型がこのストリームの全ての要素の部分型でない場合
     */
    <A> A[] toArray(IntFunction<A[]> generator);

    /**
     * 与えられた単位元と、<a href="package-summary.html#Associativity">結合的</a>な累積関数を使って、このストリームの要素に<a href="package-summary.html#Reduction">簡約</a>処理を実行して簡約された値を返す。これは次と等しい。
     * <pre>{@code
     *     T result = identity;
     *     for (T element : このストリーム)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * ただし逐次的に実行されるとは制約されていない。 
     *
     * <p>値{@code identity}は累積関数の単位元である必要がある。つまり、任意の{@code t}に対して{@code accumulator.apply(identity, t)}は{@code t}と等しい。{@code accumulator}関数は<a href="package-summary.html#Associativity">結合的</a>関数である必要がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @apiNote sum, min, max, average, 文字列連結は簡約の特別な場合である。数のストリームの合計は次のようにできる。
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * もしくはより簡潔に次のようにできる。
     *
     * <pre>{@code
     *     Integer sum = integers.reduce(0, Integer::sum);
     * }</pre>
     *
     * <p>単純に累計量をループで変更させていく方法と比べて、集計の方法としては回りくどいやりかたのように見えるが、簡約処理は余分な同期処理を必要とせずにうまく並列化でき、データ競合の危険を大幅に減らせる。
     *
     * @param identity 累積関数の単位元
     * @param accumulator 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 簡約の結果
     */
    T reduce(T identity, BinaryOperator<T> accumulator);

    /**
     * 与えられた<a href="package-summary.html#Associativity">結合的</a>な累積関数を使って、このストリームの要素に<a href="package-summary.html#Reduction">簡約</a>処理を実行して簡約された値があればそれを表す{@code Optional}を返す。これは次と等しい。
     * <pre>{@code
     *     boolean foundAny = false;
     *     T result = null;
     *     for (T element : このストリーム) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? Optional.of(result) : Optional.empty();
     * }</pre>
     *
     * ただし逐次的に実行されるとは制約されていない。 
     *
     * <p>{@code accumulator}関数は<a href="package-summary.html#Associativity">結合的</a>関数である必要がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param accumulator 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 簡約の結果
     * @see #reduce(Object, BinaryOperator)
     * @see #min(java.util.Comparator)
     * @see #max(java.util.Comparator)
     */
    Optional<T> reduce(BinaryOperator<T> accumulator);

    /**
     * 与えられた単位元と累積関数と統合関数を使って、このストリームの要素に<a href="package-summary.html#Reduction">簡約</a>処理を実行して簡約された値を返す。これは次と等しい。
     * <pre>{@code
     *     U result = identity;
     *     for (T element : このストリーム)
     *         result = accumulator.apply(result, element)
     *     return result;
     * }</pre>
     *
     * ただし逐次的に実行されるとは制約されていない。 
     *
     * <p>値{@code identity}は統合関数の単位元である必要がある。つまり、任意の{@code u}に対して{@code combiner.apply(identity, u)}は{@code u}と等しい。
     * 加えて、{@code combiner}関数は{@code accumulator}と整合する必要がある。つまり任意の{@code u}と{@code t}に対して次が成り立つ必要がある。
     * <pre>{@code
     *     combiner.apply(u, accumulator.apply(identity, t)) == accumulator.apply(u, t)
     * }</pre>
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @apiNote この形式を使った多くの簡約は{@code map}処理と{@code reduce}処理の明示的な組み合わせによって表現できる。{@code accumulator}関数は写像関数と累積関数を融合した関数として振る舞うが、以前に簡約された値がわかれば計算を省ける場合などには、写像と簡約を分けた場合よりも効率的な場合がある。
     *
     * @param <U> 結果の型
     * @param identity 統合関数の単位元
     * @param accumulator 結果に追加の要素を組み入れるための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @param combiner 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数。累積関数と整合する必要がある。
     * @return 簡約の結果
     * @see #reduce(BinaryOperator)
     * @see #reduce(Object, BinaryOperator)
     */
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);

    /**
     * このストリームの要素に<a href="package-summary.html#MutableReduction">可変的簡約</a>を実行する。可変的簡約は、簡約した値が{@code ArrayList}などの可変な値を保持するものであり、結果を置き換えるのではなく結果の状態を変更して各要素を組み入れるような簡約である。これは次のコードと同じ結果を生成する。
     * <pre>{@code
     *     R result = resultFactory.get();
     *     for (T element : このストリーム)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>{@link #reduce(Object, BinaryOperator)}のように、{@code collect}処理は追加の同期処理を必要とせずに並列化できる。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @apiNote JDKには{@code collect()}の引数としての利用によく適合したシグネチャを持つ既存のクラスが多くある。例えば、次のコードはArrayListに文字列を累積する。
     * <pre>{@code
     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
     * }</pre>
     *
     * <p>次のコードは文字列のストリームを取り、1つの文字列に連結する。
     * <pre>{@code
     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
     *                                          StringBuilder::append)
     *                                 .toString();
     * }</pre>
     *
     * @param <R> 結果の型
     * @param resultFactory 新しい結果コンテナを作成する関数。並列実行の場合、この関数は複数回呼ばれる場合があり、その度に新しい値を返す必要がある。
     * @param accumulator 追加の要素を結果に組み入れるための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数。
     * @param combiner 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数。累積関数と整合する必要がある。
     * @return 簡約の結果
     */
    <R> R collect(Supplier<R> resultFactory,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * このストリームの要素に対して、簡約を表現する{@code Collector}を使って<a href="package-summary.html#MutableReduction">可変的簡約</a>を実行する。{@code Collector}は{@link #collect(Supplier, BiConsumer, BiConsumer)}の引数として使われる関数をカプセル化して簡約を表現し、収集戦略の再利用を可能にすると共に、複数レベルのグループ化や組分けなどといった風に収集処理の合成を可能にする。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * <p>並列に実行された場合、可変なデータ構造の隔離を維持するために複数の中間結果がインスタンス化され、満たされ、統合される場合がある。そのため({@code ArrayList}などの)スレッドセーフでないデータ構造に対して並列的に実行されたとしても、並列簡約のために追加の同期処理は必要ない。
     *
     * @apiNote
     * 以下のコードは文字列をArrayListに累積する。
     * <pre>{@code
     *     List<String> asList = stringStream.collect(Collectors.toList());
     * }</pre>
     *
     * <p>以下のコードは{@code Person}オブジェクトを都市によって分類する。
     * <pre>{@code
     *     Map<String, Collection<Person>> peopleByCity
     *         = personStream.collect(Collectors.groupBy(Person::getCity));
     * }</pre>
     *
     * <p>以下のコードは2つの{@code Collector}を共にカスケードして、{@code Person}オブジェクトを州と都市によって分類する。
     * <pre>{@code
     *     Map<String, Map<String, Collection<Person>>> peopleByStateAndCity
     *         = personStream.collect(Collectors.groupBy(Person::getState,
     *                                                   Collectors.groupBy(Person::getCity)));
     * }</pre>
     *
     * @param <R> 結果の型
     * @param collector 簡約を表現する{@code Collector}
     * @return 簡約の結果
     * @see #collect(Supplier, BiConsumer, BiConsumer)
     * @see Collectors
     */
    <R> R collect(Collector<? super T, R> collector);

    /**
     * このストリームの最小要素を{@code Comparator}に従って返す。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合である。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param comparator このストリームの要素を比較するための、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>{@code Comparator}
     * @return このストリームの最小要素を表す{@code Optional}、またはこのストリームが空ならば空の{@code Optional}
     */
    Optional<T> min(Comparator<? super T> comparator);

    /**
     * このストリームの最大要素を{@code Comparator}に従って返す。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合である。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param comparator このストリームの要素を比較するための、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>{@code Comparator}
     * @return このストリームの最大要素を表す{@code Optional}、またはこのストリームが空ならば空の{@code Optional}
     */
    Optional<T> max(Comparator<? super T> comparator);

    /**
     * このストリームの要素数を返す。これは<a href="package-summary.html#MutableReduction">簡約</a>の特殊な場合であり、次のコードと等しい。
     * <pre>{@code
     *     return mapToLong(e -> 1L).sum();
     * }</pre>
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの要素数
     */
    long count();

    /**
     * このストリームのある要素が与えられた述語に適合するか返す。結果を特定するのに必要でなければ全ての要素に対しては述語を評価しない。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @param predicate このストリームの要素に適用する<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return このストリームのある要素が与えられた述語に適合するならば{@code true}でそうでなければ{@code false}
     */
    boolean anyMatch(Predicate<? super T> predicate);

    /**
     * このストリームのすべての要素が与えられた述語に適合するか返す。結果を特定するのに必要でなければ全ての要素に対しては述語を評価しない。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @param predicate このストリームの要素に適用する<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return このストリームのすべての要素が与えられた述語に適合するならば{@code true}でそうでなければ{@code false}
     */
    boolean allMatch(Predicate<? super T> predicate);

    /**
     * このストリームのどの要素も与えられた述語に適合しないか返す。結果を特定するのに必要でなければ全ての要素に対しては述語を評価しない。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @param predicate このストリームの要素に適用する<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return このストリームのどの要素も与えられた述語に適合しないならば{@code true}でそうでなければ{@code false}
     */
    boolean noneMatch(Predicate<? super T> predicate);

    /**
     * このストリームの(出現順順序で)最初の要素を表す{@link Optional}、もしくはストリームが空であれば空の{@code Optional}を返す。このストリームが出現順順序を持たなければ任意の要素が返される場合がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @return このストリームの最初の要素を表す{@code Optional}、またはこのストリームが空ならば空の{@code Optional}
     * @throws NullPointerException 選択された要素がnullの場合
     */
    Optional<T> findFirst();

    /**
     * このストリームの任意の要素を表す{@link Optional}、もしくはストリームが空であれば空の{@code Optional}を返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * <p>この処理の動作は明示的に非決定的であり、どの要素を選んでもよい。これにより並列実行時の性能を最大化できる。その際のコストは同じ情報源に対する複数回の呼び出しが同じ値を返さないことである(もし出現順順序で最初の要素を望むならば、代わりに{@link #findFirst()}を用いよ)。
     *
     * @return このストリームのある要素を表す{@code Optional}、またはこのストリームが空ならば空の{@code Optional}
     * @throws NullPointerException 選択された要素がnullの場合
     * @see #findFirst()
     */
    Optional<T> findAny();

    // Static factories

    /**
     * {@code Stream}のビルダを返す。
     *
     * @param <T> 要素の型
     * @return ストリームのビルダ
     */
    public static<T> StreamBuilder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }

    /**
     * 空の逐次的{@code Stream}を返す。
     *
     * @param <T> ストリームの要素型
     * @return 空の逐次的ストリーム
     */
    public static<T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.<T>emptySpliterator());
    }

    /**
     * 1つの要素を含む逐次的な{@code Stream}を返す。
     *
     * @param t 1つの要素
     * @param <T> ストリームの要素型
     * @return 1つの要素を含む逐次的なストリーム
     */
    public static<T> Stream<T> of(T t) {
        return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t));
    }

    /**
     * 要素が指定された値であるような逐次的なストリームを返す。
     *
     * @param <T> ストリームの要素型
     * @param values 新しいストリームの要素
     * @return 新しいストリーム
     */
    @SafeVarargs
    public static<T> Stream<T> of(T... values) {
        return Arrays.stream(values);
    }

    /**
     * 初期要素{@code seed}に対して関数{@code f}の繰り返しの適用によって生成された無限{@code Stream}を返す。{@code seed}, {@code f(seed)}, {@code f(f(seed))}などからなるストームを生成する。
     *
     * <p>{@code Stream}の最初の要素(位置{@code 0})は{@code seed}によって与えられる。{@code n > 0}に対しては、その位置の要素は{@code f}を位置{@code n - 1}の要素に適用した結果である。
     *
     * @param <T> ストリームの要素型
     * @param seed 初期要素
     * @param f 新しい要素を生成するために以前の値に適用される関数
     * @return 新しい逐次的な{@code Stream}
     */
    public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        final Iterator<T> iterator = new Iterator<T>() {
            @SuppressWarnings("unchecked")
            T t = (T) Streams.NONE;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return t = (t == Streams.NONE) ? seed : f.apply(t);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE));
    }

    /**
     * 各要素が{@code Supplier}によって与えられる、逐次的な{@code Stream}を返す。定数のストリームや乱数のストリームなどを生成するのに向いている。
     *
     * @param <T> ストリームの要素型
     * @param s 要素の{@code Supplier}
     * @return 新しい逐次的な{@code Stream}
     */
    public static<T> Stream<T> generate(Supplier<T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                new Iterator<T>() {
                    @Override
                    public boolean hasNext() { return true; }

                    @Override
                    public T next() { return s.get(); }
                },
                Spliterator.ORDERED | Spliterator.IMMUTABLE));
    }
}
