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
package org.gradle.api.plugins.gwt

import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction
import org.gradle.util.AntUtil
import org.apache.tools.ant.taskdefs.Expand

/**
 *
 * @author Markus Kobler
 */
class GwtDevMode extends AbstractGwtTask {

    static final String DEVMODE_CLASSNAME = 'com.google.gwt.dev.DevMode'

    boolean noserver;
    int port = 0;
    String whitelist
    String blacklist
    File logDir;
    int codeServerPort = 0;
    String bindAddress;

    File warDir

    List<String> startupUrls

    @Override
    void exec() {
        if (!modules) throw new StopActionException("No gwtModules specified");

        main = DEVMODE_CLASSNAME

        if (noserver) args "-noserver"

        if (port > 0) args "-port", "${port}"
        if (codeServerPort > 0) args "-codeServerPort", "${codeServerPort}"
        if (bindAddress) args "-bindAddress", "${bindAddress}"

        if (whitelist) args "-whitelist", "${whitelist}"
        if (blacklist) args "-blacklist", "${blacklist}"

        startupUrls.each {
            logger.info("Startup URL {}", it)
            args "-startupUrl", "${it}"
        }

        warDir.mkdirs()

        args "-war", "${warDir}"

        if (logDir) {
            logDir.mkdirs()
            args "-logdir", "${logDir}"
        }
        args "-logLevel", "${logLevel}"

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


        modules.each {
            logger.info("GWT Module {}", it)
            args it
        }

        super.exec()
    }

}
