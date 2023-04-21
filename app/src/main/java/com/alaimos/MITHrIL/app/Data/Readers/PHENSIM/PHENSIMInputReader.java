package com.alaimos.MITHrIL.app.Data.Readers.PHENSIM;

import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionDirection;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionDistribution;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class PHENSIMInputReader extends AbstractDataReader<ExpressionConstraint[]> {

    private final boolean enableNewInputFormat;

    public PHENSIMInputReader(boolean enableNewInputFormat) {
        this.enableNewInputFormat = enableNewInputFormat;
    }

    public PHENSIMInputReader() {
        this(true);
    }

    @Override
    public PHENSIMInputReader file(@NotNull File f) {
        file      = f;
        isGzipped = f.getName().endsWith(".gz");
        return this;
    }

    @Override
    protected ExpressionConstraint[] realReader() throws IOException {
        isGzipped = file.getName().endsWith(".gz");
        var expressionConstraints = new ArrayList<ExpressionConstraint>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            String[] s;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                s = line.split("\t", -1);
                if (s.length < 2) continue;
                String node = s[0].trim();
                if (node.isEmpty()) continue;
                var direction = ExpressionDirection.NONE;
                var baseLog2FC = Double.NaN;
                ExpressionDistribution distribution = null;
                if (enableNewInputFormat) {
                    if (NumberUtils.isCreatable(s[1])) {
                        baseLog2FC = NumberUtils.createDouble(s[1]);
                    } else {
                        direction = ExpressionDirection.fromString(s[1]);
                    }
                    if (s.length >= 4) {
                        distribution = new ExpressionDistribution(
                                NumberUtils.createDouble(s[2]), NumberUtils.createDouble(s[3])
                        );
                    }
                } else {
                    direction = ExpressionDirection.fromString(s[1]);
                }
                var constraint = new ExpressionConstraint(
                        node, direction, distribution, baseLog2FC
                );
                expressionConstraints.add(constraint);
            }
        }
        return expressionConstraints.toArray(new ExpressionConstraint[0]);
    }
}
