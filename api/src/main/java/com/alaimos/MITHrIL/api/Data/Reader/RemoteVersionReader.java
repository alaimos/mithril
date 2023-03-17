package com.alaimos.MITHrIL.api.Data.Reader;

import com.alaimos.MITHrIL.api.Commons.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RemoteVersionReader extends AbstractRemoteDataReader<String> {

    public RemoteVersionReader() {
        init();
    }

    private void init() {
        setGzipped(false);
        persisted(true).url(Constants.INSTANCE.get("mithril_version_url")).file(Constants.INSTANCE.get("mithril_version_file"));
    }

    @Override
    protected String realReader() {
        //Ensures that all parameters are correctly set
        init();
        //Start Reading
        String line;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            line = r.readLine();
            if (line == null) {
                throw new RuntimeException("Cannot gather current MITHrIL version.");
            } else {
                line = line.trim();
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return line;
    }
}
