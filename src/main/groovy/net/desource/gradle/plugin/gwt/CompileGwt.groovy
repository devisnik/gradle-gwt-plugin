/*
   Copyright 2011 the original author or authors.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.desource.gradle.plugin.gwt

import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

/**
 *
 * @author Markus Kobler
 */
class CompileGwt extends AbstractGwtTask {
    
    static final String COMPILER_CLASSNAME = 'com.google.gwt.dev.Compiler'

    File buildDir
    boolean debug = true
    String style = 'OBF'
    boolean disableClassMetadata
    boolean disableCastChecking
    boolean validateOnly
    boolean draftCompile
    boolean compileReport
    int localWorkers


    def CompileGwt() {
        localWorkers = Runtime.getRuntime().availableProcessors();
    }

    @InputFiles
    Iterable<File> getClasspath() {
        super.getClasspath()
    }

    @OutputDirectory
    File getBuildDir() {
        buildDir
    }

    @TaskAction
    def compileGwt() {

        if( modules == null || modules.size == 0 ) throw new StopActionException("No modules specified");

        project.javaexec {
            main COMPILER_CLASSNAME
            classpath( this.getClasspath())

            if (debug) {
                args '-ea'
            }

            args "-logLevel", "${logLevel}"
            args "-style", "${style}"

            if (validateOnly) args '-validateOnly'
            if (draftCompile) args '-draftCompile'
            if (compileReport) args '-compileReport'
            if (localWorkers > 1) args "-localWorkers", "${localWorkers}"

            if (disableClassMetadata) args "-disableClassMetadata"
            if (disableCastChecking) args "-XdisableCastChecking"

            if (genDir) {
                genDir.mkdirs()
                args "-gen", "${genDir}"
            }

            if (workDir) {
                workDir.mkdirs()
                args "-workDir", "${workDir}"
            }

            if (extraDir) {
                extraDir.mkdirs()
                args "-extra", "${extraDir}"
            }

            buildDir.mkdirs()
            args "-war", "${buildDir}"

            modules.each {
                logger.info("Compiling GWT Module {}", it)
                args it
            }
        }

    }

}
