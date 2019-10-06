package iti.kukumo.core.backend;

import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.util.Pair;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static iti.kukumo.core.backend.DefaultBackend.*;

/**
 * This class 
 *
 */
public class BackendArguments implements Iterable<Pair<String,String>> {

    private final Class<?> stepProviderClass;
    private final Step stepAnnotation;
    private final Class<?>[] methodArgTypes;
    private final KukumoDataTypeRegistry typeRegistry;
    
    private final List<Pair<String,String>> argumentMap = new ArrayList<>();
    
    
    public BackendArguments(Class<?> stepProviderClass, Method stepMethod, KukumoDataTypeRegistry typeRegistry) {
        this.stepProviderClass = stepProviderClass;
        this.stepAnnotation = stepMethod.getAnnotation(Step.class);
        this.methodArgTypes = stepMethod.getParameterTypes();
        this.typeRegistry = typeRegistry;
        parseDeclaredArguments();
        inferNonDeclaredArguments();
        completeExtraArguments();
        validateArguments();
    }
        
    
    public int size() {
        return argumentMap.size();
    }
    
    
    public Pair<String, String> get(int index) {
        return argumentMap.get(index);
    }
    
    @Override
    public Iterator<Pair<String, String>> iterator() {
        return argumentMap.iterator();
    }


    
    
    protected void parseDeclaredArguments() {
        for (int i=0; i<stepAnnotation.args().length; i++) {
            String argName = "";
            String argType = "";
            final String[] nameAndType = stepAnnotation.args()[i].split(":");
            if (nameAndType.length == 1) {
                argName = UNNAMED_ARG;
                argType = nameAndType[0];
            } else if (nameAndType.length == 2) {
                argName = nameAndType[0];
                argType = nameAndType[1];
            } else {
                throwWrongStepDefinitionException("Argument '{} is not well formed", stepAnnotation.args()[i]);
            }
            argumentMap.add(new Pair<>(argName,argType));
        }
    }
        
        
        
   


    /*
     * If the method annotated with @Step accepts arguments and the field 'args' of @Step is not
     * declared, we'll attempt to infer the arguments automatically. This contributes to make the 
     * step declarations less verbose.
     */
    protected void inferNonDeclaredArguments() {
        if (argumentMap.isEmpty()) {
            for (Class<?> methodArgType : methodArgTypes) {
                Optional<KukumoDataType<?>> argType = typeRegistry.findTypesForJavaType(methodArgType).findAny();
                if (argType.isPresent()) {
                    argumentMap.add(new Pair<>(UNNAMED_ARG,argType.get().getName()));
                }
            }
        }
    }
        

        
    protected void validateArguments() {
        String error = null;
        int i;
        for (i = 0; i<argumentMap.size(); i++) {
            error = validateArgument(i);
            if (error != null) {
                break;
            }
        }
        if (error != null) {
            throwWrongStepDefinitionException(
                "Argument '{}:{}' does not match implementation argument type {} {}",
                argumentMap.get(i).key(),argumentMap.get(i).value(),methodArgTypes[i],error
            );
        }
    }



    protected String validateArgument(int index) {
        String error = null;
        if (argumentMap.get(index).value().equals(DOCUMENT_ARG)) {
            if (!methodArgTypes[index].equals(Document.class)) {
                error = "(expected "+Document.class+")";
            }
        } else if (argumentMap.get(index).value().equals(DATATABLE_ARG)) {
            if (!methodArgTypes[index].equals(DataTable.class)) {
                error = "(expected "+DataTable.class+")";
            }
        } else {
            final String typeName = argumentMap.get(index).value();
            final KukumoDataType<?> type = typeRegistry.getType(typeName);
            if (type == null) {
                throwWrongStepDefinitionException("Type {} not registered.", typeName);
            }
            if (!type.getJavaType().equals(methodArgTypes[index])) {
                error = "(expected "+type.getJavaType()+")";
            }
        }
        return error;
    }

    
    

    /*
     * Fill the argument map with the arguments that are not part of the step definition
     * (currently document string and datatables, but new extra arguments may be added in the future).
     */
    protected void completeExtraArguments() {
        int index = argumentMap.size();
        if (methodArgTypes.length - index == 0) {
            return;
        }
        boolean error = false;
        if (methodArgTypes.length - index == 1) {
            if (methodArgTypes[index].equals(Document.class)) {
                argumentMap.add(new Pair<>(DOCUMENT_ARG,DOCUMENT_ARG));
            } else if (methodArgTypes[index].equals(DataTable.class)) {
                argumentMap.add(new Pair<>(DATATABLE_ARG,DATATABLE_ARG));
            } else {
                error = true;
            }
        } else {
            error = true;
        }
        if (error) {
            String definitionInfo = argumentMap.stream().map(pair -> pair.key()+":"+pair.value()).collect(Collectors.joining(", "));
            String implementationInfo = Stream.of(methodArgTypes).map(String::valueOf).collect(Collectors.joining(", "));
            throw new WrongStepDefinitionException(stepProviderClass, stepAnnotation.value(),
                    "Step arguments not machting implementation arguments\n\tdefinition={}, implementation={}",
                    definitionInfo,implementationInfo);
        }
    }


    @Override
    public String toString() {
        return argumentMap.toString();
    }

    private void throwWrongStepDefinitionException(String message, Object... args)
    throws WrongStepDefinitionException {
        throw new WrongStepDefinitionException(stepProviderClass, stepAnnotation.value(), message, args);
    }


    

    







}
