### Configuration

The following properties are recognized by the Kukumo Gherkin plugin:
  
---
  
#####  ```tagFilter```
  
Defines a filter expression based on the tags present in features and scenarios. 
The expression can uses the logic keywords ```and```, ```or```, ```not```,
and the parentheses symbols in order to build complex conditions.

##### Examples
- Execute only scenarios tagged with _@important_ : ```important```
- Execute only scenarios tagged with _@important_ but excluding the ones tagged with _@slow_ : 
```important and not slow```
- Execute only scenarios either tagged with _@important_ and not tagged with _@slow_, or 
tagged with _@critical_:  ```(important and not slow) or critical``` 

> Notice that, in contrast to 
[Cucumber Tag Expressions](https://github.com/cucumber/cucumber/tree/master/tag-expressions), 
the ```@``` symbol is not used within the filter expression
  
- Accepted values: _<tag expression\>_
- Default value: _<none\>_  
---
  
##### ```idTagPattern```
Defines the regular expression used to identify specific tags in features and
scenarios in a way that such tag will be use as a unique identifier. Unique 
identifiers are required with scenario redefinitions. 

- Accepted values: _<regex expression\>_
- Default value: ```ID-(.*)```


---

##### ```redefinition.enabled```
Enable/disable the scenario redefinition capabilities.

- Accepted values: ```true``` ```false```
- Default value: ```true```

---


#####  ```redefinition.definitionTag```
Defines the tag used to mark a feature or scenario as a _definition document_. It only has effect when 
```redefinition.enabled = true```.

- Accepted values: _<string\>_
- Default value: ```definition```

---


#####  ```redefinition.implementationTag```

Defines the tag used to mark a feature or scenario as a _implementation document_. It only has effect when 
```redefinition.enabled = true```.

- Accepted values: _<string\>_
- Default value: ```implementation```


---


#####  ```dataFormatLanguage```

Defines the locale format used to write data values. By overriding this property, 
data values can be expressed in a different language than the one used to write the scenario.

##### Example
```gherkin
# dataFormatLanguage: es
Scenario: Load a set of numbers written in Spanish
   Given the following values:
   | 1.234,66 |  999.877.435,55 | 
```

- Accepted values: _<language code\>_
- Default value: _<inherited form property ```language```\>_

