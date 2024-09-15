cmake_minimum_required(VERSION 3.7)
project(deflate-jni)
add_subdirectory(libdeflate)
set(JNI
    checksums.c
    jni_util.c
    jni_util.h
    #decompressor.c
    compressor.c
    common.h
)
add_library(deflate_jni SHARED ${JNI})
target_include_directories(
        deflate_jni    
        PUBLIC    
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/libdeflate
)        
find_library(
        log-lib
        log)
target_link_libraries(
        deflate_jni     
        ${log-lib}
        libdeflate_shared
)
