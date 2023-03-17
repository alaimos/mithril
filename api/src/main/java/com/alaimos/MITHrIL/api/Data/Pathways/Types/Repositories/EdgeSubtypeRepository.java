package com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeSubtype;
import com.alaimos.MITHrIL.api.Data.Records.TextFileRecordRepository;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This repository contains all the edge subtypes
 *
 * @author alaimos
 */
public class EdgeSubtypeRepository extends TextFileRecordRepository<EdgeSubtype> {

    private static final EdgeSubtypeRepository INSTANCE = new EdgeSubtypeRepository();

    public static EdgeSubtypeRepository getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        File f = getFileObject();
        if (!f.exists()) {
            Utils.download(Constants.INSTANCE.get("commons_edge_subtype"), f);
        }
    }

    @Override
    protected EdgeSubtype newInstance(String key, String @NotNull [] data) {
        double weight = 0.0;
        int priority = 0;
        String symbol = "";
        if (data.length >= 1) {
            weight = Double.parseDouble(data[0]);
            if (data.length >= 2) {
                priority = Integer.parseInt(data[1]);
                if (data.length >= 3) {
                    symbol = data[2];
                }
            }
        }
        return new EdgeSubtype(key, weight, priority, symbol);
    }

    @Override
    protected Class<EdgeSubtype> getRecordClass() {
        return EdgeSubtype.class;
    }

    @Override
    protected String getKey(@NotNull EdgeSubtype record) {
        return record.name();
    }

    @Override
    protected String getDefaultValue() {
        return "UNKNOWN";
    }
}
