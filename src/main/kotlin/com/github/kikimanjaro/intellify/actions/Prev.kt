package com.github.kikimanjaro.intellify.actions

import com.github.kikimanjaro.intellify.services.SpotifyService
import com.intellij.openapi.actionSystem.AnActionEvent

class Prev : AbstractSpotifyAction() {

    override fun actionPerformed(e: AnActionEvent) {
        SpotifyService.prevTrack()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isActionActive
    }
}