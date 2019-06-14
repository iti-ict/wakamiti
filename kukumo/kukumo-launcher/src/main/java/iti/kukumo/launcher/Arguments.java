package iti.kukumo.launcher;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;

/**
 * @author ITI
 * Created by ITI on 2/04/19
 */
public class Arguments {

    private boolean mustClean;
    private final Map<String,String> kukumoProperties = new HashMap<>();
    private final Map<String,String> mavenFetcherProperties = new HashMap<>();
    private Optional<String> confFile = Optional.empty();
    private final List<String> modules = new ArrayList<>();
    private final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    private Runnable command;


    public Arguments (Path localConfigFile, String[] args) throws ConfigurationException {

        if (localConfigFile.toFile().exists()) {
            kukumoProperties.putAll(configurationBuilder.buildFromPath(localConfigFile.toString()).inner("kukumo").asMap());
            mavenFetcherProperties.putAll(configurationBuilder.buildFromPath(localConfigFile.toString()).inner("mavenFetcher").asMap());
        }

        if (args == null || args.length == 0) {
            throw new IllegalArgumentException();
        }
        args = trim(args);
        
        if (args[0].equals("fetch")) {
            command = this::fetch;
        } else if (args[0].equals("verify")) {
            command = this::verify;
        } else {
            throw new IllegalArgumentException();
        }

        for (int i=1; i<args.length; i++) {
            String arg = args[i].trim();
            if (!arg.startsWith("-")) {
                throw new IllegalArgumentException();
            }
            arg = arg.substring(1);
            if (!mappedArgumennt(arg, "K", kukumoProperties) &&
                !mappedArgumennt(arg, "M", mavenFetcherProperties)
            ) { 
                if (arg.equals("modules")) {
                    if (i == args.length -1) {
                        throw new IllegalArgumentException();
                    }
                    i++;
                    int j;
                    for (j = i; j<args.length; j++) {
                        if (args[j].startsWith("-")) {
                            break;
                        }
                        modules.add(args[j]);
                    }
                    i = j-1;
                  } else {
                      i = singleValuedArgument(args,i,"-conf", value -> confFile = Optional.of(value));
                      i = flagArgument(args,i,"-clean", () -> mustClean = true);
                  }
            }
        }
        
        Path kukumoConfFile = Paths.get(this.confFile.orElse("kukumo.yaml"));
        if (kukumoConfFile.toFile().exists()) {
            kukumoProperties.putAll(configurationBuilder.buildFromPath(kukumoConfFile.toString()).inner("kukumo").asMap());
            mavenFetcherProperties.putAll(configurationBuilder.buildFromPath(kukumoConfFile.toString()).inner("mavenFetcher").asMap());
        }
    }




    private String[] trim(String[] args) {
        return Stream.of(args).map(String::trim).filter(s->!s.isEmpty()).toArray(String[]::new);
    }



    private boolean mappedArgumennt(String arg, String prefix, Map<String,String> map) {
        if (arg.startsWith(prefix)) {
            arg = arg.substring(1);
            if (arg.contains("=")) {
                String[] propertyPair = arg.split("=");
                map.put(propertyPair[0],propertyPair[1]);
            } else {
                map.put(arg,"true");
            }
            return true;
        }
        return false;
    }

    
    private int singleValuedArgument(String[] args, int index, String name, Consumer<String> action) {
        if (args[index].equals(name)) {
            if (index == args.length -1) {
                throw new IllegalArgumentException();
            }
            action.accept(args[index+1]);
            return index+1;
        } else {
            return index;
        }
    }
    
    
    
    private int flagArgument(String[] args, int index, String name, Runnable action) {
        if (args[index].equals(name)) {
            action.run();
        }
        return index;
    }


    public List<String> modules() {
        return modules;
    }

    public Map<String, String> kukumoProperties() {
        return kukumoProperties;
    }

    public Map<String, String> mavenFetcherProperties() {
        return mavenFetcherProperties;
    }
    
    public Optional<String> confFile() {
        return confFile;
    }

    public Runnable command() {
        return command;
    }
    
    
    public void fetch() {
        new KukumoFetcher().fetch(this);
    }
    
    public void verify() {
        new KukumoVerifier().verify(this);
    }

    public boolean mustClean() {
        return this.mustClean;
    }
    


}
