#!/bin/bash
cd ..
# Add '.dev' on the version name. Non dev builds are done though jenkins build only
export BUILD_NAME_SUFFIX=".staging-SNAPSHOT"
export BUILD_TYPE_NAME="release"

./gradlew :android-appcoins-billing:clean :appcoins-billing:clean :communication:clean :appcoins-core:clean
./gradlew :android-appcoins-billing:assemble :appcoins-billing:assemble :communication:assemble :appcoins-core:assemble
./gradlew publish