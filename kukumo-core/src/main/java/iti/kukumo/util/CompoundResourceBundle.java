package iti.kukumo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author ITI
 *         Created by ITI on 8/01/19
 */
public class CompoundResourceBundle extends ResourceBundle {

    private final List<ResourceBundle> resourceBundles;

    public CompoundResourceBundle(List<ResourceBundle> resourceBundles) {
        this.resourceBundles = new ArrayList<>(resourceBundles);
    }


    @Override
    protected Object handleGetObject(String key) {
        for (ResourceBundle resourceBundle : resourceBundles) {
            Object value = resourceBundle.getObject(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }


    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<>();
        for (ResourceBundle resourceBundle : resourceBundles) {
            keys.addAll(resourceBundle.keySet());
        }
        return Collections.enumeration(keys);
    }



}
