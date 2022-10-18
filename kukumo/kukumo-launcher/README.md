# Wakamiti::Launcher

This project is a stand-alone, command-line tool to execute Wakamiti test plans. It does not require Maven present to be 
launched, although a Maven repository is still required in order to obtain libraries dependencies. 



## Requirements

- Java 8 or newer installed in the O.S.
- Access to a Maven repository

## Usage

### Installation
#### Linux
1. Download the `kukumo-launcher-x.x.x.zip` file and extract it in a folder of your choice
2. Add execution permissions to the file `kukumo.sh` (in case they were not set)
```
chmod +x kukumo.sh
```
3. Add a environment variable to your OS named `KUKUMO_PATH` and valued with the absolute path to the folder.
```
export KUKUMO_HOME=path_to_kukumo_folder
```
Also, you may want to include it in every boot. In the case of using `bash`, for example:
```
echo export KUKUMO_HOME=path_to_kukumo_folder >> ~/.profile
```
4. Create a symbolic link to the executable
```
sudo ln -s $KUKUMO_HOME/kukumo.sh /usr/local/bin/kukumo ; sudo chmod +x /usr/local/bin/kukumo
```
5. Optionally, modify the `launcher.properties` file from the installation folder with custom values
#### Windows 
1. Download the `kukumo-launcher-x.x.x.zip` file and extract it in a folder of your choice
2. Add a environment variable to your OS named `KUKUMO_PATH` and valued with the absolute path to the folder.
```
setx KUKUMO_HOME "path_to_kukumo_folder" /M
```
3. Add `KUKUMO_HOME\kukumo.bat` to the `PATH` environment variable
```
setx PATH "%PATH%;%KUKUMO_HOME\kukumo.bat%"
```
4. Optionally, modify the `launcher.properties` file from the installation folder with custom values





> **INFO**  
> By default, a ready-to-use public Maven repository is configured. You can add more repositories editing 
> the `mavenFetcher.remoteRepositories` line, each repository separated with `;` . For further explanation of 
> any possible repository configuration please check the _Maven Fetcher_ project.

> **TIP**  
> At this current stage of the project, it is possible that the own Wakamiti libraries are not published yet.  
> However, you can clone or download the entire project and install it in your local repository by typing 
> `mvn install` (Maven must be installed to do that). The compiled libraries will be located at `{HOME}/.m2/repository`,
> so simple add that folder as a repository using the `file:///` protocol instead of `https://`.

### Command line

The more basic usage of the launcher is simply typing the following in your terminal:

```
kukumo
```

The launcher will search for any suitable test according the attached plugins and will proceed to execute the resulting 
test iti.kukumo.test.gherkin.plan. However, bear in mind that Wakamiti do nothing by itself, so we need to, at least, provide a test discovery 
plugin and a step library plugin in order to get a successful execution.

A proper example would be the following:
```
kukumo -modules iti.kukumo:kukumo-gherkin:1.0.0 iti.kukumo:kukumo-rest:1.0.0
```
This way, Wakamiti will search for any Gherkin (`.feature`) file in the current folder and sub-folders, and it will try to
build and execute a test iti.kukumo.test.gherkin.plan with REST-related steps.

> **INFO**  
> The Wakamiti Launcher will create a folder named `.kukumo` in the current folder in order to store libraries and pass 
> them to the Java classpath. You can delete this folder safely, but it will be recreated every time the launcher is 
> executed.

There is an alternative to typing the required modules every time: use a local configuration file. If you create a file 
in the current folder using the default name `kukumo.yaml`, you can configure every Wakamiti property inside instead of 
passing them as command-line arguments:

###### kukumo.yaml
```yaml
kukumo:
  launcher:
     modules:
       - iti.kukumo:kukumo-gherkin:1.0.0
       - iti.kukumo:kukumo-rest:1.0.0
```  

> **IMPORTANT**  
> The `modules` property accepts not only the plugins used directly by Wakamiti but any other Maven artifact.  
> For example, if you use the database steps plugin, you will require as well a library with the proper JDBC driver. 
> Just declare the corresponding Maven artifact as a regular module and it will be obtained and attached to the 
> execution.

### Arguments
The following is a list of accepted arguments:

| Command line argument  | Configuration file property | Definition |
| --------------------- | --------------------------- | ---------- |
|`-modules <module1> [module2 ...]` | `kukumo.modules` | Set a list of required artifacts |
|`-conf <filename>` | *not applicable* | Set a custom file to be used as configuration file |
|`-M<argument>` | `mavenFetcher.<property>` | Set a custom *Maven Fetcher* property |
|`-K<argument>` | `kukumo.<property>` | Set a custom *Wakamiti* property |




## License
```
    Mozilla Public License 2.0

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at https://mozilla.org/MPL/2.0/.
```


## Contributing
Currently the project is closed to external contributions but this may change in the future.


## Authors
- Luis Iñesta Gelabert  |  :email: <linesta@iti.es> | :email: <luiinge@gmail.com>