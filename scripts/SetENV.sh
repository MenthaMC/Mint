prop() {
  grep "^[[:space:]]*${1}" gradle.properties | cut -d'=' -f2 | sed 's/^[[:space:]]*//; s/\r//'
}

project_id="mint"
project_id_b="Mint"

commitid=$(git log --pretty='%h' -1)
mcversion=$(prop mcVersion)
grdversion=$(prop version)
preVersion=$(prop preVersion)
release_tag="$mcversion-$commitid"
jarName="$project_id-$mcversion"
jarName_dir="mint-server/build/libs/$jarName.jar"
make_latest=$([ $preVersion = "true" ] && echo "false" || echo "true")

# 确保目录存在
mkdir -p mint-server/build/libs/

# 重命名 JAR 文件
if [ -f "mint-server/build/libs/$project_id-paperclip-$grdversion-mojmap.jar" ]; then
  mv "mint-server/build/libs/$project_id-paperclip-$grdversion-mojmap.jar" "$jarName_dir"
fi

# 设置环境变量
{
  echo "project_id=$project_id"
  echo "project_id_b=$project_id_b"
  echo "commit_id=$commitid"
  echo "commit_msg=$(git log --pretty='> [%h] %s' -1)"
  echo "mcversion=$mcversion"
  echo "pre=$preVersion"
  echo "tag=$release_tag"
  echo "jar=$jarName"
  echo "jar_dir=$jarName_dir"
  echo "make_latest=$make_latest"
} >> $GITHUB_ENV
