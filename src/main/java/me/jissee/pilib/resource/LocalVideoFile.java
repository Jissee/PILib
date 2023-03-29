package me.jissee.pilib.resource;


import net.minecraftforge.fml.loading.FMLLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import static me.jissee.pilib.PILib.LOGGER;
import static me.jissee.pilib.resource.LocalResourceUtil.getFfmpegPath;
import static me.jissee.pilib.resource.LocalResourceUtil.getSEP;

/**
 * 用于需要解压到本地的视频文件
 */

public final class LocalVideoFile {
    private final LocalResourceUtil util;
    private final String path;
    private final String name;
    private final int frames;
    private final double fps;

    /**
     * @param path   文件完整路径 The full path of the video file
     * @param name   解压后的帧前缀名称，用于被渲染器读取 The name prefix of the decoded frames, which will be read by the render.
     * @param frames 视频总帧数，若不需要精确，则可以通过总时长*帧率获得，也可以通过ffprobe精确查看<br/>
     *               Total frame count of the video, which can be roughly calculated by (total time) * (fps), or view by ffprobe precisely<br/>ffprobe command:<br/>
     *               ffprobe -v error -count_frames -select_streams v:0 -show_entries stream=nb_read_frames -of default=nokey=1:noprint_wrappers=1 (input.mp4)
     * @param fps    帧率 Frames per second.
     */
    public LocalVideoFile(LocalResourceUtil util, String path, String name, int frames, double fps) {
        this.util = util;
        this.path = path;
        this.name = name;
        this.frames = frames;
        this.fps = fps;
    }

    public void decode() throws FileNotFoundException {
        if (FMLLoader.getDist() == null || FMLLoader.getDist().isClient()) {
            if (!isDecoded()) {
                File videoFile = new File(path);

                if (!videoFile.exists()) {
                    throw new FileNotFoundException("Cannot find video file " + path);
                }
                String outPath = util.getModDecodeBasePath();
                File outDirectory = new File(outPath);
                outDirectory.mkdirs();

                Thread thread = new Thread(() -> {
                    try {
                        String[] cmd = new String[]{
                                getFfmpegPath(),
                                "-i",
                                videoFile.getPath(),
                                "-r",
                                String.valueOf(fps),
                                "-f",
                                "image2",
                                outPath + name + "_%d.jpg"
                        };
                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        LOGGER.info("Video resource \"" + this.name + "\" is going to be decoded.");
                        LOGGER.info("Make Sure you have enough disk space.");
                        pb.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.start();
            }
        }
    }

    public File getImageFile(int index) {
        return new File(util.getModDecodeBasePath() + name + "_" + index + ".jpg");
    }

    public boolean isDecoded() {
        if (path.endsWith(getSEP())) {
            return new File(path + name + "_" + frames + ".jpg").exists();
        } else {
            return new File(path + getSEP() + name + "_" + frames + ".jpg").exists();
        }
    }

    public LocalResourceUtil util() {
        return util;
    }

    public String path() {
        return path;
    }

    public String name() {
        return name;
    }

    public int frames() {
        return frames;
    }

    public double fps() {
        return fps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LocalVideoFile) obj;
        return Objects.equals(this.util, that.util) &&
                Objects.equals(this.path, that.path) &&
                Objects.equals(this.name, that.name) &&
                this.frames == that.frames &&
                Double.doubleToLongBits(this.fps) == Double.doubleToLongBits(that.fps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(util, path, name, frames, fps);
    }

    @Override
    public String toString() {
        return "LocalVideoFile[" +
                "util=" + util + ", " +
                "path=" + path + ", " +
                "name=" + name + ", " +
                "frames=" + frames + ", " +
                "fps=" + fps + ']';
    }


}
