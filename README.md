# Gradle GWT plugin https://travis-ci.org/youribonnaffe/gradle-gwt-plugin#

## Usage

To use the GWT plugin, include in your build script:

    apply plugin: 'gwt'

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on
[BinTray](https://bintray.com/youribonnaffe/maven/gradle-gwt-plugin/).
Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an
example on how to retrieve it from Bintray:

    buildscript {
        repositories {
            maven { url { 'http://dl.bintray.com/youribonnaffe/maven' } }
        }

        dependencies {
            classpath 'org.gradle.api.plugins:gradle-gwt-plugin:0.1-02112013'
        }
    }

## Tasks

The GWT plugin defines the following tasks:

* `compileGwt`: Run GWT compiler
* `gwtSources`: Build sources JAR to be used in GWT module
* `gwtDevMode`: Run GWT compiler

## Convention properties

The GWT plugin defines the following convention properties in the `gwt` closure:

* `gwtModules`: Specifies the name(s) of the module(s) to host (List of strings)
* `gwtVersion`: GWT version to use (Default to 2.5.1)

The configuration `gwt` is created for GWT dependencies (compile time only). You can eventually define them yourself, otherwise they will be automatically defined from `gwtVersion`.

The task `compileGwt` allows to run the GWT compiler. Define the tasks' properties in the
closure `compileGwt`:

* `buildDir`: The directory into which deployable output files will be written (must be writeable; defaults to build/gwt/out)
* `workDir`:  The compiler's working directory for internal use (must be writeable; defaults to build/gwt/work)
* `genDir`:  The directory into which generated files, not intended for deployment, will be written (defaults to build/gwt/gen)
* `extraDir`:  The directory into which extra files, not intended for deployment, will be written (defaults to build/gwt/extra)
* `style`: Script output style: OBF[USCATED], PRETTY, or DETAILED (defaults to OBF)
* `disableClassMetadata`: EXPERIMENTAL: Disables some java.lang.Class methods (e.g. getName())
* `disableCastChecking`: EXPERIMENTAL: Disables run-time checking of cast operations
* `validateOnly`: Validate all source code, but do not compile
* `draftCompile`: Enable faster, but less-optimized, compilations
* `localWorkers`: The number of local workers to use when compiling permutations
* `modules`:  Specifies the name(s) of the module(s) to host (List of strings)

The task `gwtDevMode` allows to run your GWT application in development mode. Define the tasks' properties in the
closure `gwtDevMode`:

* `noserver`: Prevents the embedded web server from running
* `port`: Specifies the TCP port for the embedded web server (defaults to 8888)
* `whitelist`: Allows the user to browse URLs that match the specified regexes (comma or space separated)
* `blacklist`: Prevents the user browsing URLs that match the specified regexes (comma or space separated)
* `logdir`: Logs to a file in the given directory, as well as graphically (File)
* `bindAddress`: Specifies the bind address for the code server and web server (defaults to 127.0.0.1)
* `codeServerPort`: Specifies the TCP port for the code server (defaults to 9997)
* `startupUrls`: Automatically launches the specified URLs (List of strings)
* `warDir`: The directory into which deployable output files will be written (defaults to 'src/main/webapp')
* `workDir`:  The compiler's working directory for internal use (must be writeable; defaults to build/gwt/work)
* `genDir`:  The directory into which generated files, not intended for deployment, will be written (defaults to build/gwt/gen)
* `extraDir`:  The directory into which extra files, not intended for deployment, will be written (defaults to build/gwt/extra)
* `modules`:  Specifies the name(s) of the module(s) to host (List of strings)

### Example

    gwt {
        gwtModules << 'com.acme.MyModule'
    }

    gwt {
        gwtVersion = '2.5.1' // default value, will add GWT dependencies (user, dev, servlet, validation)
        // to define the dependencies yourself use gwt scope in dependencies

        modules << 'com.acme.AnotherModule'
        modules = ['com.acme.MyModule', 'com.acme.AnotherModule']

    }

    compileGwt {
        style = 'DETAILED'
        compileReport = true
        jvmArgs =   [ '-Xmx512M', '-Xss1024k'] // JVM Options from JavaExec are supported
    }

## Acknowledgements

Based on https://github.com/desource/gradle-gwt-plugin and inspired by GWT Maven plugin

Readme format from https://github.com/bmuschko/gradle-gae-plugin

Travis CI: [![Build Status](https://travis-ci.org/youribonnaffe/gradle-gwt-plugin.png)](https://travis-ci.org/youribonnaffe/gradle-gwt-plugin])
