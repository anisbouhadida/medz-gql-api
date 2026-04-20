package dz.anisbouhadida.medzgqlapi.application.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/// Utility class for encoding and decoding opaque pagination cursors.
///
/// Cursors are base64-encoded strings of the format {@code "offset:<absoluteOffset>"}.
/// They are intentionally opaque to API consumers.
///
/// @author Anis Bouhadida
/// @since 0.0.1
final class CursorUtils {

  private static final String PREFIX = "offset:";

  private CursorUtils() {}

  /// Encodes an absolute 0-based offset into an opaque cursor string.
  static String encode(long offset) {
    String raw = PREFIX + offset;
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  /// Decodes an opaque cursor string back to its absolute 0-based offset.
  ///
  /// @throws IllegalArgumentException if the cursor is malformed
  static long decode(String cursor) {
    try {
      String raw = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
      if (!raw.startsWith(PREFIX)) {
        throw new IllegalArgumentException("Invalid cursor: " + cursor);
      }
      return Long.parseLong(raw.substring(PREFIX.length()));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid cursor: " + cursor, e);
    }
  }
}

