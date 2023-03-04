package com.alaimos.MITHrIL.api.Enums;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Enums.DynamicEnums.TextFileDynamicEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;

/**
 * This enum represents the type of node in a pathway
 *
 * @author alaimos
 * @version 2.0.0.0
 * @since 06/12/2015
 */
public class NodeType extends TextFileDynamicEnum {
    @Serial
    private static final long serialVersionUID = -5744625932116726682L;

    static {
        File f = getFileObject(NodeType.class);
        if (!f.exists()) {
            Utils.download(Constants.COMMONS_NODE_TYPE, f);
        }
        init(NodeType.class);
    }

    private double sign = 0.0;

    protected NodeType(int ordinal, String name) {
        super(ordinal, name);
    }

    protected NodeType(int ordinal, String name, String @NotNull [] others) {
        super(ordinal, name);
        if (others.length >= 1) {
            sign = Double.parseDouble(others[0]);
        }
    }

    public double sign() {
        return sign;
    }

    @SuppressWarnings("unchecked")
    public static NodeType valueOf(String name) {
        return (NodeType) valueOf(NodeType.class, name);
    }

    public static NodeType[] values() {
        return values(NodeType.class);
    }

    @SuppressWarnings("unchecked")
    public static NodeType fromString(String name) {
        return (NodeType) fromString(NodeType.class, name, "OTHER");
    }

    /**
     * Add a new element to this enum
     *
     * @param name the name of the new element
     * @return the added element
     */
    public static NodeType add(String name) {
        return add(NodeType.class, name);
    }

    /**
     * Add a new element to this enum
     *
     * @param name   the name of the new element
     * @param others other parameters for the element
     * @return the added element
     */
    @Nullable
    public static NodeType add(String name, String[] others) {
        return add(NodeType.class, name, others);
    }

    /**
     * Add a new node type
     *
     * @param name the name of this type
     * @param sign the sign used for the accumulator computation
     * @return the added type
     */
    @Nullable
    public static NodeType add(String name, double sign) {
        return add(NodeType.class, name, new String[]{Double.toString(sign)});
    }

}
