package com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeType;
import com.alaimos.MITHrIL.api.Data.Records.TextFileRecordRepository;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This repository contains all the node types
 *
 * @author alaimos
 */
public class EdgeTypeRepository extends TextFileRecordRepository<EdgeType> {

    private static final EdgeTypeRepository INSTANCE = new EdgeTypeRepository();

    public static EdgeTypeRepository getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        File f = getFileObject();
        if (!f.exists()) {
            Utils.download(Constants.COMMONS_EDGE_TYPE, f);
        }
    }

    @Override
    protected EdgeType newInstance(String key, String @NotNull [] data) {
        return new EdgeType(key);
    }

    @Override
    protected Class<EdgeType> getRecordClass() {
        return EdgeType.class;
    }

    @Override
    protected String getKey(@NotNull EdgeType record) {
        return record.name();
    }

    @Override
    protected String getDefaultValue() {
        return "OTHER";
    }
}
