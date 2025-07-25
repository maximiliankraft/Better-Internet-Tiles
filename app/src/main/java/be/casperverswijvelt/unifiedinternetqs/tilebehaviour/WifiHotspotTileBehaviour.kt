package be.casperverswijvelt.unifiedinternetqs.tilebehaviour

import android.content.Context
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.TileSyncService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiHotspotTileService
import be.casperverswijvelt.unifiedinternetqs.util.AlertDialogData
import be.casperverswijvelt.unifiedinternetqs.util.executeShellCommandAsync
import be.casperverswijvelt.unifiedinternetqs.util.getWifiHotspotEnabled
import kotlinx.coroutines.Runnable

class WifiHotspotTileBehaviour(
    context: Context,
    showDialog: (AlertDialogData) -> Unit = {},
    unlockAndRun: (Runnable) -> Unit = { it.run() }
) : TileBehaviour(context, showDialog, unlockAndRun) {

    companion object {
        private const val TAG = "WifiHotspotTileBehaviour"
    }

    override val type: TileType
        get() = TileType.WifiHotspot
    override val tileName: String
        get() = resources.getString(R.string.wifi_hotspot)
    override val defaultIcon: Icon
        get() = Icon.createWithResource(
            context,
            R.drawable.ic_baseline_wifi_tethering_24
        )

    @Suppress("UNCHECKED_CAST")
    override val tileServiceClass: Class<TileService>
        get() = WifiHotspotTileService::class.java as Class<TileService>

    override val tileState: TileState
        get() {
            val tile = TileState()
            val hotspotEnabled = getWifiHotspotEnabled(context)

            if ((hotspotEnabled && !TileSyncService.isTurningOffWifiHotspot) || TileSyncService.isTurningOnWifiHotspot) {

                if (hotspotEnabled) TileSyncService.isTurningOnWifiHotspot = false

                tile.label = resources.getString(R.string.wifi_hotspot)
                tile.state = Tile.STATE_ACTIVE
                tile.icon = R.drawable.ic_baseline_wifi_tethering_24
                tile.subtitle = when {
                    TileSyncService.isTurningOnWifiHotspot -> resources.getString(R.string.turning_on)
                    TileSyncService.wifiHotspotClientsCount > 0 -> "${TileSyncService.wifiHotspotClientsCount} device${if (TileSyncService.wifiHotspotClientsCount == 1) "" else "s"}"
                    else -> resources.getString(R.string.on)
                }

            } else {

                if (!hotspotEnabled) TileSyncService.isTurningOffWifiHotspot = false

                tile.label = resources.getString(R.string.wifi_hotspot)
                tile.state = Tile.STATE_INACTIVE
                tile.icon = R.drawable.ic_baseline_wifi_tethering_off_24
                tile.subtitle = resources.getString(R.string.off)
            }
            return tile
        }
    
    override val onLongClickIntentAction: String
        get() = Settings.ACTION_WIFI_AP_SETTINGS

    override fun onClick() {
        log("onClick")

        if (!checkShellAccess()) return

        if (requiresUnlock) {
            unlockAndRun { toggleWifiHotspot() }
        } else {
            toggleWifiHotspot()
        }
    }

    private fun toggleWifiHotspot() {

        val hotspotEnabled = getWifiHotspotEnabled(context)

        if (hotspotEnabled || TileSyncService.isTurningOnWifiHotspot) {
            TileSyncService.isTurningOnWifiHotspot = false
            TileSyncService.isTurningOffWifiHotspot = true
            executeShellCommandAsync("cmd wifi set-wifi-ap-enabled false", context) {
                if (it?.isSuccess != true) {
                    TileSyncService.isTurningOffWifiHotspot = false
                }
                updateTile()
            }
        } else {
            TileSyncService.isTurningOnWifiHotspot = true
            TileSyncService.isTurningOffWifiHotspot = false
            executeShellCommandAsync("cmd wifi set-wifi-ap-enabled true", context) {
                if (it?.isSuccess != true) {
                    TileSyncService.isTurningOnWifiHotspot = false
                }
                updateTile()
            }
        }
        updateTile()
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}
