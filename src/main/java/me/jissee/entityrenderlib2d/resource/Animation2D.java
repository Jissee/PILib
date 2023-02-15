package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 二维动画类由一组描述实体外观的静态图片组成。<br/><br/>
 * The animation consists of a collection of pictures that will be shown as the appearance of the entity.
 */
public class Animation2D implements Texture2D {
    private final List<ResourceLocation> textures = new ArrayList<>();
    private final List<ResourceLocation> texturesBack = new ArrayList<>();
    private int repeat;
    private int texturesCount;
    private int previousIndex = 0;
    private long baseNanoTime = -1;
    private int fps = 0;
    private long nanoInterval = 0;
    private Texture2DManager.ControlCode statusCode;
    private long pauseNanoTime;
    private int pauseIndex;
    private RenderSetting setting;

    /**
     * 使用一系列文件名创建单面动画。<br/><br/>
     * 如“pic_1.png”,"pic_2.png","pic_3.png"......"pic_100.png",不要使用“001”，“002”这样的命名<br/><br/>
     * Create a collection of single-sided animation by a series of file name.<br/><br/>
     * For example, “pic_1.png”,"pic_2.png","pic_3.png"...“pic_100.png”, but do not use the naming like “001”，“002”<br/><br/>
     * @param modId            模组ID Mod ID
     * @param indexPlaceholder 文件名的占位符，将被替换成数字，必须出现在文件名中。例如“%i” The placeholder of the image series that will be replaced. Must appear in resourceName. For example "%i".
     * @param resourceName     带有占位符的材质文件名，例如“textures/entity/pic_%i.png” The resource name with the placeholder. For example, “textures/entity/pic_%i.png”
     * @param startIndex       占位符开始的下表，例如 1 The start number of the index. For example, 1
     * @param endIndex         占位符结束的下表，例如 100 The end number of the index. For example, 100
     * @param nanoInterval     每两帧之间间隔的时间，用纳秒表示，例如 2_000 （即2毫秒） The interval between two frames in nanoseconds.For example, 2_000 (2 milliseconds)
     * @param repeat           动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param setting          渲染设置 The {@link RenderSetting} of this Animation.
     * @return
     */

    public static Animation2D createSingleSide(String modId, String indexPlaceholder, String resourceName, int startIndex, int endIndex, long nanoInterval, int repeat, RenderSetting setting){
        Animation2D anim = new Animation2D(repeat,nanoInterval,setting);
        String tmp;
        for(int i = startIndex; i <= endIndex; i++){
            tmp = resourceName.replaceAll(indexPlaceholder, "" + i);
            anim.addTexture(new ResourceLocation(modId,tmp));
        }
        return anim;
    }

    /**
     * 使用一系列文件名创建单面动画,如:<br/>
     * “pic_front_1.png”,"pic_front_2.png","pic_front_3.png"......"pic_front_100.png",<br/>
     * “pic_back_1.png”,"pic_back_2.png","pic_back_3.png"......"pic_back_100.png"，不要使用“001”，“002”这样的命名<br/><br/>
     * Create a collection of single-sided animation by a series of file name. Like<br/>
     * “pic_front_1.png”,"pic_front_2.png","pic_front_3.png"......"pic_front_100.png",<br/>
     * “pic_back_1.png”,"pic_back_2.png","pic_back_3.png"......"pic_back_100.png", but do not use the naming like “001”，“002”<br/><br/>
     * @param modId             模组ID Mod ID
     * @param indexPlaceholder  文件名的占位符，将被替换成数字，必须出现在文件名中。例如“%i” The placeholder of the image series. Must appear in resourceName. For example "%i".
     * @param frontResourceName 带有占位符的正面材质文件名，例如“textures/entity/pic_front_%i.png” The resource name with the placeholder of the front texture. For example, “textures/entity/pic_front_%i.png”
     * @param backResourceName  带有占位符的反面材质文件名，例如“textures/entity/pic_back_%i.png” The resource name with the placeholder of the back texture. For example, “textures/entity/pic_back_%i.png”
     * @param startIndex        占位符开始的下表，例如 1 The start number of the index. For example, 1
     * @param endIndex          占位符结束的下表，例如 100 The end number of the index. For example, 100
     * @param nanoInterval      每两帧之间间隔的时间，用纳秒表示，例如 2_000 （即2毫秒） The interval between two frames in nanoseconds.For example, 2_000 (2 milliseconds)
     * @param repeat            动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param setting           渲染设置 The {@link RenderSetting} of this Animation.
     * @return
     */

    public static Animation2D createDoubleSide(String modId, String indexPlaceholder, String frontResourceName, String backResourceName, int startIndex, int endIndex, long nanoInterval, int repeat, RenderSetting setting){
        Animation2D anim = new Animation2D(repeat,nanoInterval,setting);
        String tmp1, tmp2;
        for(int i = startIndex; i <= endIndex; i++){
            tmp1 = frontResourceName.replaceAll(indexPlaceholder, String.valueOf(i));
            tmp2 = backResourceName.replaceAll(indexPlaceholder, String.valueOf(i));
            anim.addTexture(new ResourceLocation(modId,tmp1),new ResourceLocation(modId,tmp2));
        }
        return anim;
    }

    /**
     * 连接两段动画。
     * Combine two animations.
     * @param anim1   第一段动画 The first animation.
     * @param anim2   第二段动画 The second animation.
     * @param repeat  动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param setting 整个动画的渲染设置 The {@link RenderSetting} of the whole Animation.
     */
    public static Animation2D combine(Animation2D anim1, Animation2D anim2, int repeat, RenderSetting setting){
        long interval = anim1.nanoInterval;
        if(anim1.nanoInterval != anim2.nanoInterval){
            int fps1 = anim1.fps;
            int fps2 = anim2.fps;
            int resfps = (int) lcm(fps1,fps2);
            Animation2D anim = new Animation2D(repeat, resfps, setting);

            int cnt1 = resfps / anim1.fps;
            int cnt2 = resfps / anim2.fps;
            int r = 0;

            r = anim1.repeat > 1 ? anim1.repeat : 1;
            for(; r > 0; r--){
                for(int i = 0; i < anim1.texturesCount; i++){
                    for(int j = 0; j < cnt1; j++){
                        anim.addTexture(anim1.textures.get(i), anim1.texturesBack.get(i));
                    }
                }
            }

            r = anim2.repeat > 1 ? anim2.repeat : 1;
            for(; r > 0; r--){
                for(int i = 0; i < anim2.texturesCount; i++){
                    for(int j = 0; j < cnt2; j++){
                        anim.addTexture(anim2.textures.get(i), anim2.texturesBack.get(i));
                    }
                }
            }
            return anim;
        }else {
            Animation2D anim = new Animation2D(repeat, interval, setting);
            int r = 0;
            r = anim1.repeat > 1 ? anim1.repeat : 1;
            for(; r > 0; r--){
                for (int i = 0; i < anim1.texturesCount; i++){
                    anim.addTexture(anim1.textures.get(i), anim1.texturesBack.get(i));
                }
            }
            r = anim2.repeat > 1 ? anim2.repeat : 1;
            for(; r > 0; r--){
                for (int i = 0; i < anim2.texturesCount; i++){
                    anim.addTexture(anim2.textures.get(i), anim2.texturesBack.get(i));
                }
            }
            return anim;
        }
    }

    /**
     * 创建空的动画，按顺序添加材质。
     * Create new empty Animation2D and add textures sequentially.
     * @param repeat        动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param nanoInterval  每两帧之间间隔的时间，用纳秒表示，例如 2_000 （即2毫秒） The interval between two frames in nanoseconds.For example, 2_000 (2 milliseconds)
     * @param setting       渲染设置 The {@link RenderSetting} of this Animation.
     */
    public Animation2D(int repeat, long nanoInterval, RenderSetting setting){
        this.repeat = repeat;
        this.nanoInterval = nanoInterval;
        this.fps = (int) (1.0 / (nanoInterval / 1e9));
        this.setting = setting;
    }
    /**
     * 创建空的动画，按顺序添加材质。
     * Create new empty Animation2D and add textures sequentially.
     * @param repeat  动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param fps     每秒帧数 Frames per second
     * @param setting 渲染设置 The {@link RenderSetting} of this Animation.
     */
    public Animation2D(int repeat, int fps, RenderSetting setting){
        this.repeat = repeat;
        this.fps = fps;
        this.nanoInterval = (long) (1.0 / (double)fps * 1e9);
        this.setting = setting;
    }

    /**
     * 按顺序依次添加材质 (单面)
     * Add Textures sequentially. (Single Side)
     * @param texture 正面材质 Texture of front side
     */
    public void addTexture(ResourceLocation texture){
        addTexture(texture, null);
    }

    /**
     * 按顺序依次添加材质 (双面)
     * Add Textures sequentially. (Double Sides)
     * @param texture 正面材质 Texture of front side
     * @param textureBack 反面材质 Texture of back side
     */
    public void addTexture(ResourceLocation texture, ResourceLocation textureBack){
        texturesCount++;
        textures.add(texture);
        texturesBack.add(textureBack);
    }


    @Override
    public ResourceLocation getCurrentTextureFront(){//res0 2 tk, res1 2 tk
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return textures.get(previousIndex);
        }
        previousIndex = getIndex();
        return textures.get(previousIndex);
    }

    @Override
    public ResourceLocation getCurrentTextureBack(){//res0 2 tk, res1 2 tk
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return texturesBack.get(previousIndex);
        }
        previousIndex = getIndex();
        return texturesBack.get(previousIndex);
    }

    public void setRenderSetting(RenderSetting setting) {
        this.setting = setting;
    }

    @Override
    public RenderSetting getRenderSetting() {
        return setting;
    }

    public int getIndex(){
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return pauseIndex;
        }
        long thisNanoTime = System.nanoTime();
        if(baseNanoTime == -1){
            baseNanoTime = thisNanoTime;
        }
        int repeatCopy = repeat;
        int index = (int) ((thisNanoTime - baseNanoTime) / nanoInterval);
        if(index >= texturesCount){
            if(repeatCopy == -1){
                baseNanoTime = thisNanoTime;
                index = 0;
            }else if(repeatCopy > 0){
                repeatCopy--;
                baseNanoTime = thisNanoTime;
                index = 0;
            }else{
                index = texturesCount - 1;
            }
        }
        return index;
    }

    @Override
    public void startOrReset() {
        this.statusCode = Texture2DManager.ControlCode.START_OR_RESET;
        baseNanoTime = -1;
    }

    @Override
    public void pause() {
        this.pauseNanoTime = System.nanoTime();
        this.pauseIndex = getIndex();
        this.statusCode = Texture2DManager.ControlCode.PAUSE;
    }

    @Override
    public void resume() {
        long diff = System.nanoTime() - pauseNanoTime;
        baseNanoTime += diff;
        this.statusCode = Texture2DManager.ControlCode.RESUME;
    }

    public void setRepeat(int repeat){
        this.repeat = repeat;
    }



    private static long gcd(long a, long b){
        long c;
        do{
            c = a % b;
            a = b;
            b = c;
        }while(b != 0);
        return a;
    }

    private static long lcm(long a, long b){
        return a * b / gcd(a,b);
    }


}
