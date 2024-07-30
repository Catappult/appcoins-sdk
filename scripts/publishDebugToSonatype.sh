#!/bin/bash
cd ..
# Add '.dev' on the version name. Non dev builds are done though jenkins build only
export BUILD_NAME_SUFFIX=".dev-SNAPSHOT"
export BUILD_TYPE_NAME="debug"

./gradlew :appcoins:clean :android-appcoins-billing:clean :appcoins-billing:clean :communication:clean :appcoins-lifecycle:clean :appcoins-core:clean
./gradlew :appcoins:assembleDebug :android-appcoins-billing:assembleDebug :appcoins-billing:assembleDebug :communication:assembleDebug :appcoins-lifecycle:assembleDebug :appcoins-core:assembleDebug
./gradlew publish