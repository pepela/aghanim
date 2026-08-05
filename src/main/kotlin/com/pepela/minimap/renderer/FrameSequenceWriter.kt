package com.pepela.minimap.renderer

import com.pepela.minimap.parser.GameState
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

internal class FrameSequenceWriter(
    outputDir: Path
) : AutoCloseable {
    val framesDir: Path = outputDir.resolve("frames")
    val snapshotPath: Path = outputDir.resolve("snapshots.jsonl")
    var frameCount: Int = 0
        private set

    private val renderer = MinimapRenderer(loadMinimap())
    private val snapshotWriter: java.io.BufferedWriter

    init {
        Files.createDirectories(framesDir)
        Files.createDirectories(outputDir)
        snapshotWriter = Files.newBufferedWriter(snapshotPath)
    }

    fun write(state: GameState) {
        val frame = renderer.render(state)
        val file = framesDir.resolve("frame_%06d.png".format(frameCount))
        ImageIO.write(frame, "png", file.toFile())
        snapshotWriter.write(state.toJsonLine(frameCount))
        snapshotWriter.newLine()
        frameCount += 1
    }

    override fun close() {
        snapshotWriter.close()
    }

    private fun loadMinimap(): BufferedImage {
        val stream = javaClass.getResourceAsStream("/minimap.png")
            ?: error("Bundled minimap resource not found: /minimap.png")
        stream.use {
            return ImageIO.read(it) ?: error("Bundled minimap resource is not a readable image: /minimap.png")
        }
    }
}
