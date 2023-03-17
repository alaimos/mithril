package com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.NodeType;
import com.alaimos.MITHrIL.api.Data.Records.TextFileRecordRepository;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This repository contains all the node types
 *
 * @author alaimos
 */
public class NodeTypeRepository extends TextFileRecordRepository<NodeType> {

    private static final NodeTypeRepository INSTANCE = new NodeTypeRepository();

    public static NodeTypeRepository getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        File f = getFileObject();
        if (!f.exists()) {
            Utils.download(Constants.INSTANCE.get("commons_node_type"), f);
        }
    }

    @Override
    protected NodeType newInstance(String key, String @NotNull [] data) {
        double sign = 0.0;
        if (data.length >= 1) {
            sign = Double.parseDouble(data[0]);
        }
        return new NodeType(key, sign);
    }

    @Override
    protected Class<NodeType> getRecordClass() {
        return NodeType.class;
    }

    @Override
    protected String getKey(@NotNull NodeType record) {
        return record.name();
    }

    @Override
    protected String getDefaultValue() {
        return "OTHER";
    }
}
