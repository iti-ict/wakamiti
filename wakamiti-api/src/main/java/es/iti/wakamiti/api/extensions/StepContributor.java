/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.commons.jext.LoadStrategy;


/**
 * This interface extends {@link Contributor} and serves as
 * an ExtensionPoint for implementing an Extension step provider.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 * @see Contributor
 */
@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface StepContributor extends Contributor {

}