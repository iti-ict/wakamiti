# Changelog

## 1.0.1
### New features
- Now clients can manage extensions without register them in the Java extension mechanism, implementing custom extension 
  loaders (e. g. using a bean manager). Those extension classes must be annotated with ```externallyManaged = true```.  

### API changes
- Introducing a new field in annotation ```@Extension``` : ```externallyManaged = false```
- New public interface ```ExtensionLoader```

  

## 1.0.0
Initial release