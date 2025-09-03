# Minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Path to the API Keys file.
set(API_KEYS_FILE "${CMAKE_SOURCE_DIR}/API_KEYS.txt")

# Function to read the key-value pairs from the API Keys file
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

read_config_file(${API_KEYS_FILE})

# Definition of the values for the API Keys
add_definitions(-DINDICATIVE_API_KEY="${INDICATIVE_API_KEY}")
add_definitions(-DINDICATIVE_API_KEY_DEV="${INDICATIVE_API_KEY_DEV}")

# Definition of the values for the Matomo URL
add_definitions(-DMATOMO_URL="${MATOMO_URL}")
add_definitions(-DMATOMO_URL_DEV="${MATOMO_URL_DEV}")

# Definition of the values for the Matomo API Keys
add_definitions(-DMATOMO_API_KEY="${MATOMO_API_KEY}")
add_definitions(-DMATOMO_API_KEY_DEV="${MATOMO_API_KEY_DEV}")

# Declaration of the project name.
project("native-keys-storer")

# Adding library to the project for visibility to the Java code.
add_library(${CMAKE_PROJECT_NAME} SHARED native-keys-storer.cpp)

# Add 16 KB page size compatibility
target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")
target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,common-page-size=16384")
