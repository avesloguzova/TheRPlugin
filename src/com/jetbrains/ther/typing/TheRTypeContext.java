package com.jetbrains.ther.typing;

import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import com.jetbrains.ther.typing.types.TheRType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TheRTypeContext {
  private static final Map<Project, TheRTypeContext> CONTEXT = new HashMap<Project, TheRTypeContext>();
  private final Map<TheRPsiElement, TheRType> cache = new HashMap<TheRPsiElement, TheRType>();
  private final Lock cacheLock = new ReentrantLock();
  private long myModificationCount = -1;

  private TheRTypeContext(long modificationCount) {
    myModificationCount = modificationCount;
  }

  public static TheRType getTypeFromCache(TheRPsiElement element) {
    final Project project = element.getProject();
    final PsiModificationTracker tracker = PsiModificationTracker.SERVICE.getInstance(project);
    TheRTypeContext context;
    synchronized (CONTEXT) {
      context = CONTEXT.get(project);
      final long count = tracker.getModificationCount();
      if (context == null || context.myModificationCount != count) {
        context = new TheRTypeContext(count);
        CONTEXT.put(project, context);
      }
    }
    return context.getType(element);
  }


  public TheRType getType(TheRPsiElement element) {
    TheREvaluatingNowType evaluatingType = new TheREvaluatingNowType();
    cacheLock.lock();
    TheRType type = cache.get(element);
    if (type != null) {
      if (type instanceof TheREvaluatingNowType) {
        cacheLock.unlock();
        evaluatingType = (TheREvaluatingNowType)type;
        try {
          //noinspection SynchronizationOnLocalVariableOrMethodParameter
          synchronized (evaluatingType) {
            if (evaluatingType.isNotRecursive()) {
              while (!evaluatingType.isReady()) {
                evaluatingType.wait(5000);
              }
            }
          }
        }
        catch (InterruptedException e) {
          //
        }
        return evaluatingType.getResult();
      }
      cacheLock.unlock();
      return type;
    }
    cache.put(element, evaluatingType);
    cacheLock.unlock();
    type = TheRTypeProvider.buildType(element);
    cacheLock.lock();
    cache.put(element, type);
    cacheLock.unlock();
    evaluatingType.setResult(type);
    return type;
  }

  private static class TheREvaluatingNowType extends TheRType {
    private TheRType myResult;
    private volatile boolean myReady;
    private final long myThreadId;

    private TheREvaluatingNowType() {
      myResult = TheRType.UNKNOWN;
      myThreadId = Thread.currentThread().getId();
    }

    @Override
    public String getName() {
      return "evaluating now";
    }

    public TheRType getResult() {
      return myResult;
    }

    public synchronized void setResult(TheRType result) {
      myResult = result;
      myReady = true;
      notifyAll();
    }

    public boolean isReady() {
      return myReady;
    }

    public boolean isNotRecursive() {
      return Thread.currentThread().getId() != myThreadId;
    }
  }
}
