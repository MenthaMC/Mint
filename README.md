
<div align="center">
<img src="image/mint.png" alt="Mint" width="500">

### Mint 是基于 Folia 的分支，致力于提供更好的整体性能和原版机制

![GitHub Repo stars](https://img.shields.io/github/stars/MenthaMC/Mint?style=for-the-badge&logo=github&label=Stars&logoColor=white&color=ffda65)
![GitHub Build](https://img.shields.io/github/actions/workflow/status/MenthaMC/Mint/build_1.21.8.yml?style=for-the-badge&logo=github&label=Build&logoColor=white&color=06d094)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/MenthaMC/Mint/total?style=for-the-badge&logo=github&label=Downloads&logoColor=white&color=c4a400)
</div>

## ✨特色
- 可配置的原版特性
- Tpsbar、Membar、Regionbar、Networkbar监控
- 各种Fork的优化
- 对单线程区域性能的优化
- 支持线性区域文件格式
- 修复上游的错误
- 以及提高稳定性
- 集成 [Pufferfish](https://github.com/pufferfish-gg/Pufferfish) 的 [Sentry](https://sentry.io/welcome/)，轻松详细追踪服务器的所有报错
- NetworkAnalyser 数据包分析
- 支持部分function数据包

## 📦下载或构建
任何版本都可以在 [Release](https://github.com/MenthaMC/Mint/releases) 中找到，也可以通过以下步骤构建
```shell
./gradlew applyAllPatches && ./gradlew createMojmapPaperclipJar
```

## 📫联系
**QQ群: [1020403749](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=_UmBe7SYb9kBrh8pvTGr1aGPygk5DfF7&authKey=cCQ1U%2FTBIG8su93cGQK4rfm5vtqwXF3BSUz%2FAvd8st2S9BQ3PFeVHzbNAdFuSNWK&noverify=0&group_code=1020403749)** | **Discord： [点击加入](https://discord.gg/PK4YAtAHpr)**

## 📈BStats
[![bStats Graph Data](https://bstats.org/signatures/server-implementation/Mint.svg)](https://bstats.org/plugin/server-implementation/Mint)

## 🧪API
### Gradle
```
maven {
    name = "menthamc"
    url = "https://repo.menthamc.org/repository/maven-public/"
}
dependencies {
    compileOnly("dev.bacteriawa.mint:mint-api:$VERSION")
}
```
### Maven
```xml
<repository>
    <id>menthamc</id>
    <url>https://repo.menthamc.org/repository/maven-public/</url>
</repository>
```
```xml
<dependency>
    <groupId>dev.bacteriawa.mint</groupId>
    <artifactId>mint-api</artifactId>
    <version>$VERSION</version>
    <scope>provided</scope>
</dependency>
```

## 请给我们一个 ⭐Star！
> [!TIP]
> 你的每一个免费的 ⭐Star 就是我们每一个前进的动力！
> 
[![Star History Chart](https://api.star-history.com/svg?repos=MenthaMC/Mint&type=Date)](https://star-history.com/#MenthaMC/Mint&Date)
