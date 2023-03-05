package com.alaimos.MITHrIL.api.Data.Pathways.Types;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories.EdgeSubtypeRepository;

import java.io.Serializable;

/**
 * This enum represents the subtype of edge in a pathway
 *
 * @author alaimos
 * @version 3.0.0.0
 */
public record EdgeSubtype(String name, double weight, int priority, String symbol) implements Serializable {

    public static EdgeSubtype add(String name, double weight, int priority, String symbol) {
        return EdgeSubtypeRepository.getInstance().add(new EdgeSubtype(name, weight, priority, symbol));
    }

    public static EdgeSubtype valueOf(String name) {
        return EdgeSubtypeRepository.getInstance().valueOf(name);
    }

    public static EdgeSubtype fromString(String name) {
        return EdgeSubtypeRepository.getInstance().fromString(name);
    }

    public static EdgeSubtype[] values() {
        return EdgeSubtypeRepository.getInstance().values();
    }

}
