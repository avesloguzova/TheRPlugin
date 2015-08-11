package com.jetbrains.ther.packages;

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.CatchingConsumer;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.RepoPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheRPackageInformationLoadUtil {
  public static final String R_INSTALLED_PACKAGES = "r-packages/r-packages-installed.r";
  public static final String R_ALL_PACKAGES = "r-packages/r-packages-all.r";
  public static final String ARGUMENT_DELIMETER = " ";

  public static List<InstalledPackage> getInstalledPackages() throws IOException {
    final ArrayList<InstalledPackage> installedPackages = Lists.newArrayList();
    final String stdout = TheRPackagesUtil.getHelperSuccsessOutput(R_INSTALLED_PACKAGES);
    if (stdout == null) {
      return installedPackages;
    }
    final String[] splittedOutput = StringUtil.splitByLines(stdout);
    for (String line : splittedOutput) {
      final List<String> packageAttributes = StringUtil.split(line, ARGUMENT_DELIMETER);
      if (packageAttributes.size() == 4) {
        final InstalledPackage theRPackage =
          new InstalledPackage(packageAttributes.get(1).replace("\"", ""), packageAttributes.get(2).replace("\"", ""));
        installedPackages.add(theRPackage);
      }
    }
    return installedPackages;
  }

  @Nullable
  public static List<RepoPackage> getAvailablePackages() throws IOException {
    List<String> args = TheRPackagesUtil.getHelperRepositoryArguments();
    final TheRPackagesUtil.TheRRunResult
      result = TheRPackagesUtil.runHelperWithArgs(R_ALL_PACKAGES, args.toArray(new String[args.size()]));
    if (result == null || result.getExitCode() != 0) {
      return null;
    }
    Map<String, String> packages = getPackages();
    packages.clear();
    List<RepoPackage> packageList = Lists.newArrayList();
    final String[] splittedOutput = StringUtil.splitByLines(result.getStdOut());
    for (String line : splittedOutput) {
      final List<String> packageAttributes = StringUtil.split(line, ARGUMENT_DELIMETER);
      if (packageAttributes.size() >= 3) {
        RepoPackage repoPackage = new RepoPackage(packageAttributes.get(1).replace("\"", ""), packageAttributes.get(3).replace("\"", ""),
                                                  packageAttributes.get(2).replace("\"", ""));
        packages.put(repoPackage.getName(), repoPackage.getLatestVersion() + ARGUMENT_DELIMETER + repoPackage.getRepoUrl());
        packageList.add(repoPackage);
      }
    }
    return packageList;
  }

  public static void fetchPackageDetails(@NotNull final String packageName, final CatchingConsumer<String, Exception> consumer) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          String details = loadPackageDetails(packageName);
          consumer.consume(formatDetails(details));
        }
        catch (IOException e) {
          consumer.consume(e);
        }
        catch (ExecutionException e) {
          consumer.consume(e);
        }
      }
    });
  }

  private static String loadPackageDetails(@NotNull String packageName) throws IOException, ExecutionException {
    TheRPackagesUtil.TheRRunResult result;
    List<String> args = TheRPackagesUtil.getHelperRepositoryArguments();
    args.add(0, packageName);

    result = TheRPackagesUtil.runHelperWithArgs(TheRPackagesUtil.R_PACKAGES_DETAILS, args.toArray(new String[args.size()]));
    if (result != null && result.getExitCode() == 0) {
      return result.getStdOut();
    }
    else {
      if (result == null) {
        throw new ExecutionException("Can't fetch package details.");
      }
      throw new TheRExecutionException("Can't fetch package details.", result.getCommand(), result.getStdOut(), result.getStdErr(),
                                       result.getExitCode());
    }
  }

  private static String formatDetails(String details) {
    String[] splittedString = details.split("\n");
    StringBuilder builder = new StringBuilder();
    for (String string : splittedString) {
      builder.append(string);
      builder.append("<br>");
    }
    return builder.toString();
  }

  public static Map<String, String> getPackages() {
    return TheRPackageService.getInstance().allPackages;
  }

  public static List<RepoPackage> getOrLoadPackages() throws IOException {
    Map<String, String> nameVersionMap = getPackages();
    if (nameVersionMap.isEmpty()) {
      getAvailablePackages();
      nameVersionMap = getPackages();
    }
    return versionMapToPackageList(nameVersionMap);
  }

  private static List<RepoPackage> versionMapToPackageList(Map<String, String> packageToVersionMap) {

    List<RepoPackage> packages = new ArrayList<RepoPackage>();
    for (Map.Entry<String, String> entry : packageToVersionMap.entrySet()) {
      String[] splitted = entry.getValue().split(ARGUMENT_DELIMETER);
      packages.add(new RepoPackage(entry.getKey(), splitted[1], splitted[0]));
    }
    return packages;
  }
}
