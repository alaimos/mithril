package com.alaimos.MITHrIL.api.Data.Pathways.Types;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories.EdgeTypeRepository;

import java.io.Serializable;

/**
 * This enum represents the type of edge in a pathway
 *
 * @author alaimos
 * @version 3.0.0.0
 */
public record EdgeType(String name) implements Serializable {

    public static EdgeType add(String name) {
        return EdgeTypeRepository.getInstance().add(name);
    }

    public static EdgeType valueOf(String name) {
        return EdgeTypeRepository.getInstance().valueOf(name);
    }

    public static EdgeType fromString(String name) {
        return EdgeTypeRepository.getInstance().fromString(name);
    }

    public static EdgeType[] values() {
        return EdgeTypeRepository.getInstance().values();
    }

}
