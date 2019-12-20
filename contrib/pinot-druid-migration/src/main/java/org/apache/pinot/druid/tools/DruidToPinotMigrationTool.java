/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.druid.tools;

import java.lang.reflect.Field;
import org.apache.pinot.tools.Command;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;


/**
 * Class to implement the DruidToPinotMigrationTool, which the command to convert a Druid segment to a Pinot segment
 * locally (ConvertSegment)
 */
public class DruidToPinotMigrationTool {
  @Argument(handler = SubCommandHandler.class, metaVar = "<subCommand>")
  @SubCommands({
      @SubCommand(name = "ConvertSegment", impl = DruidToPinotSegmentConverterCommand.class),
  })
  Command _subCommand;

  @Option(name = "-help", help = true, aliases = {"-h", "--h", "--help"}, usage = "Print this message.")
  boolean _help = false;
  boolean _status = false;

  private boolean getStatus() {
    return _status;
  }

  public void execute(String[] args)
      throws Exception {
    try {
      CmdLineParser parser = new CmdLineParser(this);
      parser.parseArgument(args);

      if ((_subCommand == null) || _help) {
        printUsage();
      } else if (_subCommand.getHelp()) {
        _subCommand.printUsage();
        _status = true;
      } else {
        _status = _subCommand.execute();
      }
    } catch (CmdLineException e) {
      System.out.println("Error: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Exception caught: ");
      throw e;
    }
  }

  public static void main(String[] args)
      throws Exception {
    DruidToPinotMigrationTool druidToPinotMigrationTool = new DruidToPinotMigrationTool();
    druidToPinotMigrationTool.execute(args);
    if (System.getProperties().getProperty("druid.pinot.migration.system.exit", "false").equalsIgnoreCase("true")) {
      System.exit(druidToPinotMigrationTool.getStatus() ? 0 : 1);
    }
  }

  public void printUsage() {
    System.out.println("Usage: java -jar druid-to-pinot-migration-tool-jar-with-dependencies.jar <subCommand>");
    System.out.println("Valid subCommands are:");

    Class<DruidToPinotMigrationTool> obj = DruidToPinotMigrationTool.class;

    for (Field f : obj.getDeclaredFields()) {
      if (f.isAnnotationPresent(SubCommands.class)) {
        SubCommands subCommands = f.getAnnotation(SubCommands.class);

        for (SubCommand subCommand : subCommands.value()) {
          Class<?> subCommandClass = subCommand.impl();
          Command command;

          try {
            command = (Command) subCommandClass.newInstance();
            System.out.println("\t" + subCommand.name() + "\t<" + command.description() + ">");
          } catch (Exception e) {
            System.out.println("Internal Error: Error instantiating class.");
          }
        }
      }
    }
  }

}