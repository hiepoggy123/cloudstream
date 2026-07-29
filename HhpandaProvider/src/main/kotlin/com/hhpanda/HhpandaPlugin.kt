package com.hhpanda

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class HhpandaPlugin: Plugin() {
    override fun load(context: Context) {
        // Register the provider here
        registerMainAPI(HhpandaProvider())
    }
}
