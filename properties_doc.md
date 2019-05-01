# Configuring Kukumo

A Kukumo configuration is just a set of valued properties that are
applied to a specific context in order to alter the default behaviour.
Kukumo and its extensions are highly configurable, and the
configurations can be readed from a wide range of inputs and formats,
such as the follwing:

## Configuration Sources

### Annotated Java class

  You can annotate a Java class with `@Configurator` and `@Property` in
  order to specify properties directly within your code. This approach
  is specially useful when using the Kukumo JUnit runner.
  
  ```java
  
  @RunWith(KukumoJUnitRunner.class)

  @Configurator(properties = {
    @Property(key=KukumoConfiguration.RESOURCE_TYPE, value=GherkinResourceType.NAME),
    @Property(key=KukumoConfiguration.RESOURCE_PATH, value="src/test/resources/features"),
    @Property(key=KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS, value= "iti.kukumo.gherkin.test.steps.KukumoSteps") 
  })    
  public class TestKukumoRunner {
  
  }
  ```
  
  
### Text files
 Kukumo can read configuration from text files by two means:
- Annotate a Java Class with `@Configurator` and directly specify a file
  containing the properties in the `path` attribute, instead of using
  `@Property`.
- Setting the property `kukumo.configurationPath` at some level (such as
  command line argument or Maven `pom.xml` propertie)
  
 
 The formats currently supported are:
 - Property files
 - JSON files
 - XML files
 - YAML files
### System-defined properties
 


## Configuration Levels

>   **\[NOTE\]** Please be aware that, when using properties inside a
>   resource file (e. g. a Gherkin scenario within a feature file), the
>   property prefix `kukumo` is implicit and you should not use it. By
>   contrast, the `kukumo` prefix is expected when provided by an
>   external source (such as a configuration file or command line
>   argument) in order to distinguish it from other application
>   configurations.
  

## Core Properties

- **kukumo.resourceType** : string = *(auto-detected)*

  Set the type of the resources that will be used to create the test
  plan. If this property is not set, Kukumo will use the resource type
  defined by the first resource type contributor detected in the
  classpath.
  
- **kukumo.resourcePath** : list-of-strings = *.*

  Set the absolute or relative paths where the resource discovery will
  happen.

- **kukumo.language** : string = *(auto-detected)
 
  Set the human language used to write the resources. Kukumo allows you 
  to use different languages in different resources within the same test plan.
  

- **kukumo.dataFormatLanguage** : string = *(auto-detected)

  Set the language used to express data. By default it will be used the
  same language that the enclosing element.

- **kukumo.modules** : list-of-strings

  Restrict the data type and steps that will be considered. 
  
  > **\[NOTE\]** By default, Kukumo will create, for each atomic plan
  > node, a full backend support for all the steps provided by all the
  > step contributors. This may cause problems if the name of the data
  > type or steps from different contributors collide. Setting this
  > property at any level will force Kukumo to use only the specified
  > contributors instead of all of them.

- **kukumo.nonRegisteredStepProviders** : list-of-Java-classes

  Declare extra step providers that are not registered as extensions.

  > **\[NOTE\]** The desirable way to declare a step provider is use the
  > annotation `@Extension`, but there are some scenarios, such as
  > internal testing, where a step provider should not be exposed
  > externally. This property is useful in those cases.

- **kukumo.tagFilter**

  Set a tag filter expression for the features and scenarios to be
  consider when creating the test plan. The expression should be a
  combination of tag names, brackets, and the logic operators `and`,
  `or`,`not`.

  > Examples:
  > - important and not slow
  > - important or (fast and not experimental)
  
  >  **\[NOTE\]** Do not get confused about the fact that in the Gherkin
  >  syntax tags are defined by the symbol `@`. Kukumo is a
  >  language-agnostic tool and its *tag* concept representation may
  >  differ from the specific test definition language used.

- **kukumo.idTagPattern** : string = *ID-(.\*)*

  Defines the regex pattern to be used when attempting to compute the
  identifier of an element based on its tags.
  
- **kukumo.redefinition.enabled** : boolean = *true*

  Enable/disable the tag-based redefinition capabilities of Kukumo when
  the test definition language does not support it natively.
    
- **kukumo.redefinition.definitionTag** : string = *definition*

  Defines the tag used to mark an element as the *definition* part of a
  redefinition. 

- **kukumo.redefinition.implementationTag** : string = *implementation*

  Defines the tag used to mark en alement as the *implementation* part
  of a redefinition.

- **kukumo.redefinition.stepMap** : dash-separated-integers

  Set, for a specific implementation scenario, the correspondence
  between the definition steps and the implementation steps.
  
  > Example: 
  >
  > ```redefinition.stepMap: 0-1-2-1``` 
  >
  > represents the following table: 
  >
  > | definition | implementation | 
  > | --- | --- |
  > | step A | \<no-step\> | 
  > | step B | step 1 |
  > | step C | step 2, step 3 |
  > | step D | step 4 |
> **\[NOTE\]** Redefinition implementation may vary depending on the
> specific planner used, but it should honour the above parameters.
