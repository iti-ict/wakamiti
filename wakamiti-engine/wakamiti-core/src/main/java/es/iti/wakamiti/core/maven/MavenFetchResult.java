/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;


import java.util.stream.Stream;

/**
 * This interface exposes the results of a fetch operation
 */
public interface MavenFetchResult {

    /** @return A new stream with the fetched artifacts requested */
    Stream<FetchedArtifact> artifacts();

    /** @return A new stream with all fetched artifacts, includind dependencies */
    Stream<FetchedArtifact> allArtifacts();

    /** @return <tt>true</tt> if any error has ocurred during the fetching */
    boolean hasErrors();

    /**
     * @return A new stream with all the errors ocurred
     */
    Stream<Exception> errors();

}
