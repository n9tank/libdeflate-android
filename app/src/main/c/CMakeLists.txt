cmake_minimum_required(VERSION 3.7)
project(deflate)

set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS} -g -rdynamic -no-pie")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS} -O2")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -ffunction-sections -fdata-sections")
set(CMAKE_EXE_LINKER_FLAGS    "${CMAKE_EXE_LINKER_FLAGS}    -Wl,--gc-sections,--as-needed")
set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} -Wl,--gc-sections,--as-needed")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--gc-sections,--as-needed")
option(STATICA "libdeflate .a" OFF)
option(ARCH "ARCH" OFF)
option(OS "os" OFF)

if(ARCH)
if(OS)
set(SUFFIX _${CMAKE_SYSTEM_NAME}_${CMAKE_SYSTEM_PROCESSOR})
else()
set(SUFFIX _${CMAKE_SYSTEM_PROCESSOR})
endif()
else()
set(SUFFIX "")
endif()

if(STATICA)
add_library(libdeflate STATIC IMPORTED)
set_target_properties(libdeflate_static PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/${CMAKE_SYSTEM_NAME}/libdeflate${SUFFIX}${CMAKE_STATIC_LIBRARY_SUFFIX}
)
else()
add_subdirectory(libdeflate)
endif()

add_library(deflate SHARED
checksums.c
decompressor.c
compressor.c)

target_include_directories(
deflate
PUBLIC
${CMAKE_SOURCE_DIR}/libdeflate
)

target_link_libraries(
        deflate
        libdeflate_static
)
