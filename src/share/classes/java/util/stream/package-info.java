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

/**
 * <h1>java.util.stream</h1>
 *
 * 値のストリームに対する以下のような関数型の操作を提供するクラス群。
 * 
 * (訳註: まだ書きかけの部分が多々有り、TODOなども書かれている。jdk8/tlのリビジョン7053:2602eab5f086より作成)
 *
 * <pre>{@code
 *     int sumOfWeights = blocks.stream().filter(b -> b.getColor() == RED)
 *                                       .mapToInt(b -> b.getWeight())
 *                                       .sum();
 * }</pre>
 *
 * <p>ここでは{@code Collection}などである{@code blocks}をストリームのデータ源(source)として使い、ストリームに対して絞り込み(filter)・写像(map)・簡約(reduce)({@code sum()}は<a href="package-summary.html#Reduction">簡約</a>(reduction)処理の1例である)を実行して赤いブロックの重さの和を計算した。 
 *
 * <p>この手法で利用したキーとなるインターフェースは、{@code Stream}およびそれをプリミティブ型に特化した{@link java.util.stream.IntStream}, {@link java.util.stream.LongStream}, {@link java.util.stream.DoubleStream}である。ストリームとコレクションにはいくつか異なる点がある。
 *
 * <ul>
 *     <li>記憶領域を持たない。ストリームは要素を保持するデータ構造ではない。その代わりにデータ源(データ構造・ジェネレータ・IOチャネルなど)から計算処理のパイプラインを通して値をもたらす。</li>
 *     <li>本質的に関数的である。ストリームに対する処理は成果物を生成するが、元となるデータ源を変更しない。例えば、{@code Stream}に対する絞り込みは、元となるデータ源から要素を削除するのではなく、新しい{@code Stream}を生成する。</li>
 *     <li>遅延的(lazy)であろうとする。絞り込み・写像・重複の削除といったストリームに対する多くの処理は遅延的に実装でき、最適化の機会をもたらす(例えば、「パターンにマッチする最初の{@code String}を探す」際には全ての入力文字列を調べなくてもよい)。ストリームに対する処理は中間処理(intermediate operation, {@code Stream}を生成する)と末端処理(terminal operation, 値を生成する)に分けられる。全ての中間処理は遅延的である。</li>
 *     <li>上限が無い場合がある。コレクションは有限の大きさを持つが、ストリームはそうでない。{@code limit(n)}や{@code findFirst()}といった処理を使うと無限のストリームに対する計算が有限の時間で終わるようにできる。</li>
 * </ul>
 *
 * <h2><a name="StreamPipelines">ストリームパイプライン</a></h2>
 *
 * ストリームは<a href="package-summary.html#StreamOps">処理</a>の<em>パイプライン</em>の作成のために使用される。完全なストリームパイプラインは複数の構成要素からなり、データ源(source, {@code Collection}・配列・ジェネレータ関数・IOチャネルなど)・{@code Stream.filter}や{@code Stream.map}といった零個以上の<em>中間処理</em>(intermediate operation)・{@code Stream.forEach}や{@code Stream.reduce}といった終端処理(terminal operation)からなる。ストリーム処理は引数として<em>関数値</em>(function values, 多くの場合ラムダ式であるが、メソッド参照やオブジェクトの場合もある)を取る場合がある。例えば{@code Stream#filter}メソッドに渡す{@code Predicate}のように、関数値は処理の動作をカスタマイズする。
 *
 * <p>中間処理は新しい{@code Stream}を返す。それらは遅延的(lazy)である。{@link java.util.stream.Stream#filter Stream.filter}のような中間処理を実行しても実際の絞り込みは実行されず、代わりに最初の{@code Stream}の要素のうち与えられた{@code Predicate}にマッチする要素を(走査した際に)含む新しい{@code Stream}を作成する。末端処理が実行されるまでストリームのデータ源の要素は消費されない。
 * 
 * <p>末端処理は{@code Stream}を消費し、成果物を生成するか、副作用を発生させる。末端処理を実行した後はそのストリームはもはや利用できず、元のデータ源から別の処理を開始するか、新しいデータ源を選び別の処理を開始する必要がある。例えば全ての赤いブロックの重さの和を求めた後に全ての青いブロックの重さも求めたい場合は、2つの異なるストリームに対して絞り込み・写像・簡約をする必要がある。
 *
 * <pre>{@code
 *     int sumOfRedWeights  = blocks.stream().filter(b -> b.getColor() == RED)
 *                                           .mapToInt(b -> b.getWeight())
 *                                           .sum();
 *     int sumOfBlueWeights = blocks.stream().filter(b -> b.getColor() == BLUE)
 *                                           .mapToInt(b -> b.getWeight())
 *                                           .sum();
 * }</pre>
 *
 * <p>しかし、複数回の走査が現実的でない場合や非効率的である場合は、両方の結果を1パスで取得するテクニックもある。TODO リンクを提供する
 *
 * <h3><a name="StreamOps">ストリーム処理</a></h3>
 *
 * {@code filter}や{@code sorted}といったストリームに対する中間処理は常に新しい{@code Stream}を生成し、常に<em>遅延的</em>(lazy)である。遅延処理を呼び出してもストリームの内容は処理されない。全ての処理は末端処理が開始されるまで延期される。ストリームの遅延的な処理により大幅に効率化が達成できる。上記の絞り込み・写像・合計の例のようなパイプラインでは、絞り込み・写像・加算は1パスに融合(fuse)でき、中間状態を最小にできる。また遅延性を使うと、必要ない場合には全てのデータを調べなくても済むようにできる。例えば「1000文字以上の最初の文字列を探せ」といった場合、全ての入力文字列を調べる必要はなく、目的の性質を持つものだけを探せばよい(この性質は入力ストリームが単に大きいだけでなく無限である場合にさらに重要となる)。
 *
 * <p>中間処理はさらに<em>状態を持たない</em>(stateless)ものと<em>状態を持つ</em>(stateful)ものに分けられる。状態を持たない処理は、新しい値を処理する際に前の値に起因する状態を保持しない。状態を持たない中間処理の例には{@code filter}や{@code map}が含まれる。状態を持つ処理は、新しい値を処理する際に前の値に起因する状態を組み入れる場合がある。状態を持つ中間処理の例には{@code distinct}や{@code sorted}が含まれる。状態を持つ処理は結果を生成する前に全ての入力を処理する必要がある場合がある。たとえばストリームを整列した結果はストリームの全ての入力を見るまで全く生成できない。結果として、並列計算において、状態を持つ中間処理を含むパイプラインは複数のパスで実行される必要がある場合がある。状態を持たない中間処理のみを含むパイプラインは、逐次的にも並列的にも1パスで実行できる。
 * 
 * <p>さらに、ある種の処理は<em>短絡的</em>(short-circuiting)処理であると言われる。中間処理は、無限の入力を与えられた際に有限のストリームを結果として生成する場合があるならば短絡的である。末端処理は、無限の入力を与えられた際に有限の時間で終了する場合があるならば短絡的である(短絡的処理を含むというのは無限ストリームに対する処理が有限時間で完了するための必要条件であるが十分条件ではない)。末端処理(例えば{@code forEach}や{@code findFirst})は常に即時的(eagar, 値を返す前に完全に実行される)であり、{@code Stream}以外のプリミティブ値や{@code Collection}などの結果を生成するか、副作用を持つ。
 *
 * <h3>並列性</h3>
 *
 * 集約処理を値のストリームに対する処理のパイプラリンに書き直すと、多くの集約処理はより簡単に並列化できる。{@code Stream}は逐次的(serial)にも並列的(parallel)にも実行できる。ストリームは作られる際に逐次的ストリームか並列的ストリームとして作られる。ストリームの並列性は{@link java.util.stream.Stream#sequential()}処理と{@link java.util.stream.Stream#parallel()}処理によっても切り換えられる。JDKで実装されている{@code Stream}は並列性が明示的に要求されない限り逐次ストリームを作成する。例えば、{@code Collection}は{@link java.util.Collection#stream}メソッドと{@link java.util.Collection#parallelStream}メソッドを持ち、それぞれ逐次的ストリームと並列的ストリームを生成する。{@link java.util.stream.IntStream#range(int, int)}などの他のストリームを生じるメソッドは逐次的ストリームを生成するが、結果に対して{@code parallel()}を呼ぶと効率的に並列化できる。逐次的ストリームと並列的ストリームには同一の処理が用意されている。「ブロックの重さの合計」を問い合わせる処理を並列的にするには次のようにする。
 * 
 * <pre>{@code
 *     int sumOfWeights = blocks.parallelStream().filter(b -> b.getColor() == RED)
 *                                               .mapToInt(b -> b.getWeight())
 *                                               .sum();
 * }</pre>
 *
 * <p>この例における逐次版と並列版の唯一の違いは最初の{@code Stream}の生成部分である。{@code Stream}が逐次的に実行されるか並列的に実行されるかは{@code Stream#isParallel}によって決定できる。末端処理が開始された際に、ストリームパイプラインは全体が逐次的にまたは全体が並列的に実行される。それはストリームの逐次・並列処理に影響する最後の処理によって決まる(ストリームのデータ源か{@code sequential()}メソッドまたは{@code parallel()}メソッドである)。
 *
 * <p>並列処理の結果が決定的(deterministic)であり、かつ逐次版と整合するためには、各種ストリーム演算に渡される関数値は<a href="#NonInteference"><em>状態を持たない</em></a>必要がある。
 * 
 * <h3><a name="Ordering">順序</a></h3>
 *
 * ストリームは<em>出現順順序</em>(encounter order)を持っている場合と持っていない場合がある。出現順順序はストリームから処理パイプラインに要素が与えられる順番を規定する。出現順順序の有無はデータ源・中間処理・末端処理によって決まる。ある種のストリームデータ源({@code List}や配列など)は本質的に順序付けられている(ordered)が、他のもの({@code HashSet}など)はそうでない。{@link java.util.stream.Stream#sorted()}などのある種の中間処理は順序付けられていないストリームに出現順順序を導入する場合がある。一方で別の処理は順序付けられたストリームを順序付けられていない状態にする場合がある({@link java.util.stream.Stream#unordered()}など)。ある種の末端処理は出現順順序を無視する({@link java.util.stream.Stream#forEach}など)。
 *
 * <p>もしストリームが順序付けられている場合、ほとんどの処理は要素を出現順に処理するよう制約される。もしストリームが{@code [1, 2, 3]}を含む{@code List}である場合、{@code map(x -> x*2)}を実行した結果は{@code [2, 4, 6]}である必要がある。しかし、もしデータ源に出現順順序が定められていなければ、値{@code [2, 4, 6]}の6通りの順列はどれも妥当な結果となる。順序の制約があっても多くの処理は効率的に並列化できる。
 *
 * <p>逐次ストリームに対しては、順序付けは同じデータ源に対して処理を繰り返し実行した場合の決定性にのみ関連する({@code ArrayList}は要素を順序に従って列挙するように制約されているが、{@code HashSet}はそうでなく、列挙を繰り返した場合異なる順序となる場合がある)。
 * 
 * <p>並列ストリームに対しては、順序付けの制約を緩めるとある種の処理に対して最適化された実装が可能となる。例えば順序付けられたストリームから重複を除去する場合、最初のパーティションを完全に処理するまで後のパーティションからの要素は、先に利用可能であっても返せない。一方、順序付けの制約がない場合、重複の除去は共有された{@code ConcurrentHashSet}を使ってより効率的にできる。ストリームが構造的には順序付けられている(データ源が順序付けられていて、中間処理が順序を保存する)がユーザが特に出現順にこだわらない場合がある。場合によっては{@link java.util.stream.Stream#unordered()}メソッドを使ってストリームから明示的に順序を取り除くと、状態を持つ処理や末端処理の並列性能を向上できる。
 * 
 * <h3><a name="Non-Interference">非干渉性(Non-interference)</a></h3>
 *
 * {@code java.util.stream}パッケージを使うと、{@code ArrayList}などのスレッド安全でないコレクションを含む様々なデータ源に対しても、並列な場合も含めてまとめて要素を処理できるようになる。これはストリームパイプラインを実行している間、データ源に対する<em>干渉</em>(interference)を防げる場合のみ可能となる(パイプラインの実行は末端処理が呼び出された際に開始し、末端処理が完了した際に終了する)。
 * 
 * <p>したがって、ストリームメソッドに渡されたラムダ式(または適切な関数型インターフェースを実装した他のオブジェクト)はストリームのデータ源を決して変更するべきでない。ある実装は、データ源を変更したり変更させたりするとき、そのデータ源に対して<em>干渉する</em>(interfere)と言う。非干渉性の必要性は全てのパイプラインに要求され、並列なものに留まらない。ストリームのデータ源が並行(concurrent)でない限り、ストリームパイプラインの実行中にストリームのデータ源を変更すると例外・不正な答え・一般的でない結果を引き起こす。
 * 
 * <p>さらに、ストリームオペレーションに渡したラムダ式が<em>状態を持つ</em>(stateful)場合、結果は非決定的なものや間違ったものとなる場合がある。状態を持つラムダ(もしくは適切な関数型インターフェースを実装した他のオブジェクト)とは、その結果がストリームパイプラインの実行中に変化する可能性がある状態に依存するものである。状態を持つラムダの例は次のようなものである。
 *
 * <pre>{@code
 *     Set<Integer> seen = Collections.synchronizedSet(new HashSet<>());
 *     stream.parallel().map(e -> { if (seen.add(e)) return 0; else return e; })...
 * }</pre>
 *
 * ここでもしマップ処理が並列に実行されると、同じ入力に対しても、スレッドのスケジューリングの違いにより、実行するごとに異なる結果となる場合がある。一方、状態を持たないラムダ式であれば結果は常に同じとなる。
 * 
 * <h3>副作用</h3>
 * (訳註: この節はまだ空)
 *
 * <h2><a name="Reduction">簡約処理</a></h2>
 *
 * <em>簡約</em>(reduction)処理は例えば数の集合に対して合計や最大値を求めるなど、要素のストリームを受け取ってそれを1つの値や要約に簡約する(reduce。さらに複雑なシナリオにおいては簡約処理は1つの値に簡約する前に要素から値を抽出する必要がある。例えばブロックの集合から重さの合計を求める場合、重さを合計する前に各ブロックから重さを抽出する必要がある)。
 *
 * <p>もちろん、このような処理は簡単な逐次的ループとして次のように難なく実装できる。
 * 
 * <pre>{@code
 *    int sum = 0;
 *    for (int x : numbers) {
 *       sum += x;
 *    }
 * }</pre>
 * 
 * しかし、{@link java.util.stream.Stream#reduce 簡約処理}を選択すると、上記のような可変的な累積と比較して大きな利点を得られる。つまり、適切に構築された簡約処理は{@link java.util.function.BinaryOperator 簡約演算子}(reduction operator)が適切な性質を持つかぎり本質的に並列化できる。特に、そのような演算子は<a href="#Associativity">結合的</a>(associative)である必要がある。例えば、数のストリームが与えられた場合に合計を求めたい場合、次のように書ける。
 *
 * <pre>{@code
 *    int sum = numbers.reduce(0, (x,y) -> x+y);
 * }</pre>
 * もしくはより簡潔に次のように書ける。
 * <pre>{@code
 *    int sum = numbers.reduce(0, Integer::sum);
 * }</pre>
 *
 * <p>({@link java.util.stream.IntStream}のようにプリミティブ値に特化した{@link java.util.stream.Stream}は{@link java.util.stream.IntStream#sum() sum}や{@link java.util.stream.IntStream#max() max}のような一般的な簡約処理を簡便なメソッドとして用意している。それらは簡約の簡単なラッパーとして実装されている)。
 *
 * <p>{@code reduce}の実装は、ストリームの部分部分に対して並列的に処理を実行してその後中間処理結果をまとめて最終的な正しい答えを得られるため、簡約は上手く並列化できる。上記の元のfor-eachループの代わりに、並列化可能な{@link java.util.stream.Stream#forEach(Consumer) forEach()}メソッドを使ったとしても{@code sum}という累積変数に対してスレッド安全な更新をする必要があり、必要となる同期処理は並列化から得られる性能向上を打ち消してしまうだろう。代わりに{@code reduce}メソッドを使えば簡約処理を並列化する際の一切の面倒を除去でき、ライブラリは余分な同期処理を必要とせずに効率的な並列実装を提供できるようになる。
 *
 * <p>以前示した「ブロック」の例では、簡約を他の処理と組み合わせて、ループをバルク処理で置き換える様子を示した。もし{@code blocks}が{@code getWeight}メソッドを持つ{@code Block}オブジェクトのコレクションである場合、最も重いブロックを次ようにして求められる。
 *
 * <pre>{@code
 *     OptionalInt heaviest = blocks.stream()
 *                                  .mapToInt(Block::getWeight)
 *                                  .reduce(Integer::max);
 * }</pre>
 *
 * <p>最も一般的な形式では、{@code <T>}型の要素に対して{@code <U>}型の結果を産出する{@code reduce}処理は3つのパラメータを必要とする。
 * <pre>{@code
 * <U> U reduce(U identity,
 *              BiFunction<U, ? super T, U> accumlator,
 *              BinaryOperator<U> combiner);
 * }</pre>
 *
 * ここで、<em>単位元</em>(identity)は簡約の最初の種となるとなる値であり、同時に要素が無い場合のデフォルトの結果でもある。<em>累積関数</em>(accumulator)は途中結果と次の要素を受けとり、新しい途中結果を生成する。<em>統合関数</em>(combiner)は2つの累積関数からの途中結果を統合して新しい途中結果を生成し、最終的に最終結果を生成する。
 *
 * <p>この形式は2引数形式の一般化であり、上で示した写像・簡約(map-reduce)構成(訳註: {@code map}メソッドと{@code reduce}メソッドに分けて書く形式)の一般化でもある。単純な{@code sum}の例をより一般的な形式で再構成しようと思えば、{@code 0}が単位元となり、{@code Integer::sum}が累積関数と統合関数となる。重さの合計の例の場合、次のように再構成できる。
 * <pre>{@code
 *     int sumOfWeights = blocks.stream().reduce(0,
 *                                               (sum, b) -> sum + b.getWeight())
 *                                               Integer::sum);
 * }</pre>
 * しかし、写像・簡約形式の方がより読みやすく一般的には好ましい。一般的な形式は写像と簡約を1つの関数へ統合すると多くの処理が最適化によって取り除ける場合のために用意されている。
 *
 * <p>より形式的には{@code identity}は統合関数の<em>単位元</em>である必要がある。つまり任意の{@code u}に対して、{@code combiner.apply(identity, u)}は{@code u}であるということである。加えて、{@code combiner}関数は<a href="#Associativity">結合的</a>であり、{@code accumulator}関数と適合する必要がある。つまり任意の{@code u}と{@code t}について次の等式が成り立つ必要がある。
 * <pre>{@code
 *     combiner.apply(u, accumulator.apply(identity, t)) == accumulator.apply(u, t)
 * }</pre>
 *
 * <h3><a name="MutableReduction">可変的簡約</a></h3>
 *
 * <em>可変的</em>(mutable)簡約処理は値のストリームを1つの値に簡約するという点では類似しているが、独立した1つの値を生成するのではなく、ストリームの値を処理する中で{@code Collection}や{@code StringBuilder}などの汎用的な<em>結果コンテナ</em>(result container)を変更していく。
 *
 * <p>例えば、文字列のストリームを受け取り、1つの長い文字列に連結したいとする。これは次のような通常の簡約<em>でも</em>実現できるだろう。 
 * <pre>{@code
 *     String concatenated = strings.reduce("", String::concat)
 * }</pre>
 *
 * これでも望む結果は得られるし、並列的にも動作できる。しかし性能は満足いくものではないだろう。このような実装は大量の文字列コピーを実行し、実行時間は要素数に対して<em>O(n^2)</em>となるだろう。より性能面で良い方法は、{@link java.lang.StringBuilder}という文字列を累積していく可変的コンテナに結果を累積していく方法だろう。可変的簡約の並列化には通常の簡約の際と同じ手法が使える。
 *
 * <p>可変的簡約処理は{@code StringBuilder}のような結果コンテナに望む結果を収集(collect)していくため、{@link java.util.stream.Stream#collect(Collector) collect()}と呼ばれる。{@code collect}処理は3つのものを必要とする。新しい結果コンテナを構築するファクトリ関数・新たな要素を取り入れて結果関数を更新する累積関数・2つの結果コンテナを取りその内容を統合する統合関数である。この形式は通常の簡約の形式とよく似ている。
 * <pre>{@code
 * <R> R collect(Supplier<R> resultFactory,
 *               BiConsumer<R, ? super T> accumulator,
 *               BiConsumer<R, R> combiner);
 * }</pre>
 * {@code reduce()}と同様に{@code collect}をこのような抽象的な形で表す利点は、並列化を直ちに適用できる点である。つまり中間結果を並列的に累積し、その後統合する。例えば、ストリーム中の要素の文字列形式を{@code ArrayList}に集めたい場合、次のような明確な逐次的for-each形式でも書ける。
 * <pre>{@code
 *     ArrayList<String> strings = new ArrayList<>();
 *     for (T element : stream) {
 *         strings.add(element.toString());
 *     }
 * }</pre>
 * あるいは、並列化可能な収集形式(collect form)で次のようにも書ける。
 * <pre>{@code
 *     ArrayList<String> strings = stream.collect(() -> new ArrayList<>(),
 *                                                (c, e) -> c.add(e.toString()),
 *                                                (c1, c2) -> c1.addAll(c2));
 * }</pre>
 * もしくは、写像処理を累積関数の中に埋め込んでいるのに注意するとさらに簡潔に次のように書ける。
 * <pre>{@code
 *     ArrayList<String> strings = stream.map(Object::toString)
 *                                       .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
 * }</pre>
 * ここでは供給者(supplier)はただの{@link java.util.ArrayList#ArrayList() ArrayListのコンストラクタ}であり、累積関数は文字列化した要素を{@code ArrayList}に加えていき、統合関数としては単に{@link java.util.ArrayList#addAll addAll}を使ってあるコンテナから別のコンテナに文字列をコピーする。
 *
 * <p>通常の簡約処理と同様に、並列化は<a href="package-summary.html#Associativity">結合性</a>の条件が成り立つときのみに可能である。{@code combiner}は結果コンテナ{@code r1}, {@code r2}, {@code r3}について次の2つが等しいときに結合的である。
 * <pre>{@code
 *    combiner.accept(r1, r2);
 *    combiner.accept(r1, r3);
 * }</pre>
 * <pre>{@code
 *    combiner.accept(r2, r3);
 *    combiner.accept(r1, r2);
 * }</pre>
 * ここで「等しい」とは、{@code r1}が同じ状態(要素型の{@link java.lang.Object#equals equals}の意味で)となるという意味である。同様に{@code resultFactory}は{@code combiner}について<em>単位元</em>となる必要がある。つまり任意の結果コンテナ{@code r}について次の式が{@code r}の状態を(やはり{@link java.lang.Object#equals equals}の意味で)変えない必要がある。
 * <pre>{@code
 *     combiner.accept(r, resultFactory.get());
 * }</pre>
 * 最後に、{@code accumulator}と{@code combiner}は適合する必要がある。つまり結果コンテナ{@code r}と要素{@code t}について次の2つの式が等しくなる必要がある。
 * <pre>{@code
 *    r2 = resultFactory.get();
 *    accumulator.accept(r2, t);
 *    combiner.accept(r, r2);
 * }</pre>
 * <pre>{@code
 *    accumulator.accept(r,t);
 * }</pre>
 * ここで「等しい」とは、{@code r}が(やはり{@link java.lang.Object#equals equals}の意味で)同じ状態となるという意味である。
 *
 * <p>{@code collect}の3つの側面、つまり供給者・累積関数・統合関数は非常に密に結合している場合が多く、3つの側面を包括するオブジェクトとして{@link java.util.stream.Collector}を導入すると便利である。単に{@code Collector}を取り結果コンテナを返す{@link java.util.stream.Stream#collect(Collector) collect}が用意されている。文字列を{@code List}に収集する上記の例は標準の{@code Collector}を使って次のように書き換えられる。
 * <pre>{@code
 *     ArrayList<String> strings = stream.map(Object::toString)
 *                                       .collect(Collectors.toList());
 * }</pre>
 *
 * <h3><a name="ConcurrentReduction">簡約と並行性と順序</a></h3>
 *
 * 例えば次のような{@code Map}を生成する収集のように、複雑な簡約処理について考える。
 * <pre>{@code
 *     Map<Buyer, List<Transaction>> salesByBuyer
 *         = txns.parallelStream()
 *               .collect(Collectors.groupingBy(Transaction::getBuyer));
 * }</pre>
 * (ここで{@link java.util.stream.Collectors#groupingBy}は要素の集合をなんらかのキーによってグループ化する{@link java.util.stream.Collector}を返すユーティリティ関数である)。この処理を並列的に実行するのは実際には逆効果である。これは統合段階({@code Map}をキーに従って他のマップに統合する)の処理はある種の{@code Map}の実装では高価となるためである。
 *
 * <p>しかし、この簡約で使われる結果コンテナが{@link java.util.concurrent.ConcurrentHashMap ConcurrentHashMap}のように並行的に変更できるコレクションであったとしよう。その場合、並列的に複数起動された累積関数は共有する1つの結果コンテナに結果を累積でき、統合関数が個別の結果コンテナを統合せずに済むようにできるだろう。これは並列実行の性能を向上する可能性がある。これを<em>並行</em>簡約と呼ぶ。
 *
 * <p>並行簡約をサポートする{@link java.util.stream.Collector}は{@link java.util.stream.Collector.Characteristics#CONCURRENT}特性の印が付けられている。並行コレクタが与えられているというのは並行簡約のための必要条件であるがそれだけでは不十分である。複数の累積関数が共有コンテナに値を累積している様子を想像してみると、結果が累積される順番は非決定的であるとわかる。それゆえ、並行簡約はストリームを処理する順番が重要でないときにのみ可能となる。{@link java.util.stream.Stream#collect(Collector)}は次の条件が満たされるときにのみ並行簡約を実施する。
 * <ul>
 * <li>ストリームが並列的である。</li>
 * <li>コレクタが{@link java.util.stream.Collector.Characteristics#CONCURRENT}特性を持つ。</li>
 * <li>ストリームが順序を持たない、またはコレクタが{@link java.util.stream.Collector.Characteristics#UNORDERED}特性を持つ。</li>
 * </ul>
 * 例:
 * <pre>{@code
 *     Map<Buyer, List<Transaction>> salesByBuyer
 *         = txns.parallelStream()
 *               .unordered()
 *               .collect(groupingByConcurrent(Transaction::getBuyer));
 * }</pre>
 * (ここで{@link java.util.stream.Collectors#groupingByConcurrent}は{@code groupingBy}の並行版である)。
 *
 * <p>順序は並行挿入によって失なわれる特性の1つであるため、もし与えられたキーに対する要素の順序が重要な場合は並行簡約はできないことに注意せよ。そういった条件の場合、逐次簡約か統合ベースの並列簡約に制限される。
 *
 * <h2><a name="Associativity">結合性</a></h2>
 * (訳註: この後の節はまだ書きかけという性質が強い)
 *
 * 演算子または関数{@code op}は次の条件を満たすとき<em>結合的</em>である。
 * <pre>{@code
 *     (a op b) op c == a op (b op c)
 * }</pre>
 * 並列評価にとってこの性質の重要性は次の4つの式に適用するとわかる。
 * <pre>{@code
 *     a op b op c op d == (a op b) op (c op d)
 * }</pre>
 * つまり{@code (a op b)}を{@code (c op d)}と並列に評価し、その後その結果に{@code op}を適用できる。
 * TODO 可変統合関数にとって結合性とはどういう意味となる?
 * FIXME: 可変な結合性については上記した。
 *
 * <h2><a name="StreamSources">ストリームのデータ源</a></h2>
 * TODO この節はどこに入れる?
 *
 * XXX - この節をストリームの構築に変え、徐々により複雑な構築方法を紹介するように変える
 *     - Collectionからの構築
 *     - Iteratorからの構築
 *     - 配列からの構築
 *     - ジェレネレータからの構築
 *     - スプリッテレータからの構築
 *
 * XXX - 以下は極めて低水準であるが、ストリームの構築の重要な側面である
 *
 * <p>パイプラインはストリームのデータ源から与えられるスプリッテレータ(spliterator, {@link java.util.Spliterator}を見よ)からまず構築される。スプリッテレータはデータ源の要素を扱い、並列な場合も含めた計算のための走査処理を提供する。スプリッテレータを使ったパイプラインの構築については{@link java.util.stream.Streams}のメソッド群を見よ。
 *
 * <p>データ源は直接スプリッテレータを用意する場合がある。その場合、スプリッテレータは末端処理が開始した後に走査され・分割し・大きさの見積りを問い合わせられるが、決して末端処理が開始する前にはなされない。スプリッテレータは{@code IMMUTABLE}特性か{@code CONCURRENT}特性を公表するよう強く推奨される。もしくは<em>遅延束縛</em>(late-binding)をし、走査・分割・大きさの見積りの問い合わせをされるまで扱う要素を束縛するべきでない。
 *
 * <p>もしデータ源が推奨されるスプリッテレータを直接用意できない場合は、スプリッテレータを{@code Supplier}を使って間接的に用意してもよい。スプリッテレータはストリームパイプラインの末端処理が開始した後に取得されるが、決してその前には取得されない。
 *
 * <p>このような要求は潜在的な干渉が起きる範囲を末端処理の開始から、結果を生成するか副作用を発生させて終了するまでの範囲に大幅に低減する。詳細は<a href="package-summary.html#Non-Interference">非干渉性</a>を参照せよ。
 *
 * XXX - 以下を非干渉性の節に移動する
 *
 * <p>データ源は末端処理を開始する前なら変更でき、それらの変更は扱われる要素に反映される。その後のそれ以上の変更は、データ源の性質に依存して反映されないか、{@code ConcurrentModificationException}が発生する可能性がある。
 * 
 * <p>例えば、以下のコードについて考える。
 * <pre>{@code
 *     List<String> l = new ArrayList(Arrays.asList("one", "two"));
 *     Stream<String> sl = l.stream();
 *     l.add("three");
 *     String s = sl.collect(toStringJoiner(" ")).toString();
 * }</pre>
 * 最初に"one"と"two"の2つの要素を持つリストが作成される。そしてストリームがそのリストから作成される。次にリストは3つ目の文字列"three"の追加により変更される。最後にストリームの要素は収集され、共に結合される。リストは末端の{@code collect}処理が開始する前に変更されたので、結果は文字列"one two three"となる。しかし、もしリストが次のように末端処理が開始された後に変更されたとしよう。
 * <pre>{@code
 *     List<String> l = new ArrayList(Arrays.asList("one", "two"));
 *     Stream<String> sl = l.stream();
 *     String s = sl.peek(s -> l.add("BAD LAMBDA")).collect(toStringJoiner(" ")).toString();
 * }</pre>
 * すると{@code peek}処理は末端処理が始まった後に文字列"BAD LAMBDA"をリストに追加しようとするので{@code ConcurrentModificationException}が投げられる。
 */

package java.util.stream;
