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

/**
 * {@code CloseableStream}は閉じられる{@code Stream}である。
 * closeメソッドはオブジェクトが保持している資源(開いているファイルなど)を開放するために呼び出される。
 *
 * @param <T> ストリーム要素の型
 * @since 1.8
 */
public interface CloseableStream<T> extends Stream<T>, AutoCloseable {

    /**
     * この資源を閉じ、裏にある資源を放棄する。このメソッドは{@code try}-with-resources文によって管理されているオブジェクトに対しては自動的に呼び出される。資源が既に閉じられている場合は呼ばれても何もしない。
     *
     * このメソッドはチェックされる{@code Exception}を{@link AutoCloseable#close() AutoCloseable.close()}のようには投げられない。閉じる処理が失敗するかもしれない場合は実装者による慎重な配慮が必要とされる。裏にある資源を放棄し、この資源が閉じられたと内部的に<em>印を付ける</em>ように強く勧められる。{@code close}メソッドは普通1回よりも多くは呼ばれないため、これは資源の素早い開放を保障する。さらに、これはこの資源が他の資源を包んだ場合や他の資源に包まれた場合に起き得る問題を減らす。
     *
     * @see AutoCloseable#close()
     */
    void close();
}
