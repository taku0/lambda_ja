# java.util.streamパッケージ非公式和訳

Java 8のjava.util.streamパッケージのJavadocを非公式に和訳したドキュメントです。

JDK 8 Build b128に基いています。 

次の場所に生成したドキュメントを置きました。
http://www.tatapa.org/~takuo/lambda_ja/javadoc/index.html

Javadocの生成には https://github.com/taku0/translation_doclet が必要です。

## 利用方法

JDK 8のソースを適当なディレクトリに展開する。

sbt ( http://www.scala-sbt.org/ )をインストールする。

https://github.com/taku0/translation_doclet をダウンロードしてビルド(sbt package)する。

extract.shを適切に書換え、実行する。

出来上がったtranslation.en.htmlをこのディレクトリのsourceディレクトリに置く。

OmegaT ( http:www.omegat.org/ )をインストールし、このディレクトリを開く。

翻訳結果を生成する。

翻訳結果をtranslation_docletのディレクトリにtranslation.ja.htmlとして置く。

inject.shを適切に書換え、実行する。

stylesheet_override.cssの内容をstylesheet.cssに追記する。

## License

Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
Copyright 2014 taku0 ( https://github.com/taku0 )

GPL 2.0によってライセンスされます。
詳細はLICENSEを参照してください。
