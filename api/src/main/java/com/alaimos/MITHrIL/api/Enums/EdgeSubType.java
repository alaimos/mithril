package com.alaimos.MITHrIL.api.Enums;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Enums.DynamicEnums.TextFileDynamicEnum;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serial;

/**
 * This enum represents the subtype of edge in a pathway
 *
 * @author alaimos
 * @version 2.0.0.0
 * @since 06/12/2015
 */
public class EdgeSubType extends TextFileDynamicEnum {
    @Serial
    private static final long serialVersionUID = 4786068670560829228L;

    static {
        File f = getFileObject(EdgeSubType.class);
        if (!f.exists()) {
            Utils.download(Constants.COMMONS_EDGE_SUBTYPE, f);
        }
        init(EdgeSubType.class);
    }

    private double weight = 0.0;
    private int priority = 0;
    private String symbol = "";

    protected EdgeSubType(int ordinal, String name) {
        super(ordinal, name);
    }

    protected EdgeSubType(int ordinal, String name, String @NotNull [] others) {
        super(ordinal, name);
        if (others.length >= 1) {
            weight = Double.parseDouble(others[0]);
            if (others.length >= 2) {
                priority = Integer.parseInt(others[1]);
                if (others.length >= 3) {
                    symbol = others[2];
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static EdgeSubType valueOf(String name) {
        return (EdgeSubType) valueOf(EdgeSubType.class, name);
    }

    public static EdgeSubType[] values() {
        return values(EdgeSubType.class);
    }

    @SuppressWarnings("unchecked")
    public static EdgeSubType fromString(String name) {
        return (EdgeSubType) fromString(EdgeSubType.class, name, "UNKNOWN");
    }

    @Contract(pure = true)
    public double weight() {
        return weight;
    }

    @Contract(pure = true)
    public int priority() {
        return priority;
    }

    @Contract(pure = true)
    public String symbol() {
        return symbol;
    }

    /**
     * Add a new element to this enum
     *
     * @param name the name of the new element
     * @return the added element
     */
    public static EdgeSubType add(String name) {
        return add(EdgeSubType.class, name);
    }

    /**
     * Add a new element to this enum
     *
     * @param name   the name of the new element
     * @param others other parameters for the element
     * @return the added element
     */
    @Nullable
    public static EdgeSubType add(String name, String[] others) {
        return add(EdgeSubType.class, name, others);
    }

    /**
     * Add an edge subtype
     *
     * @param name     the name of the subtype
     * @param weight   the weight of this subtype
     * @param priority the priority of this subtype
     * @param symbol   the symbol that represents this subtype
     * @return the added subtype
     */
    @Nullable
    public static EdgeSubType add(String name, double weight, int priority, String symbol) {
        return add(EdgeSubType.class, name, new String[]{
                Double.toString(weight),
                Integer.toString(priority),
                symbol
        });
    }

}
