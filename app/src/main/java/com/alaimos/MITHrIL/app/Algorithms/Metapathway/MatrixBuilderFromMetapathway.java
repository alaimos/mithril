package com.alaimos.MITHrIL.app.Algorithms.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Pathways.PathwayMatrixBuilder;
import com.alaimos.MITHrIL.app.Data.Pathways.RepositoryMatrixBuilder;
import com.alaimos.MITHrIL.app.Data.Reader.RepositoryMatrixReader;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Data.Writer.RepositoryMatrixWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MatrixBuilderFromMetapathway implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MatrixBuilderFromMetapathway.class);
    private final Repository repository;
    private final MatrixFactoryInterface<?> matrixFactory;
    private RepositoryMatrix matrix = null;

    public MatrixBuilderFromMetapathway(Repository repository, MatrixFactoryInterface<?> matrixFactory) {
        this.repository    = repository;
        this.matrixFactory = matrixFactory;
    }

    /**
     * Build the matrix representation of a repository
     *
     * @param repository    repository
     * @param matrixFactory matrix factory
     * @return the matrix representation
     */
    public static RepositoryMatrix build(Repository repository, MatrixFactoryInterface<?> matrixFactory) {
        var builder = new MatrixBuilderFromMetapathway(repository, matrixFactory);
        builder.run();
        return builder.get();
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            log.info("Looking for cached matrix representation of the metapathway");
            var reader = new RepositoryMatrixReader(repository, matrixFactory);
            log.debug("The matrix representation is stored in {}", reader.file());
            try {
                matrix = reader.read();
                log.info("Matrix representation found, using it");
            } catch (IOException e) {
                log.info("Matrix representation not found, building it");
                log.info("Building metapathway matrix");
                var metapathwayMatrix = new PathwayMatrixBuilder(matrixFactory).build(repository.get());
                log.info("Inverting metapathway matrix");
                metapathwayMatrix.matrix().invertInPlace();
                log.info("Building repository matrix");
                matrix = new RepositoryMatrixBuilder(matrixFactory).build(repository, metapathwayMatrix);
                log.info("Saving matrix representation");
                new RepositoryMatrixWriter(repository).write(matrix);
            }
            log.info("Matrix representation ready");
        } catch (Throwable e) {
            log.error("An error occurred while building the matrix representation", e);
        }
    }

    /**
     * Returns the metapathway
     *
     * @return the metapathway
     */
    public RepositoryMatrix get() {
        if (matrix == null) run();
        return matrix;
    }
}
