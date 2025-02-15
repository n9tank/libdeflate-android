# 介绍

libDeflate-android是一个不同于libDeflate-java的分支，它更考虑编译大小与执行性能。

# 说明

调用方法发生了修改，此外为了性能考虑我移除了不必要的检查与复制。

native的私有调用是公开的，这是为了更高的自由度。
