---
title: What is Wakamiti?
date: 2022-09-20
slug: /en/introduction/what-is-wakamiti
---


> **Basic knowledge** <br />
> This guide assumes that you are familiar with the basics of [Cucumber](https://cucumber.io/docs/guides/overview/),
> [Gherkin](https://cucumber.io/docs/gherkin/) and [Behaviour-Driven Development](https://cucumber.io/docs/bdd/)
> methodology.

**Wakamiti** is a Cucumber-inspired tool written in Java. The main purpose of this application is to do blackbox testing 
using natural language.

Just like Cucumber, you can define your tests using natural, human-readable language by adopting the Gherkin *grammar*,
for example. Wakamiti does not bind the steps to its test code. Instead, the steps are bound to a re-usable, common 
purpose code provided by external plugins.

Therefore, Wakamiti can be a convenient tool if your goal is to test your system using standardized protocols such as 
REST web services or JDBC connection, which tend to be a high percentage of the tests written for most applications. 
Furthermore, test code is not required so, even if you are not a programmer, you can define and execute your own tests.

Other features provided by Wakamiti are:

- **Two-layered Gherkin**: you can use Gherkin's grammar at two levels of abstraction. One focused on customer
  communication, and other targeted to system details.
- **Fully localizable**: use Wakamiti in your own language by providing Wakamiti a translation file.
- **Easily extensible**: write your own plugins for any extension point (steps, reporters, language parsers, etc) and
  share them with the community.
- **Alternative launchers**: you can execute Wakamiti as a JUnit test suite, a Maven verify goal, or a console command.

> **NOTE** <br />
> Wakamiti is a *tool*, not a *framework*. It is not meant to replace Cucumber. In fact, you can use *both* when needed!
