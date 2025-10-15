package src.java.GUI;

import java.util.List;

/**
 * This class provides methods for encoding a sequence of LatLng coordinates into an encoded polyline string.
 * The algorithm for encoding the polyline is adapted from Google Maps Services Java library:
 * https://github.com/googlemaps/google-maps-services-java/blob/master/src/main/java/com/google/maps/internal/PolylineEncoding.java
 */
public class PolylineEncoder {

    /**
     * Encodes a sequence of Place coordinates into an encoded polyline string.
     *
     * @param path A list of Place objects representing the coordinates of the polyline path.
     * @return The encoded polyline string.
     */
    public static String encode(List<Place> path) {
        long lastLat = 0;
        long lastLng = 0;

        StringBuilder result = new StringBuilder();

        for (Place place : path) {
            long lat = Math.round(place.getLatitude() * 1e5);
            long lng = Math.round(place.getLongitude() * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encodeValue(dLat, result);
            encodeValue(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    /**
     * Encodes a single coordinate value into an encoded polyline.
     *
     * @param value  The coordinate value to be encoded.
     * @param result The StringBuilder to which the encoded value will be appended.
     */
    private static void encodeValue(long value, StringBuilder result) {
        // Shift the value left by 1 and if it's negative, invert it
        value = value < 0 ? ~(value << 1) : value << 1;

        // While value is greater than or equal to 0x20, take the lowest 5 bits of it and add 0x20 to them
        while (value >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (value & 0x1f)) + 63)));
            value >>= 5;
        }

        // The lowest 5 bits of the final value are stored directly
        result.append(Character.toChars((int) (value + 63)));
    }

}

