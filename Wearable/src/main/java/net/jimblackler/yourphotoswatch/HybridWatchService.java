package net.jimblackler.yourphotoswatch;

public class HybridWatchService extends BaseWatchService {

  @Override
  protected float getDigitalTextSize() {
    return 45;
  }

  @Override
  protected float getDigitalOffset() {
    return -50;
  }

  @Override
  protected boolean isAnalog() {
    return true;
  }

  @Override
  protected boolean isDigital() {
    return true;
  }

  @Override
  protected boolean isAmPm() {
    return true;
  }
}
