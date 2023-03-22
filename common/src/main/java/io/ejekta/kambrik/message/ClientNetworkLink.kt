package io.ejekta.kambrik.message

import io.ejekta.kambrik.bridge.Kambridge
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class ClientNetworkLink<M : ClientMsg>(

    override val id: Identifier,
    override val ser: KSerializer<M>,
    override val json: Json = INetworkLink.defaultJson

    ) : INetworkLink<M> {

    override fun register(): Boolean {
        if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            return true // fake it w/o registering on Server
        }
        return Kambridge.registerClientMessage(this, id)
    }

    fun send(msg: M, players: Collection<ServerPlayerEntity>) {
        for (player in players) {
            Kambridge.sendMsgToClient(this, msg, player, id)
        }
    }

}