/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * {@code Stream}の可変なビルダ。これにより要素を個別に生成して{@code StreamBuilder}へ追加して({@code ArrayList}を一時バッファとして使った場合の複製のオーバーヘッド無しに){@code Stream}を作成できるようになる。
 *
 * <p>{@code StreamBuilder}はライフサイクルを持ち、要素を追加できる構築中段階から始まり、要素を追加できなくなる構築済段階に移行する。構築済段階は{@link #build()}メソッドが呼ばれた段階で開始し、{@link #build()}メソッドはこのストリームビルダに追加された要素を追加された順序で要素として持つ{@code Stream}を作成する。
 *
 * <p>プリミティブ値に特化した{@code StreamBuilder}が{@link OfInt int}や{@link OfLong long}や{@link OfDouble double}の値のために用意されている。
 *
 * @param <T> ストリーム要素の型
 * @see Stream#builder()
 * @since 1.8
 */
public interface StreamBuilder<T> extends Consumer<T> {

    /**
     * 構築中のストリームに要素を追加する。
     *
     * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
     */
    @Override
    void accept(T t);

    /**
     * 構築中のストリームに要素を追加する。
     *
     * @implSpec
     * デフォルト実装は次のように振る舞う。
     * <pre>{@code
     *     accept(t)
     *     return this;
     * }</pre>
     *
     * @param t 追加する要素
     * @return ビルダである{@code this}
     * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
     */
    default StreamBuilder<T> add(T t) {
        accept(t);
        return this;
    }

    /**
     * ストリームを構築し、このビルダを構築済段階に移行させる。このビルダに対してさらなる処理をしようとすると{@code IllegalStateException}が投げられる。
     *
     * @return 構築されたストリーム
     * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
     */
    Stream<T> build();

    /**
     * {@code IntStream}の可変なビルダ。
     *
     * <p>ストリームビルダはライフサイクルを持ち、要素を追加できる構築中段階から始まり、要素を追加できなくなる構築済段階に移行する。構築済段階は{@link #build()}メソッドが呼ばれた段階で開始し、{@link #build()}メソッドはこのストリームビルダに追加された要素を追加された順序で要素として持つストリームを作成する。 
     *
     * @see IntStream#builder()
     * @since 1.8
     */
    interface OfInt extends IntConsumer {

        /**
         * 構築中のストリームに要素を追加する。
         *
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        @Override
        void accept(int t);

        /**
         * 構築中のストリームに要素を追加する。
         *
         * @implSpec
         * デフォルト実装は次のように振る舞う。
         * <pre>{@code
         *     accept(t)
         *     return this;
         * }</pre>
         *
         * @param t 追加する要素
         * @return ビルダである{@code this}
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        default StreamBuilder.OfInt add(int t) {
            accept(t);
            return this;
        }

        /**
         * ストリームを構築し、このビルダを構築済段階に移行させる。このビルダに対してさらなる処理をしようとすると{@code IllegalStateException}が投げられる。
         *
         * @return 構築されたストリーム
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        IntStream build();
    }

    /**
     * {@code LongStream}の可変なビルダ。
     *
     * <p>ストリームビルダはライフサイクルを持ち、要素を追加できる構築中段階から始まり、要素を追加できなくなる構築済段階に移行する。構築済段階は{@link #build()}メソッドが呼ばれた段階で開始し、{@link #build()}メソッドはこのストリームビルダに追加された要素を追加された順序で要素として持つストリームを作成する。 
     *
     * @see LongStream#builder()
     * @since 1.8
     */
    interface OfLong extends LongConsumer {

        /**
         * 構築中のストリームに要素を追加する。
         *
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        @Override
        void accept(long t);

        /**
         * 構築中のストリームに要素を追加する。
         *
         * @implSpec
         * デフォルト実装は次のように振る舞う。
         * <pre>{@code
         *     accept(t)
         *     return this;
         * }</pre>
         *
         * @param t 追加する要素
         * @return ビルダである{@code this}
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        default StreamBuilder.OfLong add(long t) {
            accept(t);
            return this;
        }

        /**
         * ストリームを構築し、このビルダを構築済段階に移行させる。このビルダに対してさらなる処理をしようとすると{@code IllegalStateException}が投げられる。
         *
         * @return 構築されたストリーム
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        LongStream build();
    }

    /**
     * {@code DoubleStream}の可変なビルダ.
     *
     * (訳註: ここにあるはずの文章がaccpetのところにコピペミスされている)
     * 
     * @see LongStream#builder()
     * @since 1.8
     */
    interface OfDouble extends DoubleConsumer {

        /**
         * 構築中のストリームに要素を追加する。
         *
         * <p>ストリームビルダはライフサイクルを持ち、要素を追加できる構築中段階から始まり、要素を追加できなくなる構築済段階に移行する。構築済段階は{@link #build()}メソッドが呼ばれた段階で開始し、{@link #build()}メソッドはこのストリームビルダに追加された要素を追加された順序で要素として持つストリームを作成する。
         * (訳註: おそらくコピペミス)
         *
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        @Override
        void accept(double t);

        /**
         * 構築中のストリームに要素を追加する。
         *
         * @implSpec
         * デフォルト実装は次のように振る舞う。
         * <pre>{@code
         *     accept(t)
         *     return this;
         * }</pre>
         *
         * @param t 追加する要素
         * @return ビルダである{@code this}
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        default StreamBuilder.OfDouble add(double t) {
            accept(t);
            return this;
        }

        /**
         * ストリームを構築し、このビルダを構築済段階に移行させる。このビルダに対してさらなる処理をしようとすると{@code IllegalStateException}が投げられる。
         *
         * @return 構築されたストリーム
         * @throws IllegalStateException ビルダが既に構築済段階に移行している場合
         */
        DoubleStream build();
    }
}
