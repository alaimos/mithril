package com.alaimos.MITHrIL.api.Data.Records.MiRNA;

import java.util.Objects;

public class MiRNATarget {

    private final String id;
    private final String name;
    private final String species;
    private final EvidenceType evidenceType;

//    @Contract("null -> fail")
//    public static MiRNATarget fromSplitString(String[] s) {
//        if (s == null || s.length < 8 || s[1].isEmpty() || s[4].isEmpty()) {
//            throw new RuntimeException("Source string in not correctly formatted");
//        }
//        return new MiRNATarget(s[1], s[5], s[3], s[4], s[6], s[7]);
//    }

    public MiRNATarget(String id, String name, String species, String evidenceType) {
        this(id, name, species, EvidenceType.fromString(evidenceType));
    }

    public MiRNATarget(String id, String name, String species, EvidenceType evidenceType) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.evidenceType = evidenceType;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String species() {
        return species;
    }

    public EvidenceType evidenceType() {
        return evidenceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiRNATarget that)) return false;
        return Objects.equals(id, that.id) && evidenceType == that.evidenceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, evidenceType);
    }
}
