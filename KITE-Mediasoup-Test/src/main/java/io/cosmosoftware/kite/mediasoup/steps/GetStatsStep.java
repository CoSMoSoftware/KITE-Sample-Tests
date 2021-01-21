package io.cosmosoftware.kite.mediasoup.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.LinkedHashMap;
import org.webrtc.kite.stats.rtc.RTCStatList;
import org.webrtc.kite.stats.rtc.RTCStatMap;
import org.webrtc.kite.stats.rtc.RTCStats;
import org.webrtc.kite.stats.StatsUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

import static org.webrtc.kite.stats.StatsUtils.buildStatSummary;
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
    logger.info("Getting WebRTC stats via getStats");
    try {
      RTCStatMap statsOverTime =  getPCStatOvertime(webDriver, getStatsConfig);
      RTCStatList localPcStats = statsOverTime.getLocalPcStats();
      reporter.jsonAttachment(this.report, "Stats (Raw)", transformToJson(localPcStats));
      reporter.jsonAttachment(this.report, "Stats Summary", buildStatSummary(localPcStats));
    } catch (Exception e) {
      e.printStackTrace();
      throw new KiteTestException("Failed to getStats", Status.BROKEN, e);
    }
  }
}
