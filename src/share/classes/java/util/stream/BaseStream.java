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

import java.util.Iterator;
import java.util.Spliterator;

/**
 * {@link Stream}や{@link IntStream}などのストリーム型のベースインターフェース。これらのメソッドの多くは{@link AbstractPipeline}で実装されているが、{@code AbstractPipeline}は{@code BaseStream}を直接には実装しない。
 *
 * @param <T> ストリーム要素の型
 * @param <S> {@code BaseStream}を実装したストリームの型
 * @since 1.8
 */
interface BaseStream<T, S extends BaseStream<T, S>> {
    /**
     * このストリームの要素のイテレータを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの要素イテレータ
     */
    Iterator<T> iterator();

    /**
     * このストリームの要素のスプリッテレータを返す。
     *
     * <p>これは<a href="package-summary.html#StreamOps">末端処理</a>である。
     *
     * @return このストリームの要素スプリッテレータ
     */
    Spliterator<T> spliterator();

    /**
     * このストリームが実行された際に(このストリームに対してこれ以上中間処理の追加や並列性の変更などといった変更が無いと仮定して)並列に実行されるかどうかを返す。この中間ストリーム処理や末端ストリーム処理を起動した後にこのメソッドを呼ぶと予測できない結果を得る場合がある。
     *
     * @return もしこれ以上の変更が無い場合にこのストリームが並列に実行されるならば{@code true}、そうでなければ{@code false}
     */
    boolean isParallel();

    /**
     * 逐次的であり同等なストリームを返す。このストリームが既に逐次的であるため、または元のストリームの状態が逐次的に変更されたため、このストリーム自身を返す場合がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @return 逐次的なストリーム
     */
    S sequential();

    /**
     * 並列的であり同等なストリームを返す。このストリームが既に並列的であるため、または元のストリームの状態が並列的に変更されたため、このストリーム自身を返す場合がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @return 並列ストリーム
     */
    S parallel();

    /**
     * <a href="package-summary.html#Ordering">順序を持たず</a>同等なストリームを返す。このストリームが既に順序を持たなければこのストリーム自身を返す場合がある。
     *
     * <p>これは<a href="package-summary.html#StreamOps">中間処理</a>である。
     *
     * @return 順序を持たないストリーム
     */
    S unordered();
}
