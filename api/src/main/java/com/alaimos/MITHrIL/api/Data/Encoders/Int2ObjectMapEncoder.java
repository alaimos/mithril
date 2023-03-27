package com.alaimos.MITHrIL.api.Data.Encoders;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.Serial;
import java.io.Serializable;

public class Int2ObjectMapEncoder {

    @SuppressWarnings("unchecked")
    public static <E> Int2ObjectEncodedMap<E> encode(Int2ObjectMap<E> map) {
        if (map == null) return null;
        var keys = new int[map.size()];
        var values = (E[]) new Object[map.size()];
        var i = 0;
        for (var e : map.int2ObjectEntrySet()) {
            keys[i]   = e.getIntKey();
            values[i] = e.getValue();
            i++;
        }
        return new Int2ObjectEncodedMap<>(keys, values);
    }

    @SuppressWarnings("unchecked")
    public static <E> Int2ObjectMap<E> decode(Object object) {
        if (object == null) return null;
        if (!(object instanceof Int2ObjectEncodedMap<?> encodedMap)) return null;
        var m = new Int2ObjectOpenHashMap<E>(encodedMap.keys.length);
        for (var i = 0; i < encodedMap.keys.length; i++) {
            m.put(encodedMap.keys[i], (E) encodedMap.values[i]);
        }
        return m;
    }

    public record Int2ObjectEncodedMap<E>(int[] keys, E[] values) implements Serializable {
        @Serial
        private static final long serialVersionUID = 8128179574146138977L;
    }
}
