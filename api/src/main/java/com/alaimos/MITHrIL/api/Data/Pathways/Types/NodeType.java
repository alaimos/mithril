package com.alaimos.MITHrIL.api.Data.Pathways.Types;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories.NodeTypeRepository;

import java.io.Serializable;

/**
 * This enum represents the type of node in a pathway
 *
 * @author alaimos
 * @version 3.0.0.0
 */
public record NodeType(String name, double sign) implements Serializable {

    public static NodeType add(String name, double sign) {
        return NodeTypeRepository.getInstance().add(new NodeType(name, sign));
    }

    public static NodeType valueOf(String name) {
        return NodeTypeRepository.getInstance().valueOf(name);
    }

    public static NodeType fromString(String name) {
        return NodeTypeRepository.getInstance().fromString(name);
    }

    public static NodeType[] values() {
        return NodeTypeRepository.getInstance().values();
    }

}
