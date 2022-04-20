# PDCache

![java library](https://img.shields.io/badge/type-Libary-gr.svg "type")
![JDK 14](https://img.shields.io/badge/JDK-14-green.svg "SDK")
![Gradle 6.5](https://img.shields.io/badge/Gradle-6.5-04303b.svg "tool")
![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg "License")

-- [Java Doc](https://apidoc.gitee.com/PatternDirClean/PDCache) --

-------------------------------------------------------------------------------

## 简介

轻量级，高可靠，易学习的数据缓存工具

## 用法示例

如果你熟悉缓存的概念，将会让你非常容易学习本工具。

如果熟悉 Cleaner 和 Reference 对象、软引用、弱引用 等将会使你更懂本工具的一些高级用法，因为本工具中的缓存功能是靠 Reference 实现，TimeMapCache 除外

并且使用了 Cleaner 意味着缓存被回收时可以有回调，但是要求该回调要从被缓存的对象内获取，也就是要求被缓存的对象必须继承一个接口，该接口只有一个方法，用来抛出回调对象

最好熟悉我的 并发工具 [PDConcurrent](https://gitee.com/PatternDirClean/PDConcurrent) ，本工具所有方法均为并发安全，并且完全托管给 [PDConcurrent](https://gitee.com/PatternDirClean/PDConcurrent) 工具进行并发管理

### \> > 基础示例

Cache 对象用于缓存单独一个对象，不过貌似并不常用到，但是其实现最简单，而且使用起来方便

```java
public static
void main(String[] args) {
    // 构造缓存对象，内部默认为 SoftReference 软引用，被缓存的对象为 String
    Cache<String> cache = PDCache.Cache(String.class).build();
    try {
        // 存入要缓存的对象
        cache.set("hello word");

        // 取出之前缓存的对象
        String s = cache.get();
    } catch ( Exception e ) {
        e.printStackTrace();
    }
}
```

### \> > Map示例

MapCache 中的缓存实现部分与 Cache 中的理念相同，但是实现相对复杂一点，与 Map 一样的 键->值 存储，不过与 WeakHashMap 不同的是，其中被缓存的是 值 而不是 键

WeakHashMap 中决定是否丢失缓存的是 键 是否有引用，而 MapCache 中决定是否丢失缓存的是 值 是否有引用

```java
public static
void main(String[] args) {
    // 构造缓存对象，内部默认为 SoftReference 软引用
    MapCache<String, String> cache = PDCache.MapCache(String.class, String.class).build();
    try {
        // 存入要缓存的对象
        cache.put("h", "hello word");

        // 取出之前缓存的对象
        String s = cache.get("h");
    } catch ( Exception e ) {
        e.printStackTrace();
    }
}
```

## 变形示例

其中有一些特殊的缓存工具，这里只介绍 Cache 和 MapCache 的自填充变形，其他的请前往 wiki

自填充变形分别叫做 SCache 和 SMapCache，所谓自填充，就是取数据时如果对应的缓存对象已经被释放，则自己重新生成，该类缓存的 put 和 set 方法已经被删除。

以 SMapCache 为例，这个是我用的最多的类，构造时需要额外调用构造器的 `createdata` 方法传入一个生成接口，该接口在对应的缓存为空时被调用，传入当前取值的 键，并要求返回对应的 值，缓存对象将会用该值进行填充

```java
public static
void main(String[] args) {
    // 构造缓存对象，内部默认为 SoftReference 软引用
    SMapCache<String, String> cache = PDCache.SMapCache(String.class, String.class)
                                             // 指定的生成方法，返回 "key:[键]"
                                             .createdata(v -> "key:" + v).build();
    try {
        // 取出来的是 "key:h"
        String s = cache.get("h");
    } catch ( Exception e ) {
        e.printStackTrace();
    }
}
```

## 使用方法
请导入其 `jar` 文件,文件在 **发行版** 或项目的 **jar** 文件夹下可以找到

> PDCache_bin.jar 为包含了依赖库的包，PDCache.jar 为不包含依赖库的包

**发行版中可以看到全部版本<br/>项目下的 jar 文件夹是当前最新的每夜版**

依赖的同系列的项目
- [PDConcurrent](https://gitee.com/PatternDirClean/PDConcurrent)

可通过 **WIKI**、**java doc** 或者 **测试类** 查看示例，并深入学习本工具

## 分支说明
**dev-master**：当前的开发分支，可以拿到最新的每夜版 jar

**releases**：当前发布分支，稳定版的源码

-------------------------------------------------------------------------------

### 提供bug反馈或建议

- [码云Gitee](https://gitee.com/PatternDirClean/PDCache/issues)
- [Github](https://github.com/PatternDirClean/PDCache/issues)