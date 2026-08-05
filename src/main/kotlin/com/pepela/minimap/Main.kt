package com.pepela.minimap

import com.pepela.minimap.encoder.VideoEncoder
import com.pepela.minimap.parser.ReplayParser
import com.pepela.minimap.renderer.FrameSequenceWriter
import java.nio.file.Path

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: <replay.dem> <output-dir>")
        println("Example: ./gradlew run --args=\"match.dem out\"")
        return
    }

    val replayPath = Path.of(args[0])
    val outputDir = Path.of(args[1])
    val videoPath = outputDir.resolve("output.mp4")
    val fps = args.getOrNull(3)?.toIntOrNull() ?: 1

    FrameSequenceWriter(outputDir).use { writer ->
        ReplayParser { state -> writer.write(state) }.parse(replayPath)
        println("Wrote ${writer.frameCount} frames and ${writer.snapshotPath}")

        val result = VideoEncoder.encode(writer.framesDir, videoPath, fps)
        if (result.success) {
            println("Wrote video $videoPath")
        } else {
            println("Could not create video with ffmpeg: ${result.message}")
            println("PNG frames are still available in ${writer.framesDir}")
        }
    }
}
