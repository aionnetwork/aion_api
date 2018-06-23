# setup paths
PACK_PATH="pack"
MOD_NAME="modAionApi.jar"
LIB_NAME="libAionApi.jar"


cd ${PACK_PATH}
cp -r ../native .
cp -r ../lib .
cp ../docs/Java-API-*.doc.zip .
rm lib/hamcrest-all-1.3.jar
rm lib/junit-4.12.jar
VER=$(java -jar ${MOD_NAME} -v)
echo "Aion Api build ver - $VER"
mv ${MOD_NAME} "modAionApi-v${VER}-$(date +%Y-%m-%d).jar"
ln -s "modAionApi-v${VER}-$(date +%Y-%m-%d).jar" modAionApi.jar 
mv ${LIB_NAME} "libAionApi-v${VER}-$(date +%Y-%m-%d).jar"
tar -czf "libAionApi-v${VER}-$(date +%Y-%m-%d).tar.gz" "libAionApi-v${VER}-$(date +%Y-%m-%d).jar" native lib Java-API-*.doc.zip
cd ..
git checkout -- ./src/org/aion/api/IAionAPI.java
