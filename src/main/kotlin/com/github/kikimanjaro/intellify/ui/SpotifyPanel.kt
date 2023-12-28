package com.github.kikimanjaro.intellify.ui

import com.github.kikimanjaro.intellify.models.Playlist
import com.github.kikimanjaro.intellify.services.SpotifyService
import com.github.kikimanjaro.intellify.services.SpotifyStatusUpdater
import com.intellij.openapi.ui.popup.ActiveIcon
import java.awt.*
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.plaf.basic.BasicSliderUI


class SpotifyPanel(val spotifyStatusUpdater: SpotifyStatusUpdater) : JPanel(BorderLayout()) {
    val customWidth = 200
    val customHeight = 200

    private val playPauseButton: JButton
    private val prevButton: JButton
    private val nextButton: JButton
    private val bullets: Array<JButton> = Array(3) { JButton() }

    private val artistNameLabel: JLabel
    private val songNameLabel: JLabel
    private val imageIcon: ImageIcon
    private val imageLabel: JLabel
    private val titlePanel: JPanel

    private val slider: JSlider

    init {
        val image: BufferedImage = ImageIO.read(URL(SpotifyService.imageUrl))
        val scaledImage = image.getScaledInstance(customWidth, customHeight, Image.SCALE_SMOOTH)

        imageIcon = ImageIcon(scaledImage)
        imageLabel = JLabel(imageIcon)

        artistNameLabel = JLabel(SpotifyService.artist, JLabel.CENTER)
        artistNameLabel.setFont(artistNameLabel.getFont().deriveFont(Font.BOLD, 14f));
        songNameLabel = JLabel(SpotifyService.song, JLabel.CENTER)

        titlePanel = JPanel(BorderLayout())
        titlePanel.add(artistNameLabel, BorderLayout.NORTH)
        titlePanel.add(songNameLabel, BorderLayout.SOUTH)

        val buttonPanel = JPanel()
        buttonPanel.layout = BorderLayout()
        buttonPanel.isOpaque = false

        playPauseButton = JButton()
        if (SpotifyService.isPlaying) {
            playPauseButton.icon = spotifyStatusUpdater.pauseIcon
        } else {
            playPauseButton.icon = spotifyStatusUpdater.playIcon
        }
        playPauseButton.addActionListener {
            if (SpotifyService.isPlaying) {
                SpotifyService.pauseTrack()
            } else {
                SpotifyService.startTrack()
            }
            update()
        }
        prevButton = JButton(spotifyStatusUpdater.prevIcon)
        prevButton.addActionListener {
            SpotifyService.prevTrack()
            update()
        }
        nextButton = JButton(spotifyStatusUpdater.nextIcon)
        nextButton.addActionListener {
            SpotifyService.nextTrack()
            update()
        }

        slider = object : JSlider(0, SpotifyService.durationMs) {
            override fun updateUI() {
                setUI(CustomSliderUI(this));
            }
        }
        slider.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        slider.value = SpotifyService.progressInMs
        slider.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseReleased(e: java.awt.event.MouseEvent) {
                val newVal = slider.value
                SpotifyService.setProgress(newVal)
                slider.value = newVal
                update()
                slider.value = newVal
            }
        })

        // Create a CardLayout that will contain the panel
        val cardLayout = CardLayout()
        val cardPanel = JPanel(cardLayout)

        // Panels to be displayed
        val playbackPanel: JPanel = JPanel(BorderLayout())
        val recentPlaylistPanel: JPanel = JPanel()
        recentPlaylistPanel.layout = BoxLayout(recentPlaylistPanel, BoxLayout.Y_AXIS)

        val homePanel: JPanel = JPanel()
        homePanel.layout = BoxLayout(homePanel, BoxLayout.Y_AXIS)

        val playlistScrollPane = JScrollPane()
        playlistScrollPane.preferredSize = Dimension(playlistScrollPane.preferredSize.width, 300)
        playlistScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
        playlistScrollPane.setViewportView(homePanel)

        val playlistPanels: List<JPanel> = SpotifyService.getCurrentUsersPlaylists()
                .map { createPlaylistItemPanel(it) }
                .toList()

        playlistPanels.forEach { homePanel.add(it) }
        homePanel.revalidate()
        homePanel.repaint()


        recentPlaylistPanel.add(JLabel("recentPlaylistPanel"))

        cardPanel.add(playlistScrollPane, "Home")
        cardPanel.add(playbackPanel, "Playback")
        cardPanel.add(recentPlaylistPanel, "Recently Played")

        // Add the buttons to the button panel
        buttonPanel.add(prevButton, BorderLayout.WEST)
        buttonPanel.add(playPauseButton, BorderLayout.CENTER)
        buttonPanel.add(nextButton, BorderLayout.EAST)

        // Panel that contains the bullets
        val bulletPanel = JPanel(FlowLayout(FlowLayout.CENTER, 15, 5))
        val bullet1: JButton = createBulletButton(cardLayout, cardPanel, "Home")
        val bullet2: JButton = createBulletButton(cardLayout, cardPanel, "Playback")
        val bullet3: JButton = createBulletButton(cardLayout, cardPanel, "Recently Played")
        bullets[0] = bullet1
        bullets[1] = bullet2
        bullets[2] = bullet3
        bullets.forEach { bulletPanel.add(it) }

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(slider, BorderLayout.NORTH)
        bottomPanel.add(buttonPanel, BorderLayout.CENTER)

        playbackPanel.add(titlePanel, BorderLayout.NORTH)
        playbackPanel.add(imageLabel, BorderLayout.CENTER)
        playbackPanel.add(bottomPanel, BorderLayout.SOUTH)

        add(cardPanel, BorderLayout.CENTER)
        add(bulletPanel, BorderLayout.SOUTH)

        if (SpotifyService.isPlaying) {
            bullet2.doClick()
        } else {
            bullet1.doClick()
        }

    }

    private fun createPlaylistItemPanel(it: Playlist): JPanel {
        val panel = JPanel(FlowLayout())
        val cover: BufferedImage = ImageIO.read(URL(it.coverImage))
        val scaledCover = cover.getScaledInstance(32, 32, Image.SCALE_SMOOTH)

        val playlistNameLabel = JLabel(it.name)
        panel.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                SpotifyService.startPlaylist(it.id)
            }
        })

        panel.add(JLabel(ImageIcon(scaledCover)))
        panel.add(playlistNameLabel)
        return panel
    }

    fun update() {
        artistNameLabel.text = SpotifyService.artist
        songNameLabel.text = SpotifyService.song
        titlePanel.repaint()

        val image: BufferedImage = ImageIO.read(URL(SpotifyService.imageUrl))
        val scaledImage = image.getScaledInstance(customWidth, customHeight, Image.SCALE_SMOOTH)

        imageIcon.image = scaledImage
        imageLabel.repaint()

        if (SpotifyService.isPlaying) {
            playPauseButton.icon = spotifyStatusUpdater.pauseIcon
        } else {
            playPauseButton.icon = spotifyStatusUpdater.playIcon
        }

        slider.value = SpotifyService.progressInMs
    }

    private fun createBulletButton(cardLayout: CardLayout, cardPanel: JPanel, cardName: String): JButton {
        val bulletPointIcon: ActiveIcon = spotifyStatusUpdater.bulletPointIcon
        val bullet = JButton(bulletPointIcon)
        bullet.addActionListener {
            bullets.forEach { (it.icon as ActiveIcon).setActive(it == bullet) }
            cardLayout.show(cardPanel, cardName)
        }
        return bullet
    }
}

private class CustomSliderUI(b: JSlider?) : BasicSliderUI(b) {
    private val trackShape = RoundRectangle2D.Float()
    override fun calculateTrackRect() {
        super.calculateTrackRect()
        if (isHorizontal) {
            trackRect.y = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2
            trackRect.height = TRACK_HEIGHT
        } else {
            trackRect.x = trackRect.x + (trackRect.width - TRACK_WIDTH) / 2
            trackRect.width = TRACK_WIDTH
        }
        trackShape.setRoundRect(
                trackRect.x.toFloat(),
                trackRect.y.toFloat(),
                trackRect.width.toFloat(),
                trackRect.height.toFloat(),
                TRACK_ARC.toFloat(),
                TRACK_ARC.toFloat()
        )
    }

    override fun calculateThumbLocation() {
        super.calculateThumbLocation()
        if (isHorizontal) {
            thumbRect.y = trackRect.y + (trackRect.height - thumbRect.height) / 2
        } else {
            thumbRect.x = trackRect.x + (trackRect.width - thumbRect.width) / 2
        }
    }

    override fun getThumbSize(): Dimension {
        return THUMB_SIZE
    }

    private val isHorizontal: Boolean
        private get() = slider.orientation == JSlider.HORIZONTAL

    override fun paint(g: Graphics, c: JComponent) {
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        super.paint(g, c)
    }

    override fun paintTrack(g: Graphics) {
        val g2 = g as Graphics2D
        val clip: Shape = g2.clip
        val horizontal = isHorizontal
        var inverted = slider.inverted

        // Paint shadow.
        g2.color = Color(170, 170, 170)
        g2.fill(trackShape)

        // Paint track background.
        g2.color = Color(200, 200, 200)
        g2.clip = trackShape
        trackShape.y += 1f
        g2.fill(trackShape)
        trackShape.y = trackRect.y.toFloat()
        g2.clip = clip

        // Paint selected track.
        if (horizontal) {
            val ltr = slider.componentOrientation.isLeftToRight
            if (ltr) inverted = !inverted
            val thumbPos = thumbRect.x + thumbRect.width / 2
            if (inverted) {
                g2.clipRect(0, 0, thumbPos, slider.height)
            } else {
                g2.clipRect(thumbPos, 0, slider.width - thumbPos, slider.height)
            }
        } else {
            val thumbPos = thumbRect.y + thumbRect.height / 2
            if (inverted) {
                g2.clipRect(0, 0, slider.height, thumbPos)
            } else {
                g2.clipRect(0, thumbPos, slider.width, slider.height - thumbPos)
            }
        }
        g2.color = Color(29, 184, 84)
        g2.fill(trackShape)
        g2.clip = clip
    }

    override fun paintThumb(g: Graphics) {
        g.color = Color.WHITE
        g.fillOval(thumbRect.x + thumbRect.width / 4, thumbRect.y + thumbRect.height / 4, thumbRect.width / 2, thumbRect.height / 2)
    }

    override fun paintFocus(g: Graphics) {}

    companion object {
        private const val TRACK_HEIGHT = 8
        private const val TRACK_WIDTH = 8
        private const val TRACK_ARC = 10
        private val THUMB_SIZE: Dimension = Dimension(20, 20)
    }
}