# Gradle GWT plugin

## Usage

To use the GWT plugin, include in your build script:

    apply plugin: 'gwt'

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on
[BinTray]).
Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an
example on how to retrieve it from Bintray:

    buildscript {
        repositories {
            maven { url { 'http://dl.bintray.com/youribonnaffe/maven' } }
        }

        dependencies {
            classpath 'org.gradle.api.plugins:gradle-gwt-plugin:0.1-20102013'
        }
    }

## Tasks

The GWT plugin defines the following tasks:

* `compileGwt`: Run GWT compiler

## Convention properties

### Example

    gwt {
        module = 'com.acme.MyModule'
    }

    gwt {
        version = '2.5.1' // default value, will add GWT dependencies (user, dev, servlet, validation)
        // to define the dependencies yourself use gwt scope in dependencies

        modules = '...'
        modules << 'com.acme.AnotherModule'
        modules = ['com.acme.MyModule', 'com.acme.AnotherModule']

    }

## Acknowledgements

Based on https://github.com/desource/gradle-gwt-plugin and inspired by GWT Maven plugin
Readme format from https://github.com/bmuschko/gradle-gae-plugin

