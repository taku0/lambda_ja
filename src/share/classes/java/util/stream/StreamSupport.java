/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;

/**
 * ストリームの作成および操作のための低水準ユーティリティメソッド群。
 *
 * <p>このクラスは専らデータ構造に対してストリームとしてのビューを提供するライブラリ作者用である。一般利用者向けのほとんどの静的メソッドは{@link Streams}にある。
 *
 * <p>特に明記されない限り、ストリームは逐次的ストリームとして作成される。逐次ストリームは作成したストリームの{@code parallel()}メソッドの呼び出しにより並列ストリームに変換できる。
 *
 * @since 1.8
 */
public class StreamSupport {
    /**
     * {@code Spliterator}から新しい逐次{@code Stream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #stream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。
     *
     * @param <T> ストリーム要素の型
     * @param spliterator ストリーム要素を記述する{@code Spliterator}
     * @return 新しい逐次{@code Stream}
     */
    public static <T> Stream<T> stream(Spliterator<T> spliterator) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head<>(spliterator,
                                            StreamOpFlag.fromCharacteristics(spliterator),
                                            false);
    }

    /**
     * {@code Spliterator}から新しい並列{@code Stream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #parallelStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。
     *
     * @param <T> ストリーム要素の型
     * @param spliterator ストリーム要素を記述する{@code Spliterator}
     * @return 新しい並列{@code Stream}
     */
    public static <T> Stream<T> parallelStream(Spliterator<T> spliterator) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head<>(spliterator,
                                            StreamOpFlag.fromCharacteristics(spliterator),
                                            true);
    }

    /**
     * {@code Spliterator}の{@code Supplier}から新しい逐次{@code Stream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #stream(java.util.Spliterator)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。
     *
     * @param <T> ストリーム要素の型
     * @param supplier {@code Spliterator}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい逐次{@code Stream}
     * @see #stream(Spliterator)
     */
    public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier,
                                      int characteristics) {
        Objects.requireNonNull(supplier);
        return new ReferencePipeline.Head<>(supplier,
                                            StreamOpFlag.fromCharacteristics(characteristics),
                                            false);
    }

    /**
     * {@code Spliterator}の{@code Supplier}から新しい並列{@code Stream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #parallelStream(java.util.Spliterator)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。
     *
     * @param <T> ストリーム要素の型
     * @param supplier {@code Spliterator}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい並列{@code Stream}
     * @see #parallelStream(Spliterator)
     */
    public static <T> Stream<T> parallelStream(Supplier<? extends Spliterator<T>> supplier,
                                              int characteristics) {
        Objects.requireNonNull(supplier);
        return new ReferencePipeline.Head<>(supplier,
                                            StreamOpFlag.fromCharacteristics(characteristics),
                                            true);
    }

    /**
     * {@code Spliterator.OfInt}から新しい逐次{@code IntStream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #intStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。 
     *
     * @param spliterator ストリーム要素を記述する{@code Spliterator.OfInt}
     * @return 新しい逐次{@code IntStream}
     */
    public static IntStream intStream(Spliterator.OfInt spliterator) {
        return new IntPipeline.Head<>(spliterator,
                                      StreamOpFlag.fromCharacteristics(spliterator),
                                      false);
    }

    /**
     * {@code Spliterator.OfInt}から新しい並列{@code IntStream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。 
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #intParallelStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。 
     *
     * @param spliterator ストリーム要素を記述する{@code Spliterator.OfInt}
     * @return 新しい並列{@code IntStream}
     */
    public static IntStream intParallelStream(Spliterator.OfInt spliterator) {
        return new IntPipeline.Head<>(spliterator,
                                      StreamOpFlag.fromCharacteristics(spliterator),
                                      true);
    }

    /**
     * {@code Spliterator.OfInt}の{@code Supplier}から新しい逐次{@code IntStream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。 
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #intStream(java.util.Spliterator.OfInt)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。 
     *
     * @param supplier {@code Spliterator.OfInt}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator.OfInt}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい逐次{@code IntStream}
     * @see #intStream(Spliterator.OfInt)
     */
    public static IntStream intStream(Supplier<? extends Spliterator.OfInt> supplier,
                                      int characteristics) {
        return new IntPipeline.Head<>(supplier,
                                      StreamOpFlag.fromCharacteristics(characteristics),
                                      false);
    }

    /**
     * {@code Spliterator.OfInt}の{@code Supplier}から新しい並列{@code IntStream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。 
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #intParallelStream(java.util.Spliterator.OfInt)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。
     *
     * @param supplier {@code Spliterator.OfInt}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator.OfInt}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい並列{@code IntStream}
     * @see #intParallelStream(Spliterator.OfInt)
     */
    public static IntStream intParallelStream(Supplier<? extends Spliterator.OfInt> supplier,
                                              int characteristics) {
        return new IntPipeline.Head<>(supplier,
                                      StreamOpFlag.fromCharacteristics(characteristics),
                                      true);
    }

    /**
     * {@code Spliterator.OfLong}から新しい逐次{@code LongStream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #longStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Longerference">非干渉性</a>を参照せよ。 
     *
     * @param spliterator ストリーム要素を記述する{@code Spliterator.OfLong}
     * @return 新しい逐次{@code LongStream}
     */
    public static LongStream longStream(Spliterator.OfLong spliterator) {
        return new LongPipeline.Head<>(spliterator,
                                       StreamOpFlag.fromCharacteristics(spliterator),
                                       false);
    }

    /**
     * {@code Spliterator.OfLong}から新しい並列{@code LongStream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。 
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #longParallelStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Longerference">非干渉性</a>を参照せよ。 
     *
     * @param spliterator ストリーム要素を記述する{@code Spliterator.OfLong}
     * @return 新しい並列{@code LongStream}
     */
    public static LongStream longParallelStream(Spliterator.OfLong spliterator) {
        return new LongPipeline.Head<>(spliterator,
                                       StreamOpFlag.fromCharacteristics(spliterator),
                                       true);
    }

    /**
     * {@code Spliterator.OfLong}の{@code Supplier}から新しい逐次{@code LongStream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。 
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #longStream(java.util.Spliterator.OfLong)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Longerference">非干渉性</a>を参照せよ。 
     *
     * @param supplier {@code Spliterator.OfLong}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator.OfLong}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい逐次{@code LongStream}
     * @see #longStream(Spliterator.OfLong)
     */
    public static LongStream longStream(Supplier<? extends Spliterator.OfLong> supplier,
                                        int characteristics) {
        return new LongPipeline.Head<>(supplier,
                                       StreamOpFlag.fromCharacteristics(characteristics),
                                       false);
    }

    /**
     * {@code Spliterator.OfLong}の{@code Supplier}から新しい並列{@code LongStream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。 
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #longParallelStream(java.util.Spliterator.OfLong)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Longerference">非干渉性</a>を参照せよ。
     *
     * @param supplier {@code Spliterator.OfLong}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator.OfLong}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい並列{@code LongStream}
     * @see #longParallelStream(Spliterator.OfLong)
     */
    public static LongStream longParallelStream(Supplier<? extends Spliterator.OfLong> supplier,
                                                int characteristics) {
        return new LongPipeline.Head<>(supplier,
                                       StreamOpFlag.fromCharacteristics(characteristics),
                                       true);
    }

    /**
     * {@code Spliterator.OfDouble}から新しい逐次{@code DoubleStream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #doubleStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Doubleerference">非干渉性</a>を参照せよ。 
     *
     * @param spliterator ストリーム要素を記述する{@code Spliterator.OfDouble}
     * @return 新しい逐次{@code DoubleStream}
     */
    public static DoubleStream doubleStream(Spliterator.OfDouble spliterator) {
        return new DoublePipeline.Head<>(spliterator,
                                         StreamOpFlag.fromCharacteristics(spliterator),
                                         false);
    }

    /**
     * {@code Spliterator.OfDouble}から新しい並列{@code DoubleStream}を作成する。
     *
     * <p>スプリッテレータはストリープパイプラインの末端処理が開始した後にのみ走査・分割・推定サイズの問い合わせをされる。 
     *
     * <p>スプリッテレータは{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公表するか、<a href="Spliterator.html#binding">遅延束縛</a>をするよう強く推奨される。そうでなければデータ源との潜在的な干渉が起きる範囲を減らすために{@link #doubleParallelStream(Supplier, int)}を使うべきである。詳細は<a href="package-summary.html#Non-Doubleerference">非干渉性</a>を参照せよ。 
     *
     * @param spliterator ストリーム要素を記述する{@code Spliterator.OfDouble}
     * @return 新しい並列{@code DoubleStream}
     */
    public static DoubleStream doubleParallelStream(Spliterator.OfDouble spliterator) {
        return new DoublePipeline.Head<>(spliterator,
                                         StreamOpFlag.fromCharacteristics(spliterator),
                                         true);
    }

    /**
     * {@code Spliterator.OfDouble}の{@code Supplier}から新しい逐次{@code DoubleStream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。 
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #doubleStream(java.util.Spliterator.OfDouble)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Doubleerference">非干渉性</a>を参照せよ。 
     *
     * @param supplier {@code Spliterator.OfDouble}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator.OfDouble}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい逐次{@code DoubleStream}
     * @see #doubleStream(Spliterator.OfDouble)
     */
    public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier,
                                            int characteristics) {
        return new DoublePipeline.Head<>(supplier,
                                         StreamOpFlag.fromCharacteristics(characteristics),
                                         false);
    }

    /**
     * {@code Spliterator.OfDouble}の{@code Supplier}から新しい並列{@code DoubleStream}を作成する。
     *
     * <p>ファクトリ関数に対して{@link Supplier#get()}メソッドはストリームパイプラインの末端処理が開始した後に高々1回だけ呼ばれる。 
     *
     * <p>{@code IMMUTABLE}特性や{@code CONCURRENT}特性を公開するスプリッテレータや<a href="Spliterator.html#binding">遅延束縛</a>をするスプリッテレータは代わりに{@link #doubleParallelStream(java.util.Spliterator.OfDouble)}を使った方がより効率的である可能性が高い。この形式では{@code Supplier}の利用により、データ源との潜在的な干渉の範囲を減らすための1段階の間接参照が与えられる。ファクトリ関数は末端処理が開始した後にのみ呼ばれるため、データ源に対する末端処理の開始までの全ての変更は結果のストリームに反映される。詳細は<a href="package-summary.html#Non-Doubleerference">非干渉性</a>を参照せよ。
     *
     * @param supplier {@code Spliterator.OfDouble}の{@code Supplier}
     * @param characteristics 与えられた{@code Spliterator.OfDouble}の特性。特性は{@code source.get().getCharacteristics()}と等しい必要がある。
     * @return 新しい並列{@code DoubleStream}
     * @see #doubleParallelStream(Spliterator.OfDouble)
     */
    public static DoubleStream doubleParallelStream(Supplier<? extends Spliterator.OfDouble> supplier,
                                                    int characteristics) {
        return new DoublePipeline.Head<>(supplier,
                                         StreamOpFlag.fromCharacteristics(characteristics),
                                         true);
    }
}
