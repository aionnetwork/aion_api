# aion_api
aion network api repository

Lastest version: 0.1.16

Notice:

The aionAPI targets to support the aion kernel v0.2.0 and newer.  
The develop can try to use the binary release to the aion kernel.  Therefore it is a beta version to test the aion kernel.
The source code will go publish soon once finish the license document.

for more details please check the wiki page.

## Kernel dependencies

aion_api contains dependencies on kernel modules (https://github.com/aionnetwork/aion).  The 
dependencies are declared in `build.gradle` as a Maven dependency; for example, for modRlp:

```
compile 'network.aion:modRlp:0.3.2'
```

This Maven publication is not today published to any Maven Central repository; instead, the
directory `lib/maven_repo` is used as a Maven repository.  Dependencies on Aion kernel modules
should all conform to this pattern.  

If you need to add a new kernel module dependency:

1. add the dependency by its Maven name into `build.gradle` (as in the modRlp example above)
1. optionally, check that the depedencies are what you expect by running `./gradlew dependencies --configuration compile -q`.  The dependency you added should be listed there, marked with FAILED since its jar is not in any Maven repository.
1. get the jar of that dependency into lib/maven_repo.  Assuming we want to add dependency modXYZ, then from a clone of the Aion kernel repo, run: `./gradlew :modXYZ:clean :modXYZ:build :modXYZ:publish -PpublishTarget=/LOCATION_OF_AION_API/lib/maven_repo`
1. build aion_api.  If the newly added module dependency has dependencies on other kernel modules that aren't yet in aion_api's maven repository, then the build will fail and this process will need to be repeated until they are all added.  

For reference, when this build logic was initially set up, the dependencies were:

```
compile 'network.aion:modAionBase:0.3.2'
compile 'network.aion:modCrypto:0.3.2'
compile 'network.aion:modLogger:0.3.2'
compile 'network.aion:modRlp:0.3.2'
compile 'network.aion:libnzmq:1.0'
```

And the Gradle invocation for putting the jars in `lib/maven_repo` was:

`./gradlew :modAionBase:publish :modCrypto:publish :modLogger:publish :modRlp:publish :3rdParty.libnzmq:publish :aion_vm_api:publish :modUtil:publish -PpublishTarget=/home/you/path_to/aion_api/lib/maven_repo`
