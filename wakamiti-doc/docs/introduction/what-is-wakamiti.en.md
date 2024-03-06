---
title: What is Wakamiti?
date: 2022-09-20
slug: /en/introduction/what-is-wakamiti
---


> **Basic knowledge** <br />
> This guide assumes that you know the basics of [Cucumber](https://cucumber.io/docs/guides/overview/),
> [Gherkin](https://cucumber.io/docs/gherkin/) and [Behaviour-Driven Development](https://cucumber.io/docs/bdd/)
> methodology.

**Wakamiti** is a Cucumber-inspired tool written in Java focused on blackbox testing using natural language.

Just like Cucumber, you can define your tests using natural and human-readable language by adopting, for example, the
Gherkin *grammar*. However, with Wakamiti you do not bind each step to your test code; instead, steps are bound to 
reusable, common-purpose code provided by external plugins.

Thus, Wakamiti turns out to be a convenient tool if your aim is to test your system via standardized protocols such as REST
web services or JDBC connection, which tend to be a great deal of percentage of the tests written for most applications.
Also, no test code is required to be written, so even non-programmers can define and execute their own tests.

Other features provided by Wakamiti are:

- **Two-layered Gherkin**: you can make use of the Gherkin grammar at two levels of abstraction, one aimed to customer
  communication, and other aimed to system details.
- **Fully localizable**: you can use your own language as long as you provided Wakamiti with a translation file.
- **Easily extensible**: write your own plugins for any extension point (steps, reporters, language parsers, etc) and
  share them with the community.
- **Alternative launchers**: execute Wakamiti as a JUnit test suite, as a Maven verify goal, or directly as a console
  command.

> **REMEMBER** <br />
> Wakamiti is a *tool*, not a *testing framework*. Use it when fits the circumstances. Neither is it a replacement for
> Cucumber: according your necessities, you might use *both*!
