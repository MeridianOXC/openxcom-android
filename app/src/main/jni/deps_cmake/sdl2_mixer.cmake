cmake_minimum_required(VERSION 3.4.0)

project(SDL2_mixer CXX)

set(SDL2_MIXER_BASE
    SDL2_mixer/dynamic_flac.c
    SDL2_mixer/dynamic_fluidsynth.c
    SDL2_mixer/dynamic_mod.c
    SDL2_mixer/dynamic_modplug.c
    SDL2_mixer/dynamic_mp3.c
    SDL2_mixer/dynamic_ogg.c
    SDL2_mixer/effect_position.c
    SDL2_mixer/effects_internal.c
    SDL2_mixer/effect_stereoreverse.c
    SDL2_mixer/fluidsynth.c
    SDL2_mixer/load_aiff.c
    SDL2_mixer/load_flac.c
    SDL2_mixer/load_mp3.c
    SDL2_mixer/load_ogg.c
    SDL2_mixer/load_voc.c
    SDL2_mixer/mixer.c
    SDL2_mixer/music.c
    SDL2_mixer/music_cmd.c
    SDL2_mixer/music_flac.c
    SDL2_mixer/music_mad.c
    SDL2_mixer/music_mod.c
    SDL2_mixer/music_modplug.c
    SDL2_mixer/music_ogg.c
    SDL2_mixer/wavestream.c
)

set(SDL2_MIXER_OGG_FILES
    SDL2_mixer/external/libvorbisidec-1.2.1/mdct.c
    SDL2_mixer/external/libvorbisidec-1.2.1/block.c
    SDL2_mixer/external/libvorbisidec-1.2.1/window.c
    SDL2_mixer/external/libvorbisidec-1.2.1/synthesis.c
    SDL2_mixer/external/libvorbisidec-1.2.1/info.c
    SDL2_mixer/external/libvorbisidec-1.2.1/floor1.c
    SDL2_mixer/external/libvorbisidec-1.2.1/floor0.c
    SDL2_mixer/external/libvorbisidec-1.2.1/vorbisfile.c
    SDL2_mixer/external/libvorbisidec-1.2.1/res012.c
    SDL2_mixer/external/libvorbisidec-1.2.1/mapping0.c
    SDL2_mixer/external/libvorbisidec-1.2.1/registry.c
    SDL2_mixer/external/libvorbisidec-1.2.1/codebook.c
    SDL2_mixer/external/libvorbisidec-1.2.1/sharedbook.c
    SDL2_mixer/external/libogg-1.3.1/src/framing.c
    SDL2_mixer/external/libogg-1.3.1/src/bitwise.c
    )

set(SDL2_MIXER_SOURCES
    ${SDL2_MIXER_BASE}
    ${SDL2_MIXER_OGG_FILES}
    )

add_library(SDL2_mixer
            STATIC
            ${SDL2_MIXER_SOURCES})

target_link_libraries(SDL2_mixer
    SDL2)

target_include_directories(SDL2_mixer PUBLIC
        SDL2_mixer)

target_include_directories(SDL2_mixer PRIVATE
        SDL2_mixer/external/libvorbisidec-1.2.1
        SDL2_mixer/external/libogg-1.3.1/include)

set_target_properties(SDL2_mixer
    PROPERTIES
    COMPILE_FLAGS
    "-DOGG_MUSIC -DOGG_USE_TREMOR -DOGG_HEADER=\"<ivorbisfile.h>\"\
    -DWAV_MUSIC")
