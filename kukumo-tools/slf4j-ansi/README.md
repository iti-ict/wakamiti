
# SLF4J Ansi  
  
This tool enhances the [*Simple Logging Facade for Java*][1] library with the capability of 
formatting parameters using [ANSI escape codes][2], which are defined as *styles* using  
the [Jansi][3] library. In addition, overall styles can be defined for each logging level (`ERROR`,  
`WARN`,`INFO`,`DEBUG`, `TRACE`).  
  
If your console or log storing system does not work nicely with ANSI escape codes, you can   
simply disable this feature and your messages will be logged without them, while you are not   
required to change your code.  
  
## Usage  

### Quick start  
Just create an `AnsiLogger` wrapper around your Slf4J logger:  
```java  
Logger logger = AnsiLogger.of(LoggerFactory.getLogger(YourClass.class);  
```  
  
After that, when you want to embed a parameter in a message, you can indicate the style inside the   
brackets:  
  
```java  
logger.info("The service endpoint is {uri}", "http://myserver:8080/service");  
logger.warn("A timeout of {} seconds has ocurred requesting the resource {resource}", 4, "data.json");  
```  
### Defining custom styles
If you desire to add/replace styles, you should configure `AnsiLogger` in some initializing method of your application, so that the styles are applied from the very beginning:
```java  
public static void someInitializerMethod() {  
 Properties customStyles = new Properties(); customStyles.put("resource","magenta,underline,bold"); ... AnsiLogger.setStyles(customStyles);}  
```  

### Overall styles
Apart from the style of each argument of the message, you can define an overall style applied to the whole message, using the syntax `{!style}` at the beggining:
```java
logger.info("{!important} This is an important message");
```
Each log level defined in the SLF4J API use an overall style identified respectively by `error`, `warn`, `info`, `debug`, and `trace`. You can overwrite these styles, and even use them with another log level:
```java
logger.debug("{!warn} This is a debug message formatted using the style of warn messages");
```

### Enable / disable ANSI codes

It is possible to manually enable / disable the formatting:
```java
AnsiLogger.setEnabled(false);
```
When disabled, no Ansi codes are generated whatsoever. It is useful if you want to activate/deactivate
this feature according to some argument or configuration provided externally. 


## Default styles
The built-in styles provided by default are the following:

|Style|Definition |
| --- | --- |
|`error`|`red,bold` |
|`warn`|`yellow,bold` |
|`trace`|`faint` |
|`uri`|`blue,underline` |
|`id`|`cyan,bold` |
|`important`|`magenta,bold` |
|`highlight`|`white,bold` |

## Jansi Codes  
The recognized styles are defined by Jansi codes (can be combined with commas), which are:  
  
| Code | Representation |
| --- | --- |
| `black` | black text color |
| `red` | red text color |
| `green` | green text color |
| `yellow` | yellow text color |
| `blue` | blue text color |
| `magenta` | magenta text color |
| `cyan` | cyan text color |
| `white` | white text color |
| `bold` | bold text intensity |
| `faint` | faint text intensity |
| `underline` | underline text |
| `blink_slow` | text blinking at low rate |
| `blink_fast` | text blinking at high rate |
| `bg_black` | black background color |
| `bg_red` | red background color |
| `bg_green` | green background color |
| `bg_yellow` | yellow background color |
| `bg_blue` | blue text background |
| `bg_magenta` | magenta background color |
| `bg_cyan` | cyan background color |
| `bg_white` | white background color |

## Other considerations

Be aware that the use of this library will add extra computational cost to your underlying logging
system. For this reason, it is not recommended in scenarios where performance may be an issue, for 
example extensive logging during long operations. Also, logging to targets other than console 
will produce the same ANSI codes. Thus, it might difficult the readability of the messages if the 
visualizer tool used does not support ANSI.
  


  
## References  
- [**1**] *Simple Logging Facade for Java* - https://www.slf4j.org  
- [**2**] *ANSI escape code* at Wikipedia - https://en.wikipedia.org/wiki/ANSI_escape_code  
- [**3**] *Jansi* - https://fusesource.github.io/jansi  
  
[1]: https://www.slf4j.org  
[2]: https://en.wikipedia.org/wiki/ANSI_escape_code  
[3]: https://fusesource.github.io/jansi/