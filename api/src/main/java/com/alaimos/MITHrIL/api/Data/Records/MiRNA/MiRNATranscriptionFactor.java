package com.alaimos.MITHrIL.api.Data.Records.MiRNA;

import java.util.Objects;

public class MiRNATranscriptionFactor {

    private final String id;
    private final String type;
    private final EvidenceType evidenceType;

    public MiRNATranscriptionFactor(String id, String type, String evidenceType) {
        this.id           = id;
        this.type         = type;
        this.evidenceType = EvidenceType.fromString(evidenceType);
    }

    public MiRNATranscriptionFactor(String id, String type, EvidenceType evidenceType) {
        this.id           = id;
        this.type         = type;
        this.evidenceType = evidenceType;
    }

    public String id() {
        return id;
    }

    public EvidenceType evidenceType() {
        return evidenceType;
    }

    public String type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiRNATranscriptionFactor tf)) return false;
        return Objects.equals(id, tf.id) && type.equalsIgnoreCase(tf.type) && evidenceType == tf.evidenceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, evidenceType);
    }
}
