package com.jetbrains.ther.packages;

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.RepoPackage;
import com.jetbrains.ther.TheRHelpersLocator;
import com.jetbrains.ther.TheRUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author avesloguzova
 */
public final class TheRPackagesUtil {

  public static final String R_PACKAGES_DEFAULT_REPOS = "r-packages/r-packages-default-repos.r";
  public static final String R_PACKAGES_DETAILS = "r-packages/r-packages-details.r";
  private static final Pattern urlPattern = Pattern.compile("\".+\"");
  private static final Logger LOG = Logger.getInstance(TheRPackagesUtil.class.getName());
  private static final Set<String> basePackages = new HashSet<String>(Arrays.asList(
    new String[]{"base", "utils", "stats", "datasets", "graphics",
      "grDevices", "grid", "methods", "tools", "parallel", "compiler", "splines", "tcltk", "stats4"}));

  public static boolean isPackageBase(InstalledPackage pkg) {
    return basePackages.contains(pkg.getName());
  }

  public static String getHelperSuccsessOutput(String helper) throws IOException {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      LOG.info("Path to interpreter didn't set");
      return null;
    }
    final String helperPath = TheRHelpersLocator.getHelperPath(helper);
    final Process process = Runtime.getRuntime().exec(path + " --slave -f " + helperPath);
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess((int)(5 * DateFormatUtil.MINUTE));
    if (output.getExitCode() != 0) {
      LOG.error("Failed to run script. Exit code: " + output.getExitCode());
      LOG.error(output.getStderrLines());
    }
    return output.getStdout();
  }

  public static void setRepositories(List<Integer> defaultRepositories,
                                     List<String> userRepositories) {
    TheRPackageService service = TheRPackageService.getInstance();
    service.defaultRepos.clear();
    service.defaultRepos.addAll(defaultRepositories);
    service.userRepos.clear();
    service.userRepos.addAll(userRepositories);
  }


  @NotNull
  public static List<TheRDefaultRepository> getDeafaultRepositories() {
    try {
      String output = getHelperSuccsessOutput(R_PACKAGES_DEFAULT_REPOS);
      if (output != null) {
        return toDefaultPackages((output));
      }
    }
    catch (IOException e) {
      LOG.error("Can't run R interpreter.");
    }
    return Lists.newArrayList();
  }

  private static List<TheRDefaultRepository> toDefaultPackages(String output) {
    List<String> urls = getURLs(output);
    List<TheRDefaultRepository> repos = Lists.newArrayList();
    for (int i = 0; i < urls.size(); i++) {
      repos.add(new TheRDefaultRepository(urls.get(i), i + 1));
    }
    return repos;
  }

  @NotNull
  public static List<String> getEnabledRepositories() {
    TheRPackageService service = TheRPackageService.getInstance();
    List<TheRDefaultRepository> defaultRepo = getDeafaultRepositories();
    List<Integer> enabledDefaultRepo = service.defaultRepos;
    List<String> usersRepo = service.userRepos;
    List<String> result = Lists.newArrayList();
    result.addAll(usersRepo);
    for (Integer i : enabledDefaultRepo) {
      result.add(defaultRepo.get(i - 1).getUrl());
    }
    return result;
  }


  @NotNull
  public static List<String> getCRANMirrors() {

    ProcessOutput output = TheRUtils.getProcessOutput("getCRANmirrors()[,\"URL\"]");
    if (output != null && output.getExitCode() == 0) {
      return getURLs(output.getStdout());
    }
    return Lists.newArrayList();
  }

  @NotNull
  private static List<String> getURLs(@NotNull String stdout) {
    List<String> reposURL = Lists.newArrayList();
    final Matcher matcher = urlPattern.matcher(stdout);
    while (matcher.find()) {
      reposURL.add(matcher.group().replace('\"', ' ').trim());
    }
    return reposURL;
  }

  @Nullable
  public static TheRRunResult runHelperWithArgs(@NotNull String helper, String... args) throws IOException {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      LOG.info("Path to interpreter didn't set");
      return null;
    }
    String command = getHelperCommand(helper, path, args);
    Process process;
    process = Runtime.getRuntime().exec(command);
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess((int)(5 * DateFormatUtil.MINUTE));
    if (output.getExitCode() != 0) {
      LOG.error("Failed to run script. Exit code: " + output.getExitCode());
      LOG.error(output.getStderrLines());
    }
    return new TheRRunResult(command, output);
  }

  @NotNull
  private static String getHelperCommand(@NotNull String helper, String path, String[] args) {
    final String helperPath = TheRHelpersLocator.getHelperPath(helper);
    StringBuilder execStr = new StringBuilder();
    execStr.append(path).append(" --slave -f ").append(helperPath).append(" --args");
    for (String arg : args) {
      execStr.append(TheRPackageInformationLoadUtil.ARGUMENT_DELIMETER).append(arg);
    }
    return execStr.toString();
  }

  @NotNull
  public static List<String> getHelperRepositoryArguments() {
    TheRPackageService service = TheRPackageService.getInstance();
    List<String> args = Lists.newArrayList();
    args.add(String.valueOf(service.CRANMirror + 1));
    if (service.defaultRepos.size() > 0) {
      args.add(String.valueOf(service.defaultRepos.size()));
      for (Integer repository : service.defaultRepos) {
        args.add(String.valueOf(repository));
      }
    }
    else {
      args.add(String.valueOf(1));
      args.add(String.valueOf(1));
    }
    args.addAll(service.userRepos);
    return args;
  }


  public static class TheRRunResult {
    private final String myCommand;
    private final String myStdOut;
    private final String myStdErr;
    private int myExitCode;

    public TheRRunResult(@NotNull String command, @NotNull ProcessOutput output) {
      this.myCommand = command;
      this.myExitCode = output.getExitCode();
      this.myStdOut = output.getStdout();
      this.myStdErr = output.getStderr();
    }

    @NotNull
    public String getCommand() {
      return myCommand;
    }

    @NotNull
    public String getStdOut() {
      return myStdOut;
    }

    @NotNull
    public String getStdErr() {
      return myStdErr;
    }

    public int getExitCode() {
      return myExitCode;
    }
  }
}