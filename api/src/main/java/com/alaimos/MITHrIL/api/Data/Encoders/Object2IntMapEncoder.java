package com.alaimos.MITHrIL.api.Data.Encoders;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.Serial;
import java.io.Serializable;

public class Object2IntMapEncoder {

    @SuppressWarnings("unchecked")
    public static <E> Object2IntEncodedMap<E> encode(Object2IntMap<E> map) {
        if (map == null) return null;
        var keys = (E[]) new Object[map.size()];
        var values = new int[map.size()];
        var i = 0;
        for (var e : map.object2IntEntrySet()) {
            keys[i]   = e.getKey();
            values[i] = e.getIntValue();
            i++;
        }
        return new Object2IntEncodedMap<>(keys, values);
    }

    @SuppressWarnings("unchecked")
    public static <E> Object2IntMap<E> decode(Object object) {
        if (object == null) return null;
        if (!(object instanceof Object2IntEncodedMap<?> encodedMap)) return null;
        var m = new Object2IntOpenHashMap<E>(encodedMap.keys.length);
        for (var i = 0; i < encodedMap.keys.length; i++) {
            m.put((E) encodedMap.keys[i], encodedMap.values[i]);
        }
        return m;
    }

    private record Object2IntEncodedMap<E>(E[] keys, int[] values) implements Serializable {
        @Serial
        private static final long serialVersionUID = 8128179574146138977L;
    }
}
