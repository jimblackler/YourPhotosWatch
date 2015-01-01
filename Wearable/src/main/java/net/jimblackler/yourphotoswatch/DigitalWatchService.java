package net.jimblackler.yourphotoswatch;

public class DigitalWatchService extends BaseWatchService {

  @Override
  protected float getDigitalTextSize() {
    return 55;
  }

  @Override
  protected float getDigitalOffset() {
    return -70;
  }

  @Override
  protected boolean isAnalog() {
    return false;
  }

  @Override
  protected boolean isDigital() {
    return true;
  }

  @Override
  protected boolean isAmPm() {
    return false;
  }
}