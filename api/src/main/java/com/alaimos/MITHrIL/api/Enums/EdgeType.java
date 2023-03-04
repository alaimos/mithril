package com.alaimos.MITHrIL.api.Enums;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Enums.DynamicEnums.TextFileDynamicEnum;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;

/**
 * This enum represents the type of edge in a pathway
 *
 * @author alaimos
 * @version 2.0.0.0
 * @since 06/12/2015
 */
public class EdgeType extends TextFileDynamicEnum {
    @Serial
    private static final long serialVersionUID = 5286380910417889552L;

    static {
        File f = getFileObject(EdgeType.class);
        if (!f.exists()) {
            Utils.download(Constants.COMMONS_EDGE_TYPE, f);
        }
        init(EdgeType.class);
    }

    protected EdgeType(int ordinal, String name) {
        super(ordinal, name);
    }

    @SuppressWarnings("unchecked")
    public static EdgeType valueOf(String name) {
        return (EdgeType) valueOf(EdgeType.class, name);
    }

    public static EdgeType[] values() {
        return values(EdgeType.class);
    }

    @SuppressWarnings("unchecked")
    public static EdgeType fromString(String name) {
        return (EdgeType) fromString(EdgeType.class, name, "OTHER");
    }

    /**
     * Add a new element to this enum
     *
     * @param name the name of the new element
     * @return the added element
     */
    public static EdgeType add(String name) {
        return add(EdgeType.class, name);
    }

    /**
     * Add a new element to this enum
     *
     * @param name   the name of the new element
     * @param others other parameters for the element
     * @return the added element
     */
    @Nullable
    public static EdgeType add(String name, String[] others) {
        return add(EdgeType.class, name, others);
    }

}
