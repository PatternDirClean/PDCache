# PDCache

![java library](https://img.shields.io/badge/type-Libary-gr.svg "type")
![JDK 14](https://img.shields.io/badge/JDK-14-green.svg "SDK")
![Gradle 6.5](https://img.shields.io/badge/Gradle-6.5-04303b.svg "tool")
![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg "License")

-- [Java Doc](https://apidoc.gitee.com/PatternDirClean/PDCache) --

-------------------------------------------------------------------------------

## 简介

轻量级，高可靠性，强一致性，基于 java 内部 `Reference` 工具的数据缓存工具，用于在程序内部缓存可丢失型的数据。

该工具目前仅可以保存数据，不可对其进行序列化。

也可用 Map 实现 保存根据 id 区分的数据或可丢失的锁，强一致性的特点可以保证对应 id 存储的 数据 或 锁 为同一个，且在长期不使用时自动移除。

## 使用方法
请导入其 `jar` 文件,文件在 **发行版** 或项目的 **jar** 文件夹下可以找到

**发行版中可以看到全部版本<br/>项目下的 jar 文件夹是当前最新的每夜版**

依赖的同系列的项目
- [PDConcurrent](https://gitee.com/PatternDirClean/PDConcurrent)

可通过 **WIKI** 或者 **doc文档** 查看示例

## 分支说明
**dev-master**：当前的开发分支，可以拿到最新的每夜版 jar

**releases**：当前发布分支，稳定版的源码

-------------------------------------------------------------------------------

### 提供bug反馈或建议

- [码云Gitee](https://gitee.com/PatternDirClean/PDCache/issues)
- [Github](https://github.com/PatternDirClean/PDCache/issues)