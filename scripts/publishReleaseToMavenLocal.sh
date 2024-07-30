#!/bin/bash
cd ..

export BUILD_TYPE_NAME="release"

./gradlew :appcoins:clean :android-appcoins-billing:clean :appcoins-billing:clean :communication:clean :appcoins-lifecycle:clean :appcoins-core:clean
./gradlew :appcoins:assembleRelease :android-appcoins-billing:assembleRelease :appcoins-billing:assembleRelease :communication:assembleRelease :appcoins-lifecycle:assembleRelease :appcoins-core:assembleRelease
./gradlew publishToMavenLocal