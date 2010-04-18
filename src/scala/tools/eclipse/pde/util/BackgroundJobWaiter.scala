package scala.tools.eclipse.pde.util

import org.eclipse.swt.widgets.Display
import org.eclipse.core.runtime.jobs.Job

trait BackgroundJobWaiter {
  protected def waitForBackgroundJobs = {
    while (Job.getJobManager.isIdle) delay(1000)
  }
  
  private def delay(timeToDelayFor: Int) = {
    val display = Display.getCurrent
    
    if (display != null) {
      delayInsideUIThread(display, timeToDelayFor)
    } else {
      delayOutsideUIThread(timeToDelayFor)
    }
  }
  
  private def delayInsideUIThread(display: Display, timeToDelayFor: Int) = {
    val endTimeMillis = System.currentTimeMillis + timeToDelayFor
    while (System.currentTimeMillis < endTimeMillis)
        if (!display.readAndDispatch) display.sleep
    display.update
  }

  private def delayOutsideUIThread(timeToDelayFor: Int) = {
    try {
      Thread.sleep(timeToDelayFor)
    } catch {
      case _: InterruptedException => Thread.currentThread.interrupt
    }
  }
}