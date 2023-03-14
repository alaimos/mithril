package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

public record Species(String id, String name, boolean hasMiRNA, boolean hasTranscriptionFactors, String pathwayUrl,
                      String miRNAUrl, String transcriptionFactorsUrl) {
}
