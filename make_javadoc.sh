#!/bin/sh

javadoc -locale ja_JP -noqualifier all -tag apiNote:a:"API注記" -tag implSpec:a:"実装仕様" -sourcepath src/share/classes -breakiterator -encoding UTF-8 -d javadoc -use -charset UTF-8 -docencoding UTF-8 -windowtitle 'java.util.stream API仕様 非公式翻訳' java.util.stream

cat stylesheet_override.css >> javadoc/stylesheet.css
