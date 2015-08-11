package com.jetbrains.ther.packages;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.PackageManagementService;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import com.intellij.webcore.packaging.RepoPackage;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author avesloguzova
 */
public class TheRPackageTaskManager {
  public static final String R_INSTALL_PACKAGE = "r-packages/r-packages-install.r";
  private final Project myProject;
  private final TaskListener myListener;

  TheRPackageTaskManager(@Nullable Project project, @NotNull TaskListener listener) {
    myProject = project;
    myListener = listener;
  }

  public static void installPackage(@NotNull RepoPackage repoPackage)
    throws ExecutionException {
    TheRPackagesUtil.TheRRunResult result;
    List<String> args = TheRPackagesUtil.getHelperRepositoryArguments();
    args.add(0, repoPackage.getName());
    try {
      result = TheRPackagesUtil.runHelperWithArgs(R_INSTALL_PACKAGE, args.toArray(new String[args.size()]));
    }
    catch (IOException e) {
      throw new ExecutionException("Some I/O errors occurs while installing");
    }
    if (result == null) {
      throw new ExecutionException("Path to interpreter didn't set");
    }
    final String stderr = result.getStdErr();
    if (!stderr.contains(String.format("DONE (%s)", repoPackage.getName()))) {

      throw new TheRExecutionException("Some error during the installation", result.getCommand(), result.getStdOut(), result.getStdErr(),
                                       result.getExitCode());
    }
  }

  public static void uninstallPackage(List<InstalledPackage> repoPackage) throws ExecutionException {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new ExecutionException("Path to interpreter didn't set");
    }
    StringBuilder commandBuilder = getRemovePackageCommand(path, repoPackage);
    String command = commandBuilder.toString();
    Process process;
    try {
      process = Runtime.getRuntime().exec(command);
    }
    catch (IOException e) {
      throw new ExecutionException("Some I/O errors occurs while installing");
    }
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess((int)(5 * DateFormatUtil.MINUTE));
    if (output.getExitCode() != 0) {
      throw new TheRExecutionException("Can't remove package", command, output.getStdout(), output.getStderr(), output.getExitCode());
    }
  }

  private static StringBuilder getRemovePackageCommand(String path, List<InstalledPackage> repoPackage) {
    StringBuilder commandBuilder = new StringBuilder();
    commandBuilder.append(path).append(" CMD REMOVE");
    for (InstalledPackage aRepoPackage : repoPackage) {
      commandBuilder.append(" ").append(aRepoPackage.getName());
    }
    return commandBuilder;
  }

  public void install(RepoPackage pkg) {
    ProgressManager.getInstance().run(new InstallTask(myProject, myListener, pkg));
  }

  public void uninstall(List<InstalledPackage> installedPackages) {
    ProgressManager.getInstance().run(new UninstallTask(myProject, myListener, installedPackages));
  }

  public interface TaskListener {
    void started();

    void finished(List<ExecutionException> exceptions);
  }

  public abstract static class PackagingTask extends Task.Backgroundable {

    private static final String PACKAGING_GROUP_ID = "Packaging";
    private TaskListener myListener;

    PackagingTask(@Nullable Project project, @NotNull String title, @NotNull TaskListener listener) {
      super(project, title);
      myListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
      taskStarted(indicator);
      taskFinished(runTask(indicator));
    }

    protected void taskStarted(@NotNull ProgressIndicator indicator) {
      final Notification[] notifications =
        NotificationsManager.getNotificationsManager().getNotificationsOfType(Notification.class, getProject());
      for (Notification notification : notifications) {
        notification.expire();
      }
      indicator.setText(getTitle() + "...");
      if (myListener != null) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            myListener.started();
          }
        });
      }
    }

    protected void taskFinished(@NotNull final List<ExecutionException> exceptions) {
      final Ref<Notification> notificationRef = new Ref<Notification>(null);
      if (exceptions.isEmpty()) {
        notificationRef.set(new Notification(PACKAGING_GROUP_ID, getSuccessTitle(), getSuccessDescription(),
                                             NotificationType.INFORMATION, null));
      }
      else {
        final PackageManagementService.ErrorDescription description = TheRPackageManagementService.toErrorDescription(exceptions);
        if (description != null) {
          final String firstLine = getTitle() + ": error occurred.";
          final NotificationListener listener = new NotificationListener() {
            @Override
            public void hyperlinkUpdate(@NotNull Notification notification,
                                        @NotNull HyperlinkEvent event) {
              assert myProject != null;
              final String title = StringUtil.capitalizeWords(getFailureTitle(), true);
              PackagesNotificationPanel.showError(title, description);
            }
          };
          notificationRef.set(new Notification(PACKAGING_GROUP_ID, getFailureTitle(), firstLine + " <a href=\"xxx\">Details...</a>",
                                               NotificationType.ERROR, listener));
        }
      }
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          if (myListener != null) {
            myListener.finished(exceptions);
          }
          final Notification notification = notificationRef.get();
          if (notification != null) {
            notification.notify(myProject);
          }
        }
      });
    }

    @NotNull
    protected abstract List<ExecutionException> runTask(@NotNull ProgressIndicator indicator);

    @NotNull
    protected abstract String getSuccessTitle();

    @NotNull
    protected abstract String getSuccessDescription();

    @NotNull
    protected abstract String getFailureTitle();
  }

  public static class InstallTask extends PackagingTask {
    RepoPackage myPackage;

    InstallTask(@Nullable Project project,
                @NotNull TaskListener listener,
                @NotNull RepoPackage repoPackage) {
      super(project, "Install packages", listener);//TODO add title
      myPackage = repoPackage;
    }

    @NotNull
    @Override
    protected List<ExecutionException> runTask(@NotNull ProgressIndicator indicator) {
      final List<ExecutionException> exceptions = new ArrayList<ExecutionException>();

      try {
        installPackage(myPackage);
      }
      catch (ExecutionException e) {
        exceptions.add(e);
      }
      return exceptions;
    }

    @NotNull
    @Override
    protected String getSuccessTitle() {
      return "Package installed successfully";
    }

    @NotNull
    @Override
    protected String getSuccessDescription() {
      return "Installed package " + myPackage.getName();
    }

    @NotNull
    @Override
    protected String getFailureTitle() {
      return "Install package failed";
    }
  }

  public static class UninstallTask extends PackagingTask {
    private List<InstalledPackage> myPackages;

    UninstallTask(@Nullable Project project,
                  @NotNull TaskListener listener,
                  @NotNull List<InstalledPackage> packages) {
      super(project, "Uninstall packages", listener);
      myPackages = packages;
    }

    @NotNull
    @Override
    protected List<ExecutionException> runTask(@NotNull ProgressIndicator indicator) {
      final List<ExecutionException> exceptions = new ArrayList<ExecutionException>();

      try {
        uninstallPackage(myPackages);
      }
      catch (ExecutionException e) {
        exceptions.add(e);
      }
      return exceptions;
    }

    @NotNull
    @Override
    protected String getSuccessTitle() {
      return "Packages uninstalled successfully";
    }

    @NotNull
    @Override
    protected String getSuccessDescription() {
      final String packagesString = StringUtil.join(myPackages, new Function<InstalledPackage, String>() {
        @Override
        public String fun(InstalledPackage pkg) {
          return "'" + pkg.getName() + "'";
        }
      }, ", ");
      return "Uninstalled packages: " + packagesString;
    }

    @NotNull
    @Override
    protected String getFailureTitle() {
      return "Uninstall packages failed";
    }
  }
}
