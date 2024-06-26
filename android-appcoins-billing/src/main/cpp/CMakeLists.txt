# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Set the path to your configuration file
set(API_KEYS_FILE "${CMAKE_SOURCE_DIR}/API_KEYS.txt")

# Function to read the key-value pairs from the config file
function(read_config_file filepath)
    file(READ "${filepath}" content)
    string(REPLACE "\n" ";" lines "${content}")

    foreach (line IN LISTS lines)
        string(FIND "${line}" "=" delimiter_pos)
        if (NOT delimiter_pos EQUAL -1)
            string(SUBSTRING "${line}" 0 ${delimiter_pos} key)
            math(EXPR value_start "${delimiter_pos} + 1")
            string(SUBSTRING "${line}" ${value_start} -1 value)
            set(${key} "${value}" PARENT_SCOPE)
        endif ()
    endforeach ()
endfunction()

# Call the function to read the config file
read_config_file(${API_KEYS_FILE})

# Define the values as preprocessor definitions
add_definitions(-DINDICATIVE_API_KEY="${INDICATIVE_API_KEY}")
add_definitions(-DINDICATIVE_API_KEY_DEV="${INDICATIVE_API_KEY_DEV}")
add_definitions(-DRAKAM_API_KEY="${RAKAM_API_KEY}")
add_definitions(-DRAKAM_API_KEY_DEV="${RAKAM_API_KEY_DEV}")
add_definitions(-DADYEN_API_KEY="${ADYEN_API_KEY}")
add_definitions(-DADYEN_API_KEY_DEV="${ADYEN_API_KEY_DEV}")

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
add_library(${CMAKE_PROJECT_NAME} SHARED native-keys-storer.cpp)
