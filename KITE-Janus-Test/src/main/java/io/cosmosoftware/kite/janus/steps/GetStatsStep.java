package io.cosmosoftware.kite.janus.steps;

import io.cosmosoftware.kite.exception.KiteTestException;
import io.cosmosoftware.kite.interfaces.Runner;
import io.cosmosoftware.kite.janus.pages.JanusPage;
import io.cosmosoftware.kite.report.Reporter;
import io.cosmosoftware.kite.report.Status;
import io.cosmosoftware.kite.steps.StepPhase;
import io.cosmosoftware.kite.steps.TestStep;
import org.openqa.selenium.WebDriver;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.webrtc.kite.stats.rtc.RTCStatList;
import org.webrtc.kite.stats.rtc.RTCStatMap;
import org.webrtc.kite.stats.rtc.RTCStats;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;
import static org.webrtc.kite.stats.StatsUtils.*;
import static org.webrtc.kite.stats.StatsUtils.buildStatSummary;
import static org.webrtc.kite.stats.StatsUtils.transformToJson;

public class GetStatsStep extends TestStep {


  private final JsonObject getStatsConfig;
  private final boolean sfu;
  private final JanusPage janusPage;

  /**
   * for Janus demo testing, the name of the local peer connection are like "pluginHandle" + webrtcStuff.pc in the website source code (https://janus.conf.meetecho.com)
   * list of pluginHandle (corresponding Plugin Demo name):
   * - echotest (Echo Test)
   * - streaming (Streaming)
   * - sfutest (Video Room)
   * - videocall (Video Call)
   * - screentest (Screen Share)
   * <p>
   * see configs file to set the name of the peer connection for each test (key: 'peerConnection')
   */

  public GetStatsStep(Runner runner, JsonObject getStatsConfig, boolean sfu, JanusPage janusPage) {
    super(runner);
    this.janusPage = janusPage;
    this.getStatsConfig = getStatsConfig;
    this.sfu = sfu;
    setStepPhase(StepPhase.ALL);
  }


  @Override
  public String stepDescription() {
    return "GetStats";
  }

  @Override
  protected void step() throws KiteTestException {
    logger.info("Getting WebRTC stats via getStats");
    LinkedHashMap<String, String> results = new LinkedHashMap<>();
    try {
      if (sfu) {
        RTCStatMap pcStatMap = new RTCStatMap();
        List<String> remotePC = janusPage.getRemotePC();
        RTCStatList sentStats = getPCStatOvertime(webDriver,
            getStatsConfig.getJsonArray("peerConnections").getString(0),
            getStatsConfig.getInt("statsCollectionTime"),
            getStatsConfig.getInt("statsCollectionInterval"),
            getStatsConfig.getJsonArray("selectedStats"));
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        pcStatMap.put(getStatsConfig.getJsonArray("peerConnections").getString(0), sentStats);
        for (int i = 1; i < remotePC.size() + 1; i++) {
          RTCStatList receivedStats = getPCStatOvertime(webDriver,
              remotePC.get(i - 1),
              getStatsConfig.getInt("statsCollectionTime"),
              getStatsConfig.getInt("statsCollectionInterval"),
              getStatsConfig.getJsonArray("selectedStats"));
          logger.info("Adding pc stats to map for :" + remotePC.get(i - 1) );
          pcStatMap.put(remotePC.get(i - 1), receivedStats);
          arrayBuilder.add(transformToJson(receivedStats));
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("local", transformToJson(sentStats));
        builder.add("remote", arrayBuilder);
        reporter.jsonAttachment(report, "Stats (Raw)", builder.build());
        reporter.jsonAttachment(report, "Stats Summary", buildStatSummary(pcStatMap));
      } else {
        RTCStatMap statsOverTime =  getPCStatOvertime(webDriver, getStatsConfig);
        RTCStatList localPcStats = statsOverTime.getLocalPcStats();
        reporter.jsonAttachment(this.report, "Stats (Raw)", transformToJson(localPcStats));
        reporter.jsonAttachment(this.report, "Stats Summary", buildStatSummary(localPcStats));
      }

    } catch (Exception e) {
      logger.error(getStackTrace(e));
      throw new KiteTestException("Failed to getStats", Status.BROKEN, e);
    } finally {
      this.setCsvResult(results);
    }

  }

}

