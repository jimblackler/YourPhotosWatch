package net.jimblackler.yourphotoswatch;

public class AnalogWatchService extends BaseWatchService {
  @Override
  protected float getDigitalTextSize() {
    return 0;
  }

  @Override
  protected float getDigitalOffset() {
    return 0;
  }

  @Override
  protected boolean isAnalog() {
    return true;
  }

  @Override
  protected boolean isDigital() {
    return false;
  }

  @Override
  protected boolean isAmPm() {
    return false;
  }
}
