cmake_minimum_required(VERSION 3.7)
project(deflate)

set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS} -g -rdynamic -no-pie")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS} -O3")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
cmake_minimum_required(VERSION 3.10)
project(MyJNILibrary)
add_compile_options(
  -ffunction-sections
  -fdata-sections
  -fvisibility=hidden
)
add_link_options(
  -fuse-ld=lld
  -Wl,--gc-sections
  -Wl,--strip-all
)
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
target_link_options(deflate PUBLIC
  -Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/export.map
)
target_include_directories(
deflate
PUBLIC
${CMAKE_SOURCE_DIR}/libdeflate
)
target_link_libraries(deflate
  PUBLIC
  -Wl,--start-group
  libdeflate_static
  -Wl,--end-group
  -Wl,--exclude-libs=ALL
)
