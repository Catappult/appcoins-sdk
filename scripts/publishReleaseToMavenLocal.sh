#!/bin/bash
cd ..

export BUILD_TYPE_NAME="release"

./gradlew :appcoins:clean :android-appcoins-billing:clean :appcoins-billing:clean :appcoins-adyen:clean :communication:clean :appcoins-lifecycle:clean :appcoins-core:clean
./gradlew :appcoins:assembleRelease :android-appcoins-billing:assembleRelease :appcoins-billing:assembleRelease :appcoins-adyen:assembleRelease :communication:assembleRelease :appcoins-lifecycle:assembleRelease :appcoins-core:assembleRelease
./gradlew publishToMavenLocal