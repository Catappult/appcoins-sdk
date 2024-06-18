#!/bin/bash
cd ..

export BUILD_TYPE_NAME="release"

./gradlew :appcoins:clean :android-appcoins-billing:clean :appcoins-billing:clean :appcoins-ads:clean :appcoins-adyen:clean :communication:clean :appcoins-lifecycle:clean :appcoins-core:clean :appcoins-contract-proxy:clean
./gradlew :appcoins:assembleRelease :android-appcoins-billing:assembleRelease :appcoins-billing:assembleRelease :appcoins-ads:assembleRelease :appcoins-adyen:assembleRelease :communication:assembleRelease :appcoins-lifecycle:assembleRelease :appcoins-core:assembleRelease :appcoins-contract-proxy:assembleRelease
./gradlew publishToMavenLocal