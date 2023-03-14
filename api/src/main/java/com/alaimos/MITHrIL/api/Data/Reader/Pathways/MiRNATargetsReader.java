package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteTextFileReader;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.MiRNA;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.MiRNATarget;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.MiRNAContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class MiRNATargetsReader implements DataReaderInterface<MiRNAContainer> {

    private final RemoteTextFileReader reader = new RemoteTextFileReader();

    public MiRNATargetsReader(@NotNull String url) {
        reader.separator("\t").fieldCountLimit(8).url(url).persisted(true);
    }

    @Override
    public String file() {
        return reader.file();
    }

    @Override
    public DataReaderInterface<MiRNAContainer> file(String f) {
        reader.file(f);
        return this;
    }

    @Override
    public DataReaderInterface<MiRNAContainer> file(File f) {
        reader.file(f);
        return this;
    }

    @Contract("_ -> new")
    private static @NotNull MiRNATarget makeTargetFromStringArray(String @NotNull [] s) {
        return new MiRNATarget(s[1], s[5], s[3], s[4]);
    }

    private static @Nullable MiRNA makeMiRNAFromStringArray(String @Nullable [] s) {
        if (s == null || s.length < 8 || s[0].isEmpty() || s[1].isEmpty() || s[2].isEmpty() || s[4].isEmpty()) {
            return null;
        }
        return new MiRNA(s[0], s[2]);
    }

    @Override
    public MiRNAContainer read() throws IOException {
        reader.file("mirna-targets-" + IOUtils.getName(reader.url()));
        var data = reader.read();
        MiRNAContainer miRNAs = new MiRNAContainer();
        for (var d : data) {
            var m = makeMiRNAFromStringArray(d);
            if (m != null) {
                miRNAs.add(m);
                miRNAs.get(m.id()).addTarget(makeTargetFromStringArray(d));
            }
        }
        return miRNAs;
    }
}
