package io.ejekta.kambrikx.data.server

import io.ejekta.kambrikx.data.LoadableDataRegistrar
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier
import java.io.File

internal object ServerDataRegistrar : LoadableDataRegistrar() {

    var loaded = false

    override fun getFile(id: Identifier?): File {
        return FabricLoader.getInstance().configDir.resolve("kambrik_data.json").toFile()
    }

    override fun loadResults() {
        super.loadResults()
        loaded = true
    }

    override fun saveResults() {
        super.saveResults()
        loaded = false
    }

    override fun <R : Any> loadResult(key: Identifier): R {
        if (!loaded) {
            throw Exception("Trying to access server data before the server is loaded!")
        }
        return super.loadResult(key)
    }

}