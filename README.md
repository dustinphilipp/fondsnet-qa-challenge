# Prerequisites

* Java Annotation Preprocessing needs to be enabled
* JDK 11
* Docker for Desktop should be installed with minimum docker version 1.6.0
* environment variables for credentials need to be configured (or provided in run configurations for IDE)
  * FONDSNET_USERNAME
  * FONDSNET_PASSWORD

### Note for Apple Silicon
If executed on Apple Silicon (M1, M2 and onward) you have to enable

* Use Rosetta for x86/amd64 emulation on Apple Silicon

in Docker Desktop settings >> Features under Development

# How to execute

* Run all tests with IDE or
* use: mvn test in command line or gui

### Test correct setup
For convenience, a test that only reads the title of the fondsnet applications first page was created.

This test should only fail if the prerequistes are not met are the page is not available.

# Improvement strategies

* Go through the tested application (ideally with developers) and place additional id tags wherever necessary
* decide whether to create a global file for all locators, use the page object model or utilize a framework like Cucumber
* use a separate configuration file for visibility and future ease of accessibility instead of handling inline
* configure test videos (e.g. have sensible filenames)
* waitUntilPageLoad() doesn't appear optimal to me, 
but apparently Selenium lost some methods in regards to waiting in the 4.x.x versions
  * found afterwards -> ExpectedConditions and Waits where moved into separate support dependency
  * -> replace waitUntilPageLoad()
* usage of var confuses some people, even if the variable names should be self-explanatory