package com.edergi.playerlib

import android.content.Context

class PlayerLibFactory private constructor() {

    companion object {
        fun init(context: Context) {
            return PlayerLib.Config.Builder().apply {
                setContext(context)
            }.build().let { config -> PlayerLib(config).let { lib -> PlayerLib.initialize(lib) } }
        }
        fun init(context: Context, configure: PlayerLib.Config.Builder.() -> Unit) {
            PlayerLib.Config.Builder()
                .setContext(context)
                .apply(configure)
                .build()
                .let { config -> PlayerLib(config).let { lib -> PlayerLib.initialize(lib) } }
        }
        fun create(context: Context): PlayerLib {
            return PlayerLib.Config.Builder().apply {
                setContext(context)
            }.build().let { config -> PlayerLib(config) }
        }
        fun create(context: Context, configure: PlayerLib.Config.Builder.() -> Unit): PlayerLib {
            return PlayerLib.Config.Builder()
                .setContext(context)
                .apply(configure)
                .build()
                .let { config -> PlayerLib(config) }
        }
    }

}