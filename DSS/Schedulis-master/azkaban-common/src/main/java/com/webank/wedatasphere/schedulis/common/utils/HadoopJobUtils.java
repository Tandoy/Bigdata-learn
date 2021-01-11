/*
 * Copyright 2020 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.schedulis.common.utils;

import azkaban.utils.Props;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * There are many common methods that's required by the Hadoop*Job.java's. They are all consolidated
 * here.
 * 
 * Methods here include getting/setting hadoop tokens,
 * methods for manipulating lib folder paths and jar paths passed in from Azkaban prop file,
 * and finally methods for helping to parse logs for application ids, 
 * and kill the applications via Yarn (very helpful during the cancel method)
 * 
 * </pre>
 * 
 * 
 */

public class HadoopJobUtils {
  public static String MATCH_ALL_REGEX = ".*";

  public static String MATCH_NONE_REGEX = ".^";
  
  public static final String HADOOP_SECURITY_MANAGER_CLASS_PARAM = "hadoop.security.manager.class";

  // the regex to look for while looking for application id's in the hadoop log
  public static final Pattern APPLICATION_ID_PATTERN = Pattern
          .compile("application_\\d+_\\d+");

  public static final Pattern BDP_CLIENT_JOB_ID_PATTERN = Pattern
      .compile("job_\\d+_\\d+");


  // Azkaban built in property name
  public static final String JOBTYPE_GLOBAL_JVM_ARGS = "jobtype.global.jvm.args";

  // Azkaban built in property name
  public static final String JOBTYPE_JVM_ARGS = "jobtype.jvm.args";

  // Azkaban built in property name
  public static final String JVM_ARGS = "jvm.args";
  
  // the regex to look for updated partition
  public static final Pattern UPDATE_PARTITION = Pattern.
		  compile("(INFO - Partition (\\S*) stats: \\[numFiles=(\\d+), numRows=(\\d+), totalSize=(\\d+), rawDataSize=(\\d+)\\])");
  
  // the regex to look for updated table
  public static final Pattern UPDATE_TABLE = Pattern.
		  compile("(INFO - Table (\\D*) stats: \\[numFiles=(\\d+), numRows=(\\d+), totalSize=(\\d+), rawDataSize=(\\d+)\\])");


  /**
   * <pre>
   * If there's a * specification in the "jar" argument (e.g. jar=./lib/*,./lib2/*),
   * this method helps to resolve the * into actual jar names inside the folder, and in order.
   * This is due to the requirement that Spark 1.4 doesn't seem to do the resolution for users
   * 
   * </pre>
   * 
   * @param unresolvedJarSpec
   * @return jar file list, comma separated, all .../* expanded into actual jar names in order
   * 
   */
  public static String resolveWildCardForJarSpec(String workingDirectory, String unresolvedJarSpec,
          Logger log) {

    log.debug("resolveWildCardForJarSpec: unresolved jar specification: " + unresolvedJarSpec);
    log.debug("working directory: " + workingDirectory);

    if (unresolvedJarSpec == null || unresolvedJarSpec.isEmpty()) {
      return "";
    }

    StringBuilder resolvedJarSpec = new StringBuilder();

    String[] unresolvedJarSpecList = unresolvedJarSpec.split(",");
    for (String s : unresolvedJarSpecList) {
      // if need resolution
      if (s.endsWith("*")) {
        // remove last 2 characters to get to the folder
        String dirName = String.format("%s/%s", workingDirectory, s.substring(0, s.length() - 2));

        File[] jars = null;
        try {
          jars = getFilesInFolderByRegex(new File(dirName), ".*jar");
        } catch (FileNotFoundException fnfe) {
          log.warn("folder does not exist: " + dirName);
          continue;
        }

        // if the folder is there, add them to the jar list
        for (File jar : jars) {
          resolvedJarSpec.append(jar.toString()).append(",");
        }
      } else { // no need for resolution
        resolvedJarSpec.append(s).append(",");
      }
    }

    log.debug("resolveWildCardForJarSpec: resolvedJarSpec: " + resolvedJarSpec);

    // remove the trailing comma
    int lastCharIndex = resolvedJarSpec.length() - 1;
    if (lastCharIndex >= 0 && resolvedJarSpec.charAt(lastCharIndex) == ',') {
      resolvedJarSpec.deleteCharAt(lastCharIndex);
    }

    return resolvedJarSpec.toString();
  }

  /**
   * <pre>
   * Spark-submit accepts a execution jar or a python file.
   * This method looks for the proper user execution jar or a python file.
   * The user input is expected in the following 3 formats:
   *   1. ./lib/abc
   *   2. ./lib/abc.jar
   *   3. ./lib/abc.py
   * 
   * This method will use prefix matching to find any jar/py that is the form of abc*.(jar|py),
   * so that users can bump jar versions without doing modifications to their Hadoop DSL.
   * 
   * This method will throw an Exception if more than one jar that matches the prefix is found
   * 
   * @param workingDirectory
   * @param userSpecifiedJarName
   * @return the resolved actual jar/py file name to execute
   */
  public static String resolveExecutionJarName(String workingDirectory,
          String userSpecifiedJarName, Logger log) {

    if (log.isDebugEnabled()) {
      String debugMsg = String.format(
              "Resolving execution jar name: working directory: %s,  user specified name: %s",
              workingDirectory, userSpecifiedJarName);
      log.debug(debugMsg);
    }

    // in case user decides to specify with abc.jar, instead of only abc
    if (userSpecifiedJarName.endsWith(".jar")) {
      userSpecifiedJarName = userSpecifiedJarName.replace(".jar", "");
    } else if (userSpecifiedJarName.endsWith(".py")) {
      userSpecifiedJarName = userSpecifiedJarName.replace(".py", "");
    }

    // can't use java 1.7 stuff, reverting to a slightly ugly implementation
    String userSpecifiedJarPath = String.format("%s/%s", workingDirectory, userSpecifiedJarName);
    int lastIndexOfSlash = userSpecifiedJarPath.lastIndexOf("/");
    final String jarPrefix = userSpecifiedJarPath.substring(lastIndexOfSlash + 1);
    final String dirName = userSpecifiedJarPath.substring(0, lastIndexOfSlash);

    if (log.isDebugEnabled()) {
      String debugMsg = String.format("Resolving execution jar name: dirname: %s, jar name: %s",
              dirName, jarPrefix);
      log.debug(debugMsg);
    }

    File[] potentialExecutionJarList;
    try {
      potentialExecutionJarList = getFilesInFolderByRegex(new File(dirName), jarPrefix + ".*(jar|py)");
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(
              "execution jar is suppose to be in this folder, but the folder doesn't exist: "
                      + dirName);
    }

    if (potentialExecutionJarList.length == 0) {
      throw new IllegalStateException("unable to find execution jar for Spark at path: "
              + userSpecifiedJarPath + "*.(jar|py)");
    } else if (potentialExecutionJarList.length > 1) {
      throw new IllegalStateException(
              "I find more than one matching instance of the execution jar at the path, don't know which one to use: "
                      + userSpecifiedJarPath + "*.(jar|py)");
    }

    String resolvedJarName = potentialExecutionJarList[0].toString();
    log.info("Resolving execution jar/py name: resolvedJarName: " + resolvedJarName);
    return resolvedJarName;
  }

  /**
   * 
   * @return a list of files in the given folder that matches the regex. It may be empty, but will
   *         never return a null
   * @throws FileNotFoundException
   */
  private static File[] getFilesInFolderByRegex(File folder, final String regex)
          throws FileNotFoundException {
    // sanity check

    if (!folder.exists()) {
      throw new FileNotFoundException();

    }
    if (!folder.isDirectory()) {
      throw new IllegalStateException(
              "execution jar is suppose to be in this folder, but the object present is not a directory: "
                      + folder);
    }

    File[] matchingFiles = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (name.matches(regex)) {
          return true;
        }
        else {
          return false;
        }
      }
    });

    if (matchingFiles == null) {
      throw new IllegalStateException(
              "the File[] matchingFiles variable is null.  This means an IOException occured while doing listFiles.  Please check disk availability and retry again");
    }

    return matchingFiles;
  }

  /**
   * Pass in a log file, this method will find all the hadoop jobs it has launched, and kills it
   * 
   * Only works with Hadoop2
   * 
   * @param logFilePath
   * @param log
   * @return a Set<String>. The set will contain the applicationIds that this job tried to kill.
   */
  public static Set<String> killAllSpawnedHadoopJobs(String logFilePath, Logger log) {
    Set<String> allSpawnedJobs = findIdFromLog(logFilePath, log, APPLICATION_ID_PATTERN);
    log.info("applicationIds to kill: " + allSpawnedJobs);

    for (String appId : allSpawnedJobs) {
      try {
        killJobOnCluster(appId, log);
      } catch (Throwable t) {
        log.warn("something happened while trying to kill this job: " + appId, t);
      }
    }

    return allSpawnedJobs;
  }

  public static void killAllHadoopJobs(String logFilePath, Logger log) {
    Set<String> allSpawnedJobs = new HashSet<>();
    try {
      allSpawnedJobs = findIdFromLog(logFilePath, log, APPLICATION_ID_PATTERN);
    } catch (Exception e){
      log.warn(e.getMessage());
    }
    log.info("applicationIds to kill: " + allSpawnedJobs);

    for (String appId : allSpawnedJobs) {
      try {
        killByCommand(String.format("yarn application -kill %s", appId), log);
      } catch (Throwable t) {
        log.warn("something happened while trying to kill this job: " + appId, t);
      }
    }
  }

  public static void killBdpClientJob(List<String> commands, String logFilePath, Logger log){
    if(CollectionUtils.isEmpty(commands)){
      log.info("command is empty.");
      return;
    }
    Set<String> jobIds = new HashSet<>();
    try {
      jobIds = findIdFromLog(logFilePath, log, BDP_CLIENT_JOB_ID_PATTERN);
    } catch (Exception e){
      log.warn(e.getMessage());
    }
    log.info("bdp client job ids: " + jobIds);
    for (String jobId : jobIds) {
      try {
        killByCommand(String.format("bdp-client job kill -j %s", jobId), log);
      } catch (Throwable t) {
        log.warn("something happened while trying to kill this job: " + jobId, t);
      }
    }
  }


  /**
   * <pre>
   * Takes in a log file, will grep every line to look for the application_id pattern.
   * If it finds multiple, it will return all of them, de-duped (this is possible in the case of pig jobs)
   * This can be used in conjunction with the @killJobOnCluster method in this file.
   * </pre>
   * 
   * @param logFilePath
   * @return a Set. May be empty, but will never be null
   */
  public static Set<String> findIdFromLog(String logFilePath, Logger log, Pattern pattern) {

    File logFile = new File(logFilePath);

    if (!logFile.exists()) {
      throw new IllegalArgumentException("the logFilePath does not exist: " + logFilePath);
    }
    if (!logFile.isFile()) {
      throw new IllegalArgumentException("the logFilePath specified  is not a valid file: "
              + logFilePath);
    }
    if (!logFile.canRead()) {
      throw new IllegalArgumentException("unable to read the logFilePath specified: " + logFilePath);
    }

    BufferedReader br = null;
    Set<String> applicationIds = new HashSet<String>();

    try {
      br = new BufferedReader(new FileReader(logFile));
      String line;

      // finds all the application IDs
      while ((line = br.readLine()) != null) {
        String [] inputs = line.split("\\s");
        if (inputs != null) {
          for (String input : inputs) {
            Matcher m = pattern.matcher(input);
            if (m.find()) {
              String appId = m.group(0);
              applicationIds.add(appId);
            }
          }
        }
      }
    } catch (IOException e) {
      log.error("Error while trying to find applicationId for log", e);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (Exception e) {
        // do nothing
      }
    }
    return applicationIds;
  }

  /**
   * <pre>
   * Uses YarnClient to kill the job on HDFS.
   * Using JobClient only works partially:
   *   If yarn container has started but spark job haven't, it will kill
   *   If spark job has started, the cancel will hang until the spark job is complete
   *   If the spark job is complete, it will return immediately, with a job not found on job tracker
   * </pre>
   * 
   * @param applicationId
   * @throws IOException
   * @throws YarnException
   */
  public static void killJobOnCluster(String applicationId, Logger log) throws YarnException,
          IOException {

    YarnConfiguration yarnConf = new YarnConfiguration();
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(yarnConf);
    yarnClient.start();

    String[] split = applicationId.split("_");
    ApplicationId aid = ApplicationId.newInstance(Long.parseLong(split[1]),
            Integer.parseInt(split[2]));

    log.info("start klling application: " + aid);
    yarnClient.killApplication(aid);
    yarnClient.close();
    log.info("successfully killed application: " + aid);
  }

  public static void killByCommand(String cmd, Logger log) {
    //yarn application -kill %s" , bdp-client job kill -j %s
    log.info("exec cmd: " + cmd);
    try {
      Runtime.getRuntime().exec(cmd);
    }catch (IOException io){
      log.error("exec cmd failed.", io);
    }
  }

  /**
   * <pre>
   * constructions a javaOpts string based on the Props, and the key given, will return 
   *  String.format("-D%s=%s", key, value);
   * </pre>
   * 
   * @param props
   * @param key
   * @return will return String.format("-D%s=%s", key, value). Throws RuntimeException if props not
   *         present
   */
  public static String javaOptStringFromAzkabanProps(Props props, String key) {
    String value = props.get(key);
    if (value == null) {
      throw new RuntimeException(String.format("Cannot find property [%s], in azkaban props: [%s]",
              key, value));
    }
    return String.format("-D%s=%s", key, value);
  }
  
  /**
   * Filter a collection of String commands to match a whitelist regex and not match a blacklist
   * regex.
   * 
   * @param commands
   *          Collection of commands to be filtered
   * @param whitelistRegex
   *          whitelist regex to work as inclusion criteria
   * @param blacklistRegex
   *          blacklist regex to work as exclusion criteria
   * @param log
   *          logger to report violation
   * @return filtered list of matching. Empty list if no command match all the criteria.
   */
  public static List<String> filterCommands(Collection<String> commands, String whitelistRegex,
          String blacklistRegex, Logger log) {
    List<String> filteredCommands = new LinkedList<String>();
    Pattern whitelistPattern = Pattern.compile(whitelistRegex);
    Pattern blacklistPattern = Pattern.compile(blacklistRegex);
    for (String command : commands) {
      if (whitelistPattern.matcher(command).matches()
              && !blacklistPattern.matcher(command).matches()) {
        filteredCommands.add(command);
      } else {
        log.warn(String.format("Removing restricted command: %s", command));
      }
    }
    return filteredCommands;
  }

  /**
   * <pre>
   * constructions a javaOpts string based on the Props, and the key given, will return 
   *  String.format("-D%s=%s", key, value);
   * </pre>
   * 
   * @param conf
   * @param key
   * @return will return String.format("-D%s=%s", key, value). Throws RuntimeException if props not
   *         present
   */
  public static String javaOptStringFromHadoopConfiguration(Configuration conf, String key) {
    String value = conf.get(key);
    if (value == null) {
      throw new RuntimeException(String.format("Cannot find property [%s], in Hadoop configuration: [%s]",
              key, value));
    }
    return String.format("-D%s=%s", key, value);
  }
}
