package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
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
     * Create a collection of animation by a series of file name.
     * @param modId            Mod Id.
     * @param indexPlaceholder The placeholder of the image series. Must appear in resourceName.
     * @param resourceName     The resource name with the placeholder.
     * @param startIndex       The placeholder will be replaced with indexes, starting from startIndex and
     * @param endIndex         end at endIndex.
     * @param nanoInterval     The interval between two frames in nanoseconds.
     * @param repeat           Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param setting          The {@link RenderSetting} of this Animation.
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
     * Create a collection of animation by a series of file name.
     * @param modId             Mod Id.
     * @param indexPlaceholder  The placeholder of the image series. Must appear in resourceName.
     * @param frontResourceName The resource name with the placeholder of the front texture.
     * @param backResourceName  The resource name with the placeholder of the back texture
     * @param startIndex        The placeholder will be replaced with indexes, starting from startIndex and
     * @param endIndex          end at endIndex.
     * @param nanoInterval      The interval between two frames in nanoseconds.
     * @param repeat            Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param setting           The {@link RenderSetting} of this Animation.
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
     * Create an Animation2D from SingleTexture2D.
     */
    public static Animation2D fromSingleTexture2D(SingleTexture2D st2d){
        Animation2D anim = new Animation2D(-1, (long) 1e9, st2d.getRenderSetting());
        anim.addTexture(st2d.getCurrentTextureFront(), st2d.getCurrentTextureBack());
        return anim;
    }

    /**
     * Combine two animations.
     * @param anim1 The first animation.
     * @param anim2 The second animation.
     * @param repeat The times that the whole animation should repeat.
     */
    public static Animation2D combine(Animation2D anim1, Animation2D anim2, int repeat,RenderSetting setting){
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
     *
     * @param repeat        Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param nanoInterval  The interval between two frames in nanoseconds.
     * @param setting       The {@link RenderSetting} of this Animation.
     */
    public Animation2D(int repeat, long nanoInterval, RenderSetting setting){
        this.repeat = repeat;
        this.nanoInterval = nanoInterval;
        this.fps = (int) (1.0 / (nanoInterval / 1e9));
        this.setting = setting;
    }
    /**
     *
     * @param repeat        Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param fps           frames per second.
     * @param setting       The {@link RenderSetting} of this Animation.
     */
    public Animation2D(int repeat, int fps, RenderSetting setting){
        this.repeat = repeat;
        this.fps = fps;
        this.nanoInterval = (long) (1.0 / (double)fps * 1e9);
        this.setting = setting;
    }


    public void addTexture(ResourceLocation texture){
        addTexture(texture, null);
    }

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
