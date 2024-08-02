#!/bin/bash
cd ..
# Add '.dev' on the version name. Non dev builds are done though jenkins build only
export BUILD_NAME_SUFFIX=".dev-SNAPSHOT"
export BUILD_TYPE_NAME="debug"

./gradlew :android-appcoins-billing:clean :appcoins-billing:clean :communication:clean :appcoins-core:clean
./gradlew :android-appcoins-billing:assembleDebug :appcoins-billing:assembleDebug :communication:assembleDebug :appcoins-core:assembleDebug
./gradlew publish