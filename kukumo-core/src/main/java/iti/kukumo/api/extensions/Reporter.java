package iti.kukumo.api.extensions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeDescriptor;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.util.ThrowableFunction;

/**
 * @author ITI
 *         Created by ITI on 2/01/19
 */
@ExtensionPoint
public interface Reporter extends Contributor {

    
    /**
     * Perform the report operation on the given plan node descriptor. 
     * @param rootNode The root node descriptor. It may be a standalone plan or a root node grouping several plans.
     */
    public void report(PlanNodeDescriptor rootNode);
    
    
    
}
