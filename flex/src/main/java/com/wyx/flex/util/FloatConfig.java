package com.wyx.flex.util;

/**
 * @author winney E-mail: 542111388@qq.com
 * @version 创建时间: 2016/09/07 上午11:04
 */

public class FloatConfig {
  private boolean startOnLaunch = true;
  private boolean showLogCatWindow = true;
  private boolean logCatEnabled = true;
  private boolean triggerEnabled = true;

  public boolean isStartOnLaunch() {
    return startOnLaunch;
  }

  public void setStartOnLaunch(boolean startOnLaunch) {
    this.startOnLaunch = startOnLaunch;
  }

  public boolean isShowLogCatWindow() {
    return showLogCatWindow;
  }

  public void setShowLogCatWindow(boolean showLogCatWindow) {
    this.showLogCatWindow = showLogCatWindow;
  }

  public boolean isLogCatEnabled() {
    return logCatEnabled;
  }

  public void setLogCatEnabled(boolean logCatEnabled) {
    this.logCatEnabled = logCatEnabled;
  }

  public boolean isTriggerEnabled() {
    return triggerEnabled;
  }

  public void setTriggerEnabled(boolean triggerEnabled) {
    this.triggerEnabled = triggerEnabled;
  }

  public static class Builder {
    FloatConfig config;

    public Builder() {
      this.config = new FloatConfig();
    }

    public Builder setStartOnLaunch(boolean startOnLaunch) {
      this.config.setStartOnLaunch(startOnLaunch);
      return this;
    }

    public Builder setShowLogCatWindow(boolean showLogCatWindow) {
      this.config.setShowLogCatWindow(showLogCatWindow);
      return this;
    }

    public Builder setLogCatEnabled(boolean logCatEnabled) {
      this.config.setLogCatEnabled(logCatEnabled);
      return this;
    }

    public Builder setTriggerEnabled(boolean triggerEnabled) {
      this.config.setTriggerEnabled(triggerEnabled);
      return this;
    }

    public FloatConfig create() {
      return config;
    }
  }
}
