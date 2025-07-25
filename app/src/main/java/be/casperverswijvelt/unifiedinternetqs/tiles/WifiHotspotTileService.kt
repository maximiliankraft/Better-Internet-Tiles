package be.casperverswijvelt.unifiedinternetqs.tiles

import be.casperverswijvelt.unifiedinternetqs.tilebehaviour.WifiHotspotTileBehaviour
import be.casperverswijvelt.unifiedinternetqs.util.toDialog

class WifiHotspotTileService : ReportingTileService() {

    override fun getTag(): String {
        return "WifiHotspotTileService"
    }

    override fun onCreate() {
        log("WiFi Hotspot tile service created")

        tileBehaviour = WifiHotspotTileBehaviour(
            context = this,
            showDialog = { showDialog(it.toDialog(applicationContext)) },
            unlockAndRun = { unlockAndRun(it) }
        )
        super.onCreate()
    }
}
