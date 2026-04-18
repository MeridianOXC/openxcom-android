/**
 * Compatibility shim for libc++ 18+ (NDK r27+).
 *
 * libc++ 18 removed non-standard char_traits specializations for types other
 * than char, wchar_t, char8_t, char16_t, and char32_t. OpenXcom depends on
 * two of the removed specializations:
 *
 *   - char_traits<signed char>   used by ModInfoNormalizedVersion
 *     (Engine/ModInfo.h: basic_string<signed char>)
 *
 *   - char_traits<unsigned int>  used by UString
 *     (Engine/Unicode.h: basic_string<Uint32> where Uint32 = uint32_t = unsigned int)
 *
 * This header is force-included at the top of every translation unit via
 * CMakeLists.txt (-include flag) so the specializations are visible before
 * any OpenXcom header that uses these types.
 */
#pragma once

#include <string>
#include <cstring>
#include <cstdint>
#include <iosfwd>

#if defined(__ANDROID__) && defined(__clang__)

namespace std {

// ---------------------------------------------------------------------------
// char_traits<signed char>
// ---------------------------------------------------------------------------
template<>
struct char_traits<signed char>
{
    using char_type  = signed char;
    using int_type   = int;
    using pos_type   = streampos;
    using off_type   = streamoff;
    using state_type = mbstate_t;

    static void assign(char_type& c1, const char_type& c2) noexcept { c1 = c2; }
    static bool eq(char_type c1, char_type c2) noexcept { return c1 == c2; }
    static bool lt(char_type c1, char_type c2) noexcept {
        return (unsigned char)c1 < (unsigned char)c2;
    }
    static int compare(const char_type* s1, const char_type* s2, size_t n) noexcept {
        return n == 0 ? 0 : ::memcmp(s1, s2, n);
    }
    static size_t length(const char_type* s) noexcept {
        size_t i = 0; while (s[i]) ++i; return i;
    }
    static const char_type* find(const char_type* s, size_t n, const char_type& a) noexcept {
        return (const char_type*)::memchr(s, (unsigned char)a, n);
    }
    static char_type* move(char_type* s1, const char_type* s2, size_t n) noexcept {
        return (char_type*)::memmove(s1, s2, n);
    }
    static char_type* copy(char_type* s1, const char_type* s2, size_t n) noexcept {
        return (char_type*)::memcpy(s1, s2, n);
    }
    static void assign(char_type* s, size_t n, char_type a) noexcept {
        ::memset(s, (unsigned char)a, n);
    }
    static int_type not_eof(int_type c) noexcept { return c == eof() ? 0 : c; }
    static char_type to_char_type(int_type c) noexcept { return (char_type)c; }
    static int_type to_int_type(char_type c) noexcept { return (unsigned char)c; }
    static bool eq_int_type(int_type c1, int_type c2) noexcept { return c1 == c2; }
    static int_type eof() noexcept { return -1; }
};

// ---------------------------------------------------------------------------
// char_traits<unsigned int>  (Uint32 / uint32_t on arm64 Android)
// ---------------------------------------------------------------------------
template<>
struct char_traits<unsigned int>
{
    using char_type  = unsigned int;
    using int_type   = long long;
    using pos_type   = streampos;
    using off_type   = streamoff;
    using state_type = mbstate_t;

    static void assign(char_type& c1, const char_type& c2) noexcept { c1 = c2; }
    static bool eq(char_type c1, char_type c2) noexcept { return c1 == c2; }
    static bool lt(char_type c1, char_type c2) noexcept { return c1 < c2; }
    static int compare(const char_type* s1, const char_type* s2, size_t n) noexcept {
        for (size_t i = 0; i < n; ++i) {
            if (s1[i] < s2[i]) return -1;
            if (s1[i] > s2[i]) return  1;
        }
        return 0;
    }
    static size_t length(const char_type* s) noexcept {
        size_t i = 0; while (s[i]) ++i; return i;
    }
    static const char_type* find(const char_type* s, size_t n, const char_type& a) noexcept {
        for (size_t i = 0; i < n; ++i) if (s[i] == a) return s + i;
        return nullptr;
    }
    static char_type* move(char_type* s1, const char_type* s2, size_t n) noexcept {
        return (char_type*)::memmove(s1, s2, n * sizeof(char_type));
    }
    static char_type* copy(char_type* s1, const char_type* s2, size_t n) noexcept {
        return (char_type*)::memcpy(s1, s2, n * sizeof(char_type));
    }
    static void assign(char_type* s, size_t n, char_type a) noexcept {
        for (size_t i = 0; i < n; ++i) s[i] = a;
    }
    static int_type not_eof(int_type c) noexcept { return c == eof() ? 0 : c; }
    static char_type to_char_type(int_type c) noexcept { return (char_type)c; }
    static int_type to_int_type(char_type c) noexcept { return (int_type)c; }
    static bool eq_int_type(int_type c1, int_type c2) noexcept { return c1 == c2; }
    static int_type eof() noexcept { return (int_type)-1; }
};

} // namespace std

#endif // __ANDROID__ && __clang__
