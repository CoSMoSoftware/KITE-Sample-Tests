package io.cosmosoftware.kite.jitsi.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.jitsi.pages.MeetingPage;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.steps.TestStep;
import java.util.LinkedHashMap;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;

import javax.json.JsonObject;
import org.webrtc.kite.stats.rtc.RTCStatList;
import org.webrtc.kite.stats.rtc.RTCStatMap;
import org.webrtc.kite.stats.rtc.RTCStats;

import static org.webrtc.kite.stats.StatsUtils.buildStatSummary;
import static org.webrtc.kite.stats.StatsUtils.getPCStatOvertime;
import static org.webrtc.kite.stats.StatsUtils.transformToJson;

public class GetStatsStep extends TestStep {
  
  private final JsonObject getStatsConfig;
  private final MeetingPage meetingPage;

  public GetStatsStep(Runner runner, JsonObject getStatsConfig) {
    super(runner);
    this.getStatsConfig = getStatsConfig;
    this.meetingPage = new MeetingPage(runner);
  }

  @Override
  public String stepDescription() {
    return "Getting Jitsi conference statistics";
  }

  @Override
  protected void step() throws KiteTestException {
    ((JavascriptExecutor) webDriver).executeScript(meetingPage.getPeerConnectionScript());
    RTCStatMap statsOverTime =  getPCStatOvertime(webDriver, getStatsConfig);
    RTCStatList localPcStats = statsOverTime.getLocalPcStats();
    reporter.jsonAttachment(this.report, "Stats (Raw)", transformToJson(localPcStats));
    reporter.jsonAttachment(this.report, "Stats Summary", buildStatSummary(localPcStats));
  }
}
