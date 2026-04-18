# We don't use SDL's cmake file for some reason (probably because
# I don't really trust Android Studio's cmake to do anything sensible
# with it), so I just use a rough translation of what was in SDL2's Android.mk

# THIS FILE IS AS BAD AS IT GETS, IT ASSUMES IT'S INCLUDED
# IN THE TOP-LEVEL PROJECT AS IS, DON'T DO THAT KIDS

cmake_minimum_required(VERSION 3.6.0)

project(SDL2 C)

file(GLOB SDL_FILES_BASE
    SDL/src/*.c)

file(GLOB SDL_FILES_ATOMIC
        SDL/src/atomic/*.c)

file(GLOB SDL_FILES_AUDIO
        SDL/src/audio/*.c
        SDL/src/audio/dummy/*.c
		SDL/src/audio/openslES/*.c
        SDL/src/audio/android/*.c)

file(GLOB SDL_FILES_CORE
        SDL/src/core/android/*.c)

file(GLOB SDL_FILES_CPUINFO
        SDL/src/cpuinfo/*.c)

file(GLOB SDL_FILES_DYNAPI
        SDL/src/dynapi/*.c)

file(GLOB SDL_FILES_EVENTS
        SDL/src/events/*.c)

file(GLOB SDL_FILES_FILE
        SDL/src/file/*.c)

file(GLOB SDL_FILES_FILESYSTEM
        SDL/src/filesystem/android/*.c)

file(GLOB SDL_FILES_HAPTIC
        SDL/src/haptic/*.c
		SDL/src/haptic/android/*.c
        SDL/src/haptic/dummy/*.c)

file(GLOB SDL_FILES_JOYSTICK
        SDL/src/joystick/*.c
        SDL/src/joystick/android/*.c)

file(GLOB SDL_FILES_LOADSO
        SDL/src/loadso/dlopen/*.c)

file(GLOB SDL_FILES_POWER
        SDL/src/power/*.c
        SDL/src/power/android/*.c)

file(GLOB SDL_FILES_RENDER
        SDL/src/render/*.c
        SDL/src/render/opengles/*.c
        SDL/src/render/opengles2/*.c
        SDL/src/render/software/*.c)

file(GLOB SDL_FILES_STDLIB
        SDL/src/stdlib/*.c)

file(GLOB SDL_FILES_TEST
        SDL/src/test/*.c)

file(GLOB SDL_FILES_THREAD
        SDL/src/thread/*.c
        SDL/src/thread/pthread/*.c)

file(GLOB SDL_FILES_TIMER
        SDL/src/timer/*.c
        SDL/src/timer/unix/*.c)

file(GLOB SDL_FILES_VIDEO
        SDL/src/video/*.c
		SDL/src/video/yuv2rgb/*.c
        SDL/src/video/android/*.c)

file(GLOB SDL_FILES_SENSORS
		SDL/src/sensor/*.c
		SDL/src/sensor/dummy/*.c
		SDL/src/sensor/android/*.c)

file(GLOB SDL_FILES_HIDAPI
        SDL/src/hidapi/*.c
		SDL/src/hidapi/android/hid.cpp
		SDL/src/joystick/hidapi/*.c)

set(SDL_SOURCES
    ${SDL_FILES_BASE}
    ${SDL_FILES_ATOMIC}
    ${SDL_FILES_AUDIO}
    ${SDL_FILES_CORE}
    ${SDL_FILES_CPUINFO}
    ${SDL_FILES_DYNAPI}
    ${SDL_FILES_EVENTS}
    ${SDL_FILES_FILE}
    ${SDL_FILES_FILESYSTEM}
    ${SDL_FILES_HAPTIC}
    ${SDL_FILES_JOYSTICK}
    ${SDL_FILES_LOADSO}
    ${SDL_FILES_POWER}
    ${SDL_FILES_RENDER}
    ${SDL_FILES_STDLIB}
    ${SDL_FILES_TEST}
    ${SDL_FILES_THREAD}
    ${SDL_FILES_TIMER}
    ${SDL_FILES_VIDEO}
	${SDL_FILES_SENSORS}
#	${SDL_FILES_HIDAPI}  requires .so, so no.
    )

find_library(LOG_LIB log)
find_library(GLES1_LIB GLESv1_CM)
find_library(GLES2_LIB GLESv2)
find_library(ANDROID_LIB android)
find_library(DL_LIB dl)

add_library(SDL2
            STATIC
            ${SDL_SOURCES})

target_link_libraries(SDL2
        ${LOG_LIB}
        ${GLES1_LIB}
        ${GLES2_LIB}
        ${ANDROID_LIB}
        ${DL_LIB}

)
include_directories(${ANDROID_NDK}/sources/android/cpufeatures)

target_include_directories(SDL2 PUBLIC
        SDL/include)

set_target_properties(SDL2 PROPERTIES COMPILE_FLAGS "-DGL_GLEXT_PROTOTYPES")

# NDK r28+ treats -Wincompatible-function-pointer-types as an error. SDL2's
# OpenGL ES2 function table uses non-const pointer types that differ from the
# system headers by a const qualifier. Suppress the error for this SDL target.
target_compile_options(SDL2 PRIVATE -Wno-incompatible-function-pointer-types)