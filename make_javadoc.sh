#!/bin/sh

javadoc -locale ja_JP -noqualifier all -tag apiNote:a:"API注記" -sourcepath src/share/classes -breakiterator -encoding UTF-8 -d javadoc -use -charset UTF-8 -docencoding UTF-8 java.util.stream

cat stylesheet_override.css >> javadoc/stylesheet.css

