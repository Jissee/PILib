package me.jissee.pilib.resource;

import me.jissee.pilib.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

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
    private long nanoInterval = 0;
    private RenderSetting renderSetting;
    private TextureSetting textureSetting;
    private TextureControlCode statusCode;
    private int previousIndex = 0;
    private long baseNanoTime = -1;
    private int pauseProgress;
    private long totalTime = 0;
    private long singleAnimTime = 0;
    private int pauseIndex = -1;


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
     * @param textureSetting   材质设置 The {@link TextureSetting} of this Animation.
     * @param renderSetting    渲染设置 The {@link RenderSetting} of this Animation.
     * @return
     */

    public static Animation2D createSingleSide(String modId, String indexPlaceholder, String resourceName, int startIndex, int endIndex, long nanoInterval, int repeat, TextureControlCode defaultStatus, TextureSetting textureSetting, RenderSetting renderSetting){
        Animation2D anim = new Animation2D(repeat, nanoInterval, defaultStatus, textureSetting, renderSetting);
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
     * @param textureSetting    材质设置 The {@link TextureSetting} of this Animation.
     * @param renderSetting     渲染设置 The {@link RenderSetting} of this Animation.
     * @return
     */

    public static Animation2D createDoubleSide(String modId, String indexPlaceholder, String frontResourceName, String backResourceName, int startIndex, int endIndex, long nanoInterval, int repeat, TextureControlCode defaultStatus, TextureSetting textureSetting, RenderSetting renderSetting){
        Animation2D anim = new Animation2D(repeat,nanoInterval,defaultStatus,textureSetting,renderSetting);
        String tmp1, tmp2;
        for(int i = startIndex; i <= endIndex; i++){
            tmp1 = frontResourceName.replaceAll(indexPlaceholder, String.valueOf(i));
            tmp2 = backResourceName.replaceAll(indexPlaceholder, String.valueOf(i));
            anim.addTexture(new ResourceLocation(modId,tmp1),new ResourceLocation(modId,tmp2));
        }
        return anim;
    }

    /**
     * 保留原有设置，连接两段帧率、渲染设置相同动画材质。若其属性不同，请使用{@link Combined}<br/>
     * Combine the textures of two animations while keeping all settings.
     * Use {@link Combined} if they have different properties.
     *
     * @param anim1   第一段动画 The first animation.
     * @param anim2   第二段动画 The second animation.
     * @param repeat  动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param renderSetting 整个动画的渲染设置 The {@link RenderSetting} of the whole Animation.
     */
    public static Animation2D simpleCombine(Animation2D anim1, Animation2D anim2, int repeat, TextureControlCode defaultStatus, RenderSetting renderSetting){
        long interval = anim1.nanoInterval;
        long nano1 = anim1.nanoInterval;
        long nano2 = anim2.nanoInterval;
        TextureSetting tx1 = anim1.textureSetting;
        TextureSetting tx2 = anim2.textureSetting;
        if(nano1 != nano2 || !tx1.equals(tx2)){
            throw new IllegalArgumentException("For Animations with different properties, use Combined instead.");
        }
        Animation2D anim = new Animation2D(repeat, interval,defaultStatus, tx1, renderSetting);
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



    /**
     * 创建空的动画，按顺序添加材质。
     * Create new empty Animation2D and add textures sequentially.
     * @param repeat         动画重复的次数，-1表示循环播放 Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param nanoInterval   每两帧之间间隔的时间，用纳秒表示，例如 2_000 （即2毫秒） The interval between two frames in nanoseconds.For example, 2_000 (2 milliseconds)
     * @param textureSetting 材质设置 The {@link TextureSetting} of this Animation.
     * @param renderSetting  渲染设置 The {@link RenderSetting} of this Animation.
     */

    public Animation2D(int repeat, long nanoInterval, TextureControlCode defaultStatus, TextureSetting textureSetting, RenderSetting renderSetting){
        this.repeat = repeat;
        this.nanoInterval = nanoInterval;
        this.statusCode = defaultStatus;
        this.textureSetting = textureSetting;
        this.renderSetting = renderSetting;
    }

    /**
     * 按顺序依次添加材质 (单面)
     * Add Textures sequentially. (Single Side)
     * @param texture 正面材质 Texture of front side
     */
    public Animation2D addTexture(ResourceLocation texture){
        return addTexture(texture, null);
    }

    /**
     * 按顺序依次添加材质 (双面)
     * Add Textures sequentially. (Double Sides)
     * @param texture 正面材质 Texture of front side
     * @param textureBack 反面材质 Texture of back side
     */
    public Animation2D addTexture(ResourceLocation texture, ResourceLocation textureBack){
        texturesCount++;
        textures.add(texture);
        texturesBack.add(textureBack);
        this.totalTime += nanoInterval * repeat;
        this.singleAnimTime += nanoInterval;
        return this;
    }


    @Override
    public ResourceLocation getCurrentTextureFront(){
        if(statusCode == TextureControlCode.PAUSE){
            return textures.get(previousIndex);
        }
        previousIndex = getIndex();
        return textures.get(previousIndex);
    }

    @Override
    public ResourceLocation getCurrentTextureBack(){
        if(statusCode == TextureControlCode.PAUSE){
            return texturesBack.get(previousIndex);
        }
        previousIndex = getIndex();
        return texturesBack.get(previousIndex);
    }

    public Animation2D setRenderSetting(RenderSetting setting) {
        this.renderSetting = setting;
        return this;
    }
    @Override
    public RenderSetting getRenderSetting() {
        return renderSetting;
    }

    @Override
    public TextureControlCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(TextureControlCode code) {
        this.statusCode = code;
    }

    @Override
    public int getProgress() {
        if(statusCode == TextureControlCode.PAUSE){
            return pauseProgress;
        }else{
            long diff = System.nanoTime() - baseNanoTime;
            return (int)((double)diff / (double)totalTime * MAX_PROGRESS);
        }
    }

    @Override
    public void setProgress(int progress) {
        if(progress > MAX_PROGRESS) progress = MAX_PROGRESS;
        baseNanoTime = System.nanoTime();
        baseNanoTime -= totalTime * ((double)progress / (double)MAX_PROGRESS);
        pauseProgress = progress;
    }

    public Animation2D setTextureSetting(TextureSetting setting) {
        this.textureSetting = setting;
        return this;
    }
    @Override
    public TextureSetting getTextureSetting() {
        return textureSetting;
    }
    public synchronized int getIndex(){
        if(statusCode == TextureControlCode.PAUSE && pauseIndex != -1){
            return pauseIndex;
        }
        long timeNow = System.nanoTime();
        if(baseNanoTime == -1){
            baseNanoTime = timeNow;
            return 0;
        }

        long diff = timeNow - baseNanoTime;

        long pass = diff / singleAnimTime;

        if(repeat != -1 && pass >= repeat){
            return texturesCount - 1;
        }

        diff = diff - pass * singleAnimTime;

        long index = diff / nanoInterval;
        index = index % texturesCount;

        return pauseIndex = (int) index;
    }

    @Override
    public void pause() {
        this.pauseProgress = getProgress();
        this.statusCode = TextureControlCode.PAUSE;
    }

    @Override
    public void resume() {
        if(statusCode == TextureControlCode.PAUSE){
            setProgress(pauseProgress);
            this.statusCode = TextureControlCode.PLAYING;
        }
    }

    @Override
    public void tick() {
        getIndex();
    }

    public Animation2D setRepeat(int repeat){
        this.repeat = repeat;
        return this;
    }

    /**
     * 联合动画，即把多段动画连接所得
     * Combined animation, which connects multiple animations.
     */
    public static class Combined implements Texture2D{
        private final List<Animation2D> animList = new ArrayList<>();
        private final List<Long> nanoIntervals = new ArrayList<>();
        private final List<Integer> textureCounts = new ArrayList<>();
        private final List<Integer> repeats = new ArrayList<>();
        private long totalTime;
        private TextureControlCode statusCode;
        private long baseNanoTime = -1;
        private int pauseProgress;
        private Tuple<Integer,Integer> lastTuple;
        private int repeat;

        /**
         * 调用构造函数后添加动画以及设定
         * Call constructor and add animations and set the properties
         */
        public Combined(TextureControlCode defaultStatus) {
            statusCode = defaultStatus;
            totalTime = 0;
        }

        /**
         * 按顺序添加动画
         * Add animation by sequence
         */
        public Combined add(Animation2D anim){
            if(anim.repeat == 0){
                return this;
            }else if(anim.repeat < 0){
                throw new IllegalArgumentException("The animation repeats for infinite times cannot be combined.");
            }
            animList.add(anim);
            nanoIntervals.add(anim.nanoInterval);
            textureCounts.add(anim.texturesCount);
            repeats.add(anim.repeat);
            totalTime += anim.nanoInterval * anim.texturesCount * anim.repeat;
            return this;
        }
        /**
         * 按顺序添加单帧材质，并指定其持续时间（纳秒）
         * Add single frame texture by sequence with the time it lasts
         */
        public Combined add(SingleTexture2D tex, long nanoTime){
            Animation2D anim = new Animation2D(1,nanoTime,TextureControlCode.PLAYING, tex.getTextureSetting(),tex.getRenderSetting())
                    .addTexture(tex.getCurrentTextureFront(),tex.getCurrentTextureBack());
            return add(anim);
        }

        /**
         * 整个动画重复的次数，-1表示循环播放
         * Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
         */
        public Combined setRepeat(int repeat){
            this.repeat = repeat;
            return this;
        }

        private synchronized Tuple<Integer, Integer> getCurrentTuple(){
            if(statusCode == TextureControlCode.PAUSE && lastTuple != null){
                return lastTuple;
            }
            if(baseNanoTime == -1){
                baseNanoTime = System.nanoTime();
                //repeatCopy = repeat;
                return lastTuple = new Tuple<>(0,0);
            }
            long timeNow = System.nanoTime();
            long diff = timeNow - baseNanoTime;

            long pass = diff / totalTime;

            if(repeat != -1 && pass >= repeat){
                int i = animList.size() - 1;
                return new Tuple<>(i, textureCounts.get(i) - 1);
            }

            diff = diff - pass * totalTime;

            int animIndex;
            int thisRepeat;
            long thisNanoInterval;
            int thisTextureCount;
            long lastsTime;

            animIndex = 0;
            thisRepeat = repeats.get(animIndex);
            thisNanoInterval = nanoIntervals.get(animIndex);
            thisTextureCount = textureCounts.get(animIndex);
            lastsTime = thisRepeat * thisNanoInterval * thisTextureCount;

            assert thisRepeat > 0;

            while(diff >= lastsTime){
                diff -= lastsTime;
                animIndex++;
                thisRepeat = repeats.get(animIndex);
                thisNanoInterval = nanoIntervals.get(animIndex);
                thisTextureCount = textureCounts.get(animIndex);
                lastsTime = thisRepeat * thisNanoInterval * thisTextureCount;
            }
            int textureIndex;
            Animation2D thisAnim = animList.get(animIndex);
            textureIndex = (int) (diff / thisAnim.nanoInterval) % thisAnim.texturesCount;

            return lastTuple = new Tuple<>(animIndex, textureIndex);
        }

        @Override
        public ResourceLocation getCurrentTextureFront() {
            Tuple<Integer, Integer> t = getCurrentTuple();
            return animList.get(t.getA()).textures.get(t.getB());
        }

        @Override
        public ResourceLocation getCurrentTextureBack() {
            Tuple<Integer, Integer> t = getCurrentTuple();
            return animList.get(t.getA()).texturesBack.get(t.getB());
        }

        @Override
        public RenderSetting getRenderSetting() {
            Tuple<Integer,Integer> t = getCurrentTuple();
            return animList.get(t.getA()).getRenderSetting();
        }

        @Override
        public TextureControlCode getStatusCode() {
            return statusCode;
        }

        @Override
        public void setStatusCode(TextureControlCode code) {
            this.statusCode = code;
        }

        @Override
        public int getProgress() {
            if(statusCode == TextureControlCode.PAUSE){
                return pauseProgress;
            }else{
                long diff = System.nanoTime() - baseNanoTime;
                return (int)((double)diff / (double)totalTime * MAX_PROGRESS);
            }
        }

        @Override
        public void setProgress(int progress) {
            if(progress > MAX_PROGRESS) progress = MAX_PROGRESS;
            baseNanoTime = System.nanoTime();
            baseNanoTime -= totalTime * ((double)progress / (double)MAX_PROGRESS);
            pauseProgress = progress;
        }

        @Override
        public TextureSetting getTextureSetting() {
            lastTuple = getCurrentTuple();
            return animList.get(lastTuple.getA()).getTextureSetting();
        }


        @Override
        public void pause() {
            this.pauseProgress = getProgress();
            this.statusCode = TextureControlCode.PAUSE;
        }

        @Override
        public void resume() {
            if(statusCode == TextureControlCode.PAUSE){
                setProgress(pauseProgress);
                this.statusCode = TextureControlCode.PLAYING;
            }
        }

        @Override
        public void tick() {
            getCurrentTuple();
        }
    }
}
