/*
 * Copyright 2012 The Juneiform Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ifmo.juneiform.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author felleet
 */
public class XSane implements ScanEngine {

    private File file;

    public XSane(File file) {
        this.file = file;
    }

    @Override
    public void performScan() throws XSaneException {
        List<String> command = new ArrayList<String>();
        command.add("xsane");
        command.add("-s");
        command.add("-n");
        command.add("-N");
        command.add(file.getAbsolutePath());
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            throw new XSaneException("Could not run specified command: "
                    + processBuilder.command());
        }

        int result = -1;
        try {
            result = process.waitFor();
        } catch (InterruptedException ex) {
            throw new XSaneException("Scan process was interrupted");
        }

        if (result != 0) {
            throw new XSaneException("Execution result " + result);
        }

    }
}
