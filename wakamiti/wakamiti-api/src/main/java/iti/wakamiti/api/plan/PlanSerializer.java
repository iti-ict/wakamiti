/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.plan;


import iti.wakamiti.api.util.ThrowableFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;


public interface PlanSerializer {


    /**
     * Deserialize the given string into a {@link PlanNodeSnapshot} object
     *
     * @throws IOException
     */
    PlanNodeSnapshot deserialize(String json) throws IOException;

    /**
     * Serialize the given
     *
     * @throws IOException
     */
    String serialize(PlanNodeSnapshot node) throws IOException;


    void write(Writer writer, PlanNodeSnapshot node) throws IOException;


    PlanNodeSnapshot read(Reader reader) throws IOException;





    /**
     * Serialize the given iti.wakamiti.test.gherkin.plan or
     * iti.wakamiti.test.gherkin.plan node into a string
     */
    default String serialize(PlanNode node) throws IOException {
        return serialize(new PlanNodeSnapshot(node));
    }


    default void write(Writer writer, PlanNode node) throws IOException {
        write(writer, new PlanNodeSnapshot(node));
    }


    default PlanNodeSnapshot read(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
            inputStream, StandardCharsets.UTF_8
        )) {
            return read(reader);
        }
    }


    default PlanNodeSnapshot read(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream);
        }
    }


    default PlanNodeSnapshot read(Path path) throws IOException {
        try (InputStream stream = new FileInputStream(path.toFile())) {
            return read(stream);
        }
    }



    default Collection<PlanNodeSnapshot> read(Collection<Path> paths) throws IOException {
        return paths.stream().map(ThrowableFunction.unchecked(path -> read(path)))
            .collect(Collectors.toList());
    }

}