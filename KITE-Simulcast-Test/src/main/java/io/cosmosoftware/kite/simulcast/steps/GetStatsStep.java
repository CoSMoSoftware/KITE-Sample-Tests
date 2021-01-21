package io.cosmosoftware.kite.simulcast.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.List;
import org.openqa.selenium.WebDriver;

import javax.json.JsonObject;
import org.webrtc.kite.stats.rtc.RTCStatList;
import org.webrtc.kite.stats.rtc.RTCStats;

import static org.webrtc.kite.stats.StatsUtils.getPCStatOvertime;
import static org.webrtc.kite.stats.StatsUtils.transformToJson;

public class GetStatsStep extends TestStep {

  private final JsonObject getStatsConfig;

  public GetStatsStep(Runner runner, JsonObject getStatsConfig) {
    super(runner);
    this.getStatsConfig = getStatsConfig;
  }

  
  
  @Override
  public String stepDescription() {
    return "GetStats";
  }
  
  @Override
  protected void step() throws KiteTestException {
    try {
      RTCStatList localPcStats = getPCStatOvertime(webDriver, getStatsConfig).get(0);
      reporter.jsonAttachment(report, "getStatsRaw", transformToJson(localPcStats));
    } catch (Exception e) {
      e.printStackTrace();
      throw new KiteTestException("Failed to getStats", Status.BROKEN, e);
    }
  }
}
