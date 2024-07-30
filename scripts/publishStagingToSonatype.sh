#!/bin/bash
cd ..
# Add '.dev' on the version name. Non dev builds are done though jenkins build only
export BUILD_NAME_SUFFIX=".staging-SNAPSHOT"
export BUILD_TYPE_NAME="release"

./gradlew :appcoins:clean :android-appcoins-billing:clean :appcoins-billing:clean :communication:clean :appcoins-lifecycle:clean :appcoins-core:clean
./gradlew :appcoins:assemble :android-appcoins-billing:assemble :appcoins-billing:assemble :communication:assemble :appcoins-lifecycle:assemble  :appcoins-core:assemble
./gradlew publish