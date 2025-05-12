package com.edergi.playerlib

import android.content.Context

class PlayerLibFactory private constructor() {

    companion object {

        fun init(context: Context, configure: PlayerLib.Config.Builder.() -> Unit) {
            PlayerLib.Config.Builder()
                .setContext(context)
                .apply(configure)
                .build()
                .let { config -> PlayerLib(config).let { lib -> PlayerLib.initialize(lib) } }
        }

    }

}