package com.alaimos.MITHrIL.app.Algorithms.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Pathways.PathwayMatrixBuilder;
import com.alaimos.MITHrIL.app.Data.Pathways.RepositoryMatrixBuilder;
import com.alaimos.MITHrIL.app.Data.Readers.RepositoryMatrixReader;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Data.Writers.RepositoryMatrixWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MatrixBuilderFromMetapathway implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MatrixBuilderFromMetapathway.class);
    public static boolean USE_CACHE = true;
    private final Repository repository;
    private final MatrixFactoryInterface<?> matrixFactory;
    private final boolean customizeMatrixToInput;
    private final List<String> customizationNodes;
    private RepositoryMatrix matrix = null;

    @Contract(pure = true)
    public MatrixBuilderFromMetapathway(Repository repository, MatrixFactoryInterface<?> matrixFactory) {
        this.repository             = repository;
        this.matrixFactory          = matrixFactory;
        this.customizeMatrixToInput = false;
        this.customizationNodes     = null;
    }

    @Contract(pure = true)
    public MatrixBuilderFromMetapathway(
            Repository repository, MatrixFactoryInterface<?> matrixFactory, @NotNull List<String> customizationNodes
    ) {
        this.repository             = repository;
        this.matrixFactory          = matrixFactory;
        this.customizationNodes     = customizationNodes;
        this.customizeMatrixToInput = !customizationNodes.isEmpty();
    }

    /**
     * Build the matrix representation of a repository
     *
     * @param repository    repository
     * @param matrixFactory matrix factory
     * @return the matrix representation
     */
    public static RepositoryMatrix build(
            @NotNull Repository repository,
            @NotNull MatrixFactoryInterface<?> matrixFactory
    ) {
        var builder = new MatrixBuilderFromMetapathway(repository, matrixFactory);
        builder.run();
        return builder.get();
    }

    /**
     * Build the matrix representation of a repository customized to a set of nodes. This method will build a matrix
     * representation of the metapathway, invert it, and then build the repository matrix. The matrix representation
     * will be saved in a file for future use. If the matrix representation is already saved, it will be loaded from the
     * file. The customization is done by traversing the metapathway from the set of customization nodes to find all the
     * nodes that are reachable from them.
     *
     * @param repository         repository
     * @param matrixFactory      matrix factory
     * @param customizationNodes a set of nodes for matrix customization
     * @return the matrix representation
     */
    public static RepositoryMatrix build(
            @NotNull Repository repository,
            @NotNull MatrixFactoryInterface<?> matrixFactory,
            @NotNull List<String> customizationNodes
    ) {
        var builder = new MatrixBuilderFromMetapathway(repository, matrixFactory, customizationNodes);
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
            var reader = new RepositoryMatrixReader(
                    repository,
                    matrixFactory,
                    customizeMatrixToInput,
                    customizationNodes != null ? customizationNodes.hashCode() : 0
            );
            log.debug("The matrix representation is stored in {}", reader.file());
            try {
                if (!USE_CACHE) throw new IOException("Cache disabled");
                matrix = reader.read();
                log.info("Matrix representation found, using it");
            } catch (IOException e) {
                log.debug("Matrix representation not found", e);
                log.info("Matrix representation not found, building it");
                log.info("Building metapathway matrix");
                var metapathwayMatrix = new PathwayMatrixBuilder(matrixFactory).build(
                        repository.get(),
                        customizeMatrixToInput,
                        customizationNodes
                );
                log.info("Inverting metapathway matrix");
                metapathwayMatrix.matrix().invertInPlace();
                log.info("Building repository matrix");
                matrix = new RepositoryMatrixBuilder(matrixFactory).build(repository, metapathwayMatrix);
                log.info("Saving matrix representation");
                new RepositoryMatrixWriter(
                        repository,
                        customizeMatrixToInput,
                        customizationNodes != null ? customizationNodes.hashCode() : 0
                ).write(matrix);
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
