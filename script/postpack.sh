# setup paths
PACK_PATH="pack"
MOD_NAME="aion_api.jar"
LIB_NAME="libAionApi.jar"


cd ${PACK_PATH}
cp -r ../native .
cp -r ../lib .

mv lib/hamcrest-all-1.3.jar ../
mv lib/junit-4.12.jar ../
VER=$(${JAVA_HOME}/bin/java -jar ${MOD_NAME} -v)

echo "Aion Api build ver - $VER"
mv ${MOD_NAME} "modAionApi-v${VER}-$(date +%Y-%m-%d).jar"
ln -s "modAionApi-v${VER}-$(date +%Y-%m-%d).jar" modAionApi.jar 
mv ${LIB_NAME} "libAionApi-v${VER}-$(date +%Y-%m-%d).jar"
mv ../Java-API-doc.zip "Java-API-v${VER}-doc.zip"
tar -czf "libAionApi-v${VER}-$(date +%Y-%m-%d).tar.gz" "libAionApi-v${VER}-$(date +%Y-%m-%d).jar" native lib "Java-API-v${VER}-doc.zip"
cd ..

mv hamcrest-all-1.3.jar lib
mv junit-4.12.jar lib

git checkout -- ./src/org/aion/api/IAionAPI.java
