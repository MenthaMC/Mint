<div align="center">
<h1> Mint </h1>

![Repo Stars](https://shields.io/github/stars/MenthaMC/Mint?style=flat-square)
![Repo Forks](https://shields.io/github/forks/MenthaMC/Mint?style=flat-square)
[![MIT License](https://img.shields.io/github/license/MenthaMC/Mint?style=flat-square)](LICENSE)
</div>

> [!IMPORTANT]\
> 这是Mint服务端的一个分支，主要编写的是比较激进的危险改动。\
> 所以在使用此分支的服务端之前请好好考虑。出现了问题，我们概不负责。

### 概述
Mint 是基于Folia的一种分支，专注于修复被破坏的特性和改进性能\
Mint 也想着将原本Bukkit插件支持重新实现（Frish想的）

### 构建
```shell
./gradlew applyPatches && ./gradlew createMojmapPaperclipJar
```

### API 
Maven 仓库
```xml
<repository>
    <id>menthamc</id>
    <url>https://frish.menthamc.com/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>dev.mint</groupId>
    <artifactId>mint-api</artifactId>
    <version>1.21.1-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
