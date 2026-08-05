package com.pepela.minimap.encoder

import java.nio.file.Files
import java.nio.file.Path

internal object VideoEncoder {
    fun encode(framesDir: Path, videoPath: Path, fps: Int): VideoEncodeResult {
        return try {
            require(fps > 0) { "fps must be greater than zero" }
            require(Files.isDirectory(framesDir)) { "Frames directory does not exist: $framesDir" }
            videoPath.toAbsolutePath().parent?.let { Files.createDirectories(it) }

            val inputPattern = framesDir.resolve("frame_%06d.png").toString()
            val process = ProcessBuilder(
                "ffmpeg",
                "-y",
                "-framerate", fps.toString(),
                "-i", inputPattern,
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                videoPath.toString()
            )
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText().takeLast(2000)
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                VideoEncodeResult(true, "ok")
            } else {
                VideoEncodeResult(false, output.ifBlank { "ffmpeg exited with code $exitCode" })
            }
        } catch (e: Exception) {
            VideoEncodeResult(false, e.message ?: e::class.java.simpleName)
        }
    }
}
