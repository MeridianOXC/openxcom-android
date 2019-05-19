# This is an attempt to port the .mk files over to CMake.
# Looks like CMake is the way to go (for now) for all native android
# projects.
#
# Please don't try to use this file as a reference anywhere. I barely
# even know what cmake is, let alone how to write it. The way I write it,
# it's even more rigid than makefiles, which kinda defeats the whole
# purpose of the damn thing.

# Don't try to use this for anything but the android build.
# You've been warned.

# Android Studio seems to use this version
cmake_minimum_required(VERSION 3.6.0)

# Build prerequisites first (yeah, by including whole files)
include(deps_cmake/sdl.cmake)
include(deps_cmake/sdl2_image.cmake)
include(deps_cmake/sdl2_mixer.cmake)
include(deps_cmake/sdl_gfx.cmake)
include(deps_cmake/yaml-cpp.cmake)

project(OPENXCOM CXX)

#file(GLOB OPENXCOM_FILES_BASE
#    OpenXcom/src/*.cpp)
set(OPENXCOM_FILES_BASE
    OpenXcom/src/main.cpp
    OpenXcom/src/md5.cpp
    OpenXcom/libs/miniz/miniz.c
    OpenXcom/src/lodepng.cpp)

file(GLOB OPENXCOM_FILES_BASESCAPE
    OpenXcom/src/Basescape/*.cpp)

file(GLOB OPENXCOM_FILES_BATTLESCAPE
    OpenXcom/src/Battlescape/*.cpp)

file(GLOB OPENXCOM_FILES_ENGINE
    OpenXcom/src/Engine/*.cpp
    OpenXcom/src/Engine/Adlib/*.cpp
    OpenXcom/src/Engine/Scalers/*.cpp
    )

file(GLOB OPENXCOM_FILES_GEOSCAPE
    OpenXcom/src/Geoscape/*.cpp)

file(GLOB OPENXCOM_FILES_INTERFACE
    OpenXcom/src/Interface/*.cpp)

file(GLOB OPENXCOM_FILES_MENU
    OpenXcom/src/Menu/*.cpp)

file(GLOB OPENXCOM_FILES_MOD
    OpenXcom/src/Mod/*.cpp)

file(GLOB OPENXCOM_FILES_SAVEGAME
    OpenXcom/src/Savegame/*.cpp)

file(GLOB OPENXCOM_FILES_UFOPAEDIA
    OpenXcom/src/Ufopaedia/*.cpp)

set(OPENXCOM_SOURCES
    ${OPENXCOM_FILES_BASE}
    ${OPENXCOM_FILES_BASESCAPE}
    ${OPENXCOM_FILES_BATTLESCAPE}
    ${OPENXCOM_FILES_ENGINE}
    ${OPENXCOM_FILES_GEOSCAPE}
    ${OPENXCOM_FILES_INTERFACE}
    ${OPENXCOM_FILES_MENU}
    ${OPENXCOM_FILES_MOD}
    ${OPENXCOM_FILES_SAVEGAME}
    ${OPENXCOM_FILES_UFOPAEDIA}
    )

add_library(openxcom
            SHARED
            ${OPENXCOM_SOURCES}
            )

set_source_files_properties(OpenXcom/libs/miniz/miniz.c PROPERTIES COMPILE_FLAGS -DMINIZ_NO_STDIO)

find_library(EGL_LIB EGL)
find_library(LOG_LIB log)
find_library(GLESv2_LIB GLESv2)
find_library(OpenSLES_LIB OpenSLES)
find_library(ZLIB z)
find_library(DLLIB dl)

include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()

target_link_libraries(openxcom
    SDL2
    SDL2_image
    SDL2_mixer
    SDL_gfx
    YAML_CPP
    ${EGL_LIB}
    ${LOG_LIB}
    ${GLESv2_LIB}
    ${OpenSLES_LIB}
    ${ZLIB}
    cpufeatures
    ${DLLIB}
    )

set_target_properties(openxcom
    PROPERTIES
    COMPILE_FLAGS
    "-D__NO_OPENGL\
    -D__MOBILE__\
    -DGIT_BUILD"
    )

configure_file("OpenXcom/src/git_version.h.in" "${CMAKE_CURRENT_BINARY_DIR}/git_version.h" )
include_directories ( "${CMAKE_CURRENT_BINARY_DIR}" )
