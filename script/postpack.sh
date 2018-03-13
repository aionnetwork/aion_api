# setup paths
PACK_PATH="pack"
BIN_NAME="modAionApi.jar"

cd ${PACK_PATH}
cp -r ../native .
VER=$(java -jar ${BIN_NAME} -v)
echo "Aion Api build ver - $VER"
mv ${BIN_NAME} "modAionApi-v${VER}-$(date +%Y-%m-%d).jar"

cd ..
git checkout -- ./src/org/aion/api/IAionAPI.java
