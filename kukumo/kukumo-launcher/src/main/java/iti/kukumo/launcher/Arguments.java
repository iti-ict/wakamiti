package iti.kukumo.launcher;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author ITI
 * Created by ITI on 2/04/19
 */
public class Arguments {

    private Optional<String> confFile = Optional.empty();
    private final List<String> modules = new ArrayList<>();
    private final ConfigurationBuilder configurationBuilder = ConfigurationBuilder.instance();

    private final Configuration kukumoConfiguration;
    private final Configuration mavenFetcherConfiguration;



    public Arguments (Path localConfigFile, String[] args) throws ConfigurationException {

        Map<String,String> kukumoProperties = new HashMap<>();
        Map<String,String> mavenFetcherProperties = new HashMap<>();

        if (args != null && args.length > 0) {
            args = trim(args);

            for (int i=0; i<args.length; i++) {
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
                    }
                }
            }

        }

        

        kukumoConfiguration = buildConfiguration(
            localConfigFile,
            Paths.get(confFile.orElse("kukumo.yaml")),
            kukumoProperties,
            "kukumo"
        );
        mavenFetcherConfiguration = buildConfiguration(
                localConfigFile,
                Paths.get(confFile.orElse("kukumo.yaml")),
                mavenFetcherProperties,
                "mavenFetcher"
        );


    }


    private Configuration buildConfiguration(Path localFile, Path projectFile, Map<String,String> arguments, String qualifier) {
        Configuration localFileConf = (Files.exists(localFile)) ?
            configurationBuilder.buildFromPath(localFile).inner(qualifier) :
            configurationBuilder.empty();
        Configuration projectFileConf = (Files.exists(projectFile)) ?
                configurationBuilder.buildFromPath(projectFile).inner(qualifier) :
                configurationBuilder.empty();
        Configuration argumentConf = configurationBuilder.buildFromMap(arguments);
        return localFileConf.append(projectFileConf).append(argumentConf);
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

    public Configuration kukumoConfiguration() {
        return kukumoConfiguration;
    }

    public Configuration mavenFetcherConfiguration() {
        return mavenFetcherConfiguration;
    }
    
    public Optional<String> confFile() {
        return confFile;
    }


}
