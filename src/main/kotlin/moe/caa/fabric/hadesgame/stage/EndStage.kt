package moe.caa.fabric.hadesgame.stage

import moe.caa.fabric.hadesgame.util.getPlayers

data object EndStage : AbstractStage() {
    override val stageName = "结束"
    override val nextStage = InitStage


    private var tick = 0
    private var countdown = 0

    override suspend fun startStage() {
        tick = 0
        countdown = 15

        for (player in getPlayers()) {

        }
    }

    override suspend fun tickStage() {
        tick++

        if (tick % 20 == 0) {
            countdown--
        }
    }

    override suspend fun shouldEndStage(): Boolean {
        return countdown <= 0
    }
}