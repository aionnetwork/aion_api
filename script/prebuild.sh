# append the git revision into the kernel version number
GITVER=$(git log -1 --format="%h")
TOKEN="String VERSION ="
sed -i -r "/$TOKEN/ s/.{2}$//" ./src/org/aion/api/IAionAPI.java
sed -i "/$TOKEN/ s/$/.$GITVER\";/" ./src/org/aion/api/IAionAPI.java
