# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Define the path to the API key file
set(INDICATIVE_API_KEY_FILE "${CMAKE_CURRENT_SOURCE_DIR}/INDICATIVE_API_KEY_FILE.txt")

# Read the API key from the file
file(READ ${INDICATIVE_API_KEY_FILE} INDICATIVE_API_KEY)

# Trim any trailing whitespace
string(STRIP ${INDICATIVE_API_KEY} INDICATIVE_API_KEY)

# Define the API key as a macro
add_definitions(-DINDICATIVE_API_KEY="${INDICATIVE_API_KEY}")

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("native-keys-storer")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
    # List C/C++ source files with relative paths to this CMakeLists.txt.
    native-keys-storer.cpp)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
    # List libraries link to the target library
    android
    log)
