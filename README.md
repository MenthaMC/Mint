<div align="center">
<h1> Mint </h1>

![Repo Stars](https://shields.io/github/stars/MenthaMC/Mint?style=flat-square)
![Repo Forks](https://shields.io/github/forks/MenthaMC/Mint?style=flat-square)
[![MIT License](https://img.shields.io/github/license/MenthaMC/Mint?style=flat-square)](LICENSE)
</div>

## 概述
- Mint 是基于Folia的一种分支，专注于修复被破坏的特性和改进性能
- 同时也提供了可调的原版配置和优化

## 构建
```shell
./gradlew applyPatches && ./gradlew createMojmapPaperclipJar
```

## API 
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

## Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=MenthaMC/Mint&type=Date)](https://star-history.com/#MenthaMC/Mint&Date)
