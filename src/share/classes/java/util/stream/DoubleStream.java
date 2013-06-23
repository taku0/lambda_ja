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
import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

/**
 * プリミティブのdoubleである要素の列であり逐次的および並列的バルク処理を備える。
 * ストリームは{@code filter}や{@code map}といった遅延的な中間処理(ストリームを他のストリームに変換する)と、{@code forEach}や{@code findFirst}や{@codeiterator}といった末端処理(ストリームの内容を消費して結果や副作用を生じる)を備える。ストリームに対して処理が実行されると、ストリームは<em>消費された</em>とみなされ、他の処理には使えなくなる。
 *
 * <p>逐次的なストリームパイプラインの場合、パイプラインのデータ源に<a href="package-summary.html#Ordering">出現順順序</a>が定義されていれば、全ての処理はその出現順順序に従って実行される。
 *
 * <p>並列的なストリームパイプラインの場合、特に明記されていないかぎり、パイプラインのデータ源に<a href="package-summary.html#Ordering">出現順順序</a>が定義されていれば、中間ストリーム処理はデータ源の出現順順序を保存し、末端処理はデータ源の出現順順序を尊重する。ストリーム処理に対する引数が<a href="package-summary.html#NonInterference">非干渉性要求</a>を満たす場合、出現順順序が無い場合に起きる違いを除いて、同じデータ源に対して同じ処理を複数実行しても結果は変化しない。しかし、ストリームパイプラインの並列実行において、({@link #forEach(DoubleConsumer)}のような副作用を起こしてもよい処理から)副作用が生じるタイミングとスレッドは明示的に非決定的である。
 *
 * <p>特に明記していない限り、ストリームメソッドに対して{@code null}引数を与えると{@link NullPointerException}となる場合がある。
 *
 * @apiNote
 * ストリームはデータ構造でない。ストリームは要素のために保存領域を管理しないし、個々の要素へのアクセス手段を用意しない。しかし、{@link #iterator()}や{@link #spliterator()}を使えば制御された走査はできる。
 *
 * @since 1.8
 * @see <a href="package-summary.html">java.util.stream</a>
 */
public interface DoubleStream extends BaseStream<Double, DoubleStream> {

    /**
     * このストリームの要素のうち、与えられた述語に適合する要素からなるストリームを返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param predicate 各要素に適用して、要素を含むべきかどうか決めるための<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return 新しいストリーム
     */
    DoubleStream filter(DoublePredicate predicate);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなるストリームを返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    DoubleStream map(DoubleUnaryOperator mapper);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなり、オブジェクトを値として持つ{@code Stream}を返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param <U> 新しいストリームの要素型 
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなる{@code IntStream}を返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    IntStream mapToInt(DoubleToIntFunction mapper);

    /**
     * このストリームの要素に与えられた関数を適用した結果からなる{@code LongStream}を返す。
     *
     * <p>これは <a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 新しいストリーム
     */
    LongStream mapToLong(DoubleToLongFunction mapper);

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
     * @param mapper 各要素に適用する、<a href="package-summary.html#NonInterference">非干渉的で状態を持たず</a>新しい値の{@code DoubleStream}を生成する関数
     * @return 新しいストリーム
     * @see Stream#flatMap(Function)
     */
    DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper);

    /**
     * このストリームの要素のうち重複を除いた要素からなるストリームを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。
     *
     * @return 新しいストリーム
     */
    DoubleStream distinct();

    /**
     * このストリームの要素を、ソートした順番で持つストリームを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。 
     *
     * @return 新しいストリーム
     */
    DoubleStream sorted();

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
    DoubleStream peek(DoubleConsumer consumer);

    /**
     * このストリームの要素からなり、長さが{@code maxSize}より長くならないように切り詰められたストリームを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的で状態を持つ中間処理</a>である。
     *
     * @param maxSize ストリームを制限する要素数
     * @return 新しいストリーム
     * @throws IllegalArgumentException {@code maxSize}が負の場合
     */
    DoubleStream limit(long maxSize);

    /**
     * このストリームの{@code startInclusive}個の要素を取り除いた残りの要素からなるストリームを返す。もし{@code startInclusive}がこのストリームの終わりの後にあるならば、空のストリームが返される。
     *
     * <p>これは<a href="package-summary.html#StreamOps">状態を持つ中間処理</a>である。
     *
     * @param startInclusive スキップする先頭の要素数
     * @return 新しいストリーム
     * @throws IllegalArgumentException {@code startInclusive}が負の場合
     */
    DoubleStream substream(long startInclusive);

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
    DoubleStream substream(long startInclusive, long endExclusive);

    /**
     * このストリームの各要素にアクションを適用する。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * <p>並列ストリームパイプラインの場合、この処理はストリームの出現順順序を尊重する<em>とは限らない</em>。そのようにしてしまうと並列処理の利点を犠牲にしてしまうためである。与えられた要素に対して、アクションはライブラリが選んだ任意の時間とスレッドで実行される。もしアクションが共有状態を変更するならば、アクションは必要な同期処理を用意する責任を負う。
     *
     * @param action 各要素に適用される<a href="package-summary.html#NonInterference">非干渉的</a>なアクション
     */
    void forEach(DoubleConsumer action);

    /**
     * このストリームの各要素にアクションを適用する。出現順順序を持つストリームに対しては、各要素は出現順順序で処理されると保証される。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param action 各要素に適用される<a href="package-summary.html#NonInterference">非干渉的</a>なアクション
     * @see #forEach(DoubleConsumer)
     */
    void forEachOrdered(DoubleConsumer action);

    /**
     * このストリームの要素からなる配列を返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの要素からなる配列
     */
    double[] toArray();

    /**
     * 与えられた単位元と、<a href="package-summary.html#Associativity">結合的</a>な累積関数を使って、このストリームの要素に<a href="package-summary.html#Reduction">簡約</a>処理を実行して簡約された値を返す。これは次と等しい。
     * <pre>{@code
     *     double result = identity;
     *     for (double element : このストリーム)
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
     *     double sum = numbers.reduce(0, (a, b) -> a+b);
     * }</pre>
     *
     * もしくはより簡潔に次のようにできる。
     *
     * <pre>{@code
     *     double sum = numbers.reduce(0, Double::sum);
     * }</pre>
     *
     * <p>単純に累計量をループで変更させていく方法と比べて、集計の方法としては回りくどいやりかたのように見えるが、簡約処理は余分な同期処理を必要とせずにうまく並列化でき、データ競合の危険を大幅に減らせる。
     *
     * @param identity 累積関数の単位元
     * @param op 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 簡約の結果
     * @see #sum()
     * @see #min()
     * @see #max()
     * @see #average()
     */
    double reduce(double identity, DoubleBinaryOperator op);

    /**
     * 与えられた<a href="package-summary.html#Associativity">結合的</a>な累積関数を使って、このストリームの要素に<a href="package-summary.html#Reduction">簡約</a>処理を実行して簡約された値があればそれを表す{@code OptionalDouble}を返す。これは次と等しい。
     * <pre>{@code
     *     boolean foundAny = false;
     *     double result = null;
     *     for (double element : このストリーム) {
     *         if (!foundAny) {
     *             foundAny = true;
     *             result = element;
     *         }
     *         else
     *             result = accumulator.apply(result, element);
     *     }
     *     return foundAny ? OptionalDouble.of(result) : OptionalDouble.empty();
     * }</pre>
     *
     * ただし逐次的に実行されるとは制約されていない。 
     *
     * <p>{@code accumulator}関数は<a href="package-summary.html#Associativity">結合的</a>関数である必要がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param op 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数
     * @return 簡約の結果
     * @see #reduce(double, DoubleBinaryOperator)
     */
    OptionalDouble reduce(DoubleBinaryOperator op);

    /**
     * このストリームの要素に<a href="package-summary.html#MutableReduction">可変的簡約</a>を実行する。可変的簡約は、簡約した値が{@code ArrayList}などの可変な値を保持するものであり、結果を置き換えるのではなく結果の状態を変更して各要素を組み入れるような簡約である。これは次のコードと同じ結果を生成する。
     * <pre>{@code
     *     R result = resultFactory.get();
     *     for (double element : このストリーム)
     *         accumulator.accept(result, element);
     *     return result;
     * }</pre>
     *
     * <p>{@link #reduce(double, DoubleBinaryOperator)}のように、{@code collect}処理は追加の同期処理を必要とせずに並列化できる。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @param <R> 結果の型
     * @param resultFactory 新しい結果コンテナを作成する関数。並列実行の場合、この関数は複数回呼ばれる場合があり、その度に新しい値を返す必要がある。
     * @param accumulator 追加の要素を結果に組み入れるための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数。
     * @param combiner 2つの値を統合するための、<a href="package-summary.html#Associativity">結合的</a>で<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>関数。累積関数と整合する必要がある。
     * @return 簡約の結果
     * @see Stream#collect(Supplier, BiConsumer, BiConsumer)
     */
    <R> R collect(Supplier<R> resultFactory,
                  ObjDoubleConsumer<R> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * このストリームの要素の和を返す。返される和は要素が出現する順番によって変わる場合がある。これは大きく異なる値の加算による累積丸め誤差が原因である。要素を絶対値の増加順にソートするとより正確な結果が出る傾向にある。いずれかの要素が{@code NaN}であるか、いずれかの時点での和が{@code NaN}である場合、和は{@code NaN}となる。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合であり、次のコードと等しい。
     * <pre>{@code
     *     return reduce(0, Double::sum);
     * }</pre>
     *
     * @return このストリームの要素の和
     */
    double sum();

    /**
     * このストリームの最小要素を表す{@code OptionalDouble}を返すか、もしこのストリームが空であれば空の{@code OptionalDouble}を返す。もしいずれかの他所がNaNであれば、結果は{@code Double.NaN}となる。数値比較演算子と異なり、このメソッドは負の零は正の零よりも真に小さいとみなす。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合であり、次のコードに等しい。
     * <pre>{@code
     *     return reduce(Double::min);
     * }</pre>
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの最小要素を表す{@code OptionalDouble}、またはこのストリームが空ならば空の{@code OptionalDouble}
     */
    OptionalDouble min();

    /**
     * このストリームの最大要素を表す{@code OptionalDouble}を返すか、もしこのストリームが空であれば空の{@code OptionalDouble}を返す。もしいずれかの他所がNaNであれば、結果は{@code Double.NaN}となる。数値比較演算子と異なり、このメソッドは負の零は正の零よりも真に小さいとみなす。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合であり、次のコードに等しい。
     * <pre>{@code
     *     return reduce(Double::min);
     * }</pre>
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの最大要素を表す{@code OptionalDouble}、またはこのストリームが空ならば空の{@code OptionalDouble}
     */
    OptionalDouble max();

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
     * このストリームの要素の平均値を表す{@code OptionalDouble}を返すか、もしこのストリームが空であれば空の{@code OptionalDouble}を返す。返される和は要素が出現する順番によって変わる場合がある。これは大きく異なる値の加算による累積丸め誤差が原因である。要素を絶対値の増加順にソートするとより正確な結果が出る傾向にある。いずれかの要素が{@code NaN}であるか、いずれかの時点での和が{@code NaN}である場合、平均値は{@code NaN}となる。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合である。
     *
     * @return このストリームの平均値を表す{@code OptionalDouble}、またはこのストリームが空ならば空の{@code OptionalDouble}
     */
    OptionalDouble average();

    /**
     * このストリームの要素の各種概要情報を表す{@code DoubleSummaryStatistics}を返す。これは<a href="package-summary.html#MutableReduction">簡約</a>の特別な場合である。
     *
     * @return このストリームの要素の各種概要情報を表す{@code DoubleSummaryStatistics}
     */
    DoubleSummaryStatistics summaryStatistics();

    /**
     * このストリームのある要素が与えられた述語に適合するか返す。結果を特定するのに必要でなければ全ての要素に対しては述語を評価しない。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @param predicate このストリームの要素に適用する<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return このストリームのある要素が与えられた述語に適合するならば{@code true}でそうでなければ{@code false}
     */
    boolean anyMatch(DoublePredicate predicate);

    /**
     * このストリームのすべての要素が与えられた述語に適合するか返す。結果を特定するのに必要でなければ全ての要素に対しては述語を評価しない。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @param predicate このストリームの要素に適用する<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return このストリームのすべての要素が与えられた述語に適合するならば{@code true}でそうでなければ{@code false}
     */
    boolean allMatch(DoublePredicate predicate);

    /**
     * このストリームのどの要素も与えられた述語に適合しないか返す。結果を特定するのに必要でなければ全ての要素に対しては述語を評価しない。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @param predicate このストリームの要素に適用する<a href="package-summary.html#NonInterference">非干渉的で状態を持たない</a>述語
     * @return このストリームのどの要素も与えられた述語に適合しないならば{@code true}でそうでなければ{@code false}
     */
    boolean noneMatch(DoublePredicate predicate);

    /**
     * このストリームの(出現順順序で)最初の要素を表す{@link OptionalDouble}、もしくはストリームが空であれば空の{@code OptionalDouble}を返す。このストリームが出現順順序を持たなければ任意の要素が返される場合がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * @return このストリームの最初の要素を表す{@code OptionalDouble}、またはこのストリームが空ならば空の{@code OptionalDouble}
     */
    OptionalDouble findFirst();

    /**
     * このストリームの任意の要素を表す{@link OptionalDouble}、もしくはストリームが空であれば空の{@code OptionalDouble}を返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">短絡的な末端処理</a>である。
     *
     * <p>この処理の動作は明示的に非決定的であり、どの要素を選んでもよい。これにより並列実行時の性能を最大化できる。その際のコストは同じ情報源に対する複数回の呼び出しが同じ値を返さないことである(もし出現順順序で最初の要素を望むならば、代わりに{@link #findFirst()}を用いよ)。
     *
     * @return このストリームのある要素を表す{@code OptionalDouble}、またはこのストリームが空ならば空の{@code OptionalDouble}
     * @see #findFirst()
     */
    OptionalDouble findAny();

    /**
     * このストリームの要素を{@code Double}にボックス化した要素からなる{@code Stream}を返す。
     *
     * @return このストリームの要素を{@code Double}にボックス化した要素からなる{@code Stream}
     */
    Stream<Double> boxed();

    @Override
    DoubleStream sequential();

    @Override
    DoubleStream parallel();

    @Override
    PrimitiveIterator.OfDouble iterator();

    @Override
    Spliterator.OfDouble spliterator();


    // Static factories

    /**
     * {@code DoubleStream}のビルダを返す。
     *
     * @return ストリームのビルダ
     */
    public static StreamBuilder.OfDouble builder() {
        return new Streams.DoubleStreamBuilderImpl();
    }

    /**
     * 空の逐次的{@code DoubleStream}を返す。
     *
     * @return 空の逐次的ストリーム
     */
    public static DoubleStream empty() {
        return StreamSupport.doubleStream(Spliterators.emptyDoubleSpliterator());
    }

    /**
     * 1つの要素を含む逐次的な{@code DoubleStream}を返す。
     *
     * @param t 1つの要素
     * @return 1つの要素を含む逐次的なストリーム
     */
    public static DoubleStream of(double t) {
        return StreamSupport.doubleStream(new Streams.DoubleStreamBuilderImpl(t));
    }

    /**
     * 要素が指定された値であるような逐次的なストリームを返す。
     *
     * @param values 新しいストリームの要素
     * @return 新しいストリーム
     */
    public static DoubleStream of(double... values) {
        return Arrays.stream(values);
    }

    /**
     * 初期要素{@code seed}に対して関数{@code f}の繰り返しの適用によって生成された無限{@code DoubleStream}を返す。{@code seed}, {@code f(seed)}, {@code f(f(seed))}などからなるストームを生成する。
     *
     * <p>{@code DoubleStream}の最初の要素(位置{@code 0})は{@code seed}によって与えられる。{@code n > 0}に対しては、その位置の要素は{@code f}を位置{@code n - 1}の要素に適用した結果である。
     *
     * @param seed 初期要素
     * @param f 新しい要素を生成するために以前の値に適用される関数
     * @return 新しい逐次的な{@code DoubleStream}
     */
    public static DoubleStream iterate(final double seed, final DoubleUnaryOperator f) {
        Objects.requireNonNull(f);
        final PrimitiveIterator.OfDouble iterator = new PrimitiveIterator.OfDouble() {
            double t = seed;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public double nextDouble() {
                double v = t;
                t = f.applyAsDouble(t);
                return v;
            }
        };
        return StreamSupport.doubleStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL));
    }

    /**
     * 各要素が{@code DoubleSupplier}によって与えられる、逐次的な{@code DoubleStream}を返す。定数のストリームや乱数のストリームなどを生成するのに向いている。
     *
     * @param s 要素の{@code DoubleSupplier}
     * @return 新しい逐次的な{@code DoubleStream}
     */
    public static DoubleStream generate(DoubleSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.doubleStream(Spliterators.spliteratorUnknownSize(
                new PrimitiveIterator.OfDouble() {
                    @Override
                    public boolean hasNext() { return true; }

                    @Override
                    public double nextDouble() { return s.getAsDouble(); }
                },
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL));
    }
}
