/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html.factory;

import es.iti.wakamiti.api.WakamitiException;
import freemarker.core.CollectionAndSequence;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class SumAllMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {

        if (args.size() != 1 || !(args.get(0) instanceof CollectionAndSequence)) {
            throw new WakamitiException("Argument must be a number list");
        }
        return toStream(((CollectionAndSequence) args.get(0)).iterator()).mapToLong(Number::longValue).sum();
    }

    private Stream<Number> toStream(TemplateModelIterator iterator) throws TemplateModelException {
        List<Number> result = new LinkedList<>();
        while (iterator.hasNext()) {
            result.add(((SimpleNumber) iterator.next()).getAsNumber());
        }
        return result.stream();
    }
}
