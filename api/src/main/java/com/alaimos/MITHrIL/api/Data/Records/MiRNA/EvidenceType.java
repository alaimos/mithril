package com.alaimos.MITHrIL.api.Data.Records.MiRNA;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.jetbrains.annotations.Contract;

/**
 * Strength of biological evidence
 */
public enum EvidenceType {
    /**
     * Strong evidence
     */
    STRONG(0),

    /**
     * Weak evidence
     */
    WEAK(1),

    /**
     * Computational prediction
     */
    PREDICTION(2),

    /**
     * Unknown evidence
     */
    UNKNOWN(3);

    private final int value;

    private EvidenceType(int value) {
        this.value = value;
    }

    @Contract(pure = true)
    public int value() {
        return value;
    }

    public static EvidenceType fromString(String name) {
        return Utils.getEnumFromString(EvidenceType.class, name, UNKNOWN);
    }
}
