package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteTextFileReader;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.MiRNAContainer;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.MiRNATranscriptionFactor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class TranscriptionFactorsReader implements DataReaderInterface<MiRNAContainer> {

    private final RemoteTextFileReader reader = new RemoteTextFileReader();
    private final MiRNAContainer container;

    public TranscriptionFactorsReader(@NotNull String url, MiRNAContainer container) {
        reader.separator("\t").fieldCountLimit(5).url(url).persisted(true);
        this.container = container;
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

    private static @Nullable MiRNATranscriptionFactor fromStringArray(String @Nullable [] s) {
        if (s == null || s.length < 5 || s[0].isEmpty() || s[1].isEmpty()) {
            return null;
        }
        return new MiRNATranscriptionFactor(s[0], s[2], s[3]);
    }

    @Override
    public MiRNAContainer read() throws IOException {
        reader.file("mirna-tfs-" + IOUtils.getName(reader.url()));
        var data = reader.read();
        for (var d : data) {
            if (!d[1].isEmpty()) {
                if (container.containsKey(d[1])) {
                    container.get(d[1]).addTranscriptionFactor(fromStringArray(d));
                }
            }
        }
        return container;
    }
}
