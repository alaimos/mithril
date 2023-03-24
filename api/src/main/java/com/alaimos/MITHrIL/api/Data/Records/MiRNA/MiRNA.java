package com.alaimos.MITHrIL.api.Data.Records.MiRNA;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MiRNA {

    private final String id;
    private final String species;
    private final Set<MiRNATarget> targets;
    private final Set<MiRNATranscriptionFactor> transcriptionFactors;

//    @Contract("null -> fail")
//    public static miRNA fromSplitString(String[] s) {
//        if (s == null || s.length < 8 || s[0].isEmpty() || s[2].isEmpty()) {
//            throw new RuntimeException("Source string in not correctly formatted");
//        }
//        return (new miRNA(s[0], s[2])).addTarget(s);
//    }

    public MiRNA(String id, String species) {
        this.id                   = id;
        this.species              = species;
        this.targets              = new HashSet<>();
        this.transcriptionFactors = new HashSet<>();
    }

    public String id() {
        return id;
    }

    public String species() {
        return species;
    }

    public Set<MiRNATarget> targets() {
        return targets;
    }

    public boolean containsTarget(MiRNATarget target) {
        return this.targets.contains(target);
    }

    public void addTarget(MiRNATarget target) {
        targets.add(target);
    }

    public Set<MiRNATranscriptionFactor> transcriptionFactors() {
        return transcriptionFactors;
    }

    public void addTranscriptionFactor(MiRNATranscriptionFactor transcriptionFactor) {
        transcriptionFactors.add(transcriptionFactor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiRNA miRNA)) return false;
        return Objects.equals(id, miRNA.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
