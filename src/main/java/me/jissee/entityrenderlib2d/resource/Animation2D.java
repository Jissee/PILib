package me.jissee.entityrenderlib2d.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class Animation2D implements Texture2D {
    private List<ResourceLocation> textures = new ArrayList<>();
    private List<Long> nanoTime = new ArrayList<>();
    private int repeat;
    private int repeatCopy;
    private int texturesCount;
    private int previousIndex = 0;
    private long baseNanoTime = -1;
    private long thisNanoTime = 0;
    private int fps = 0;
    private long nanoInterval = 0;
    private Texture2DManager.ControlCode statusCode;
    private long pauseNanoTime;
    private int pauseIndex;

    /**
     *
     * @param repeat Set the times should the animation repeat. Set -1 if the animation should repeat for infinite times.
     * @param nanoInterval The interval nanoseconds between two frames.
     */
    public Animation2D(int repeat, long nanoInterval){
        this.repeat = repeat;
        this.nanoInterval = nanoInterval;
        this.fps = (int) (1.0 / (nanoInterval / 1e9));
    }

    public Animation2D(int repeat, int fps){
        this.repeat = repeat;
        this.fps = fps;
        this.nanoInterval = (long) (1.0 / (double)fps * 1e9);
    }


    public void addTexture(ResourceLocation texture){
        texturesCount++;
        textures.add(texture);
    }



    @Override
    public ResourceLocation getCurrentTexture(){//res0 2 tk, res1 2 tk
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return textures.get(previousIndex);
        }
        previousIndex = getIndex();
        return textures.get(previousIndex);
    }

    public int getIndex(){
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return pauseIndex;
        }
        thisNanoTime = System.nanoTime();
        if(baseNanoTime == -1){
            baseNanoTime = thisNanoTime;
        }
        repeatCopy = repeat;
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

    public static Animation2D create(String modId, String indexPlaceholder, String resWithPlaceholder, int startIndex, int endIndex, long nanoInterval, int repeat){
        Animation2D anim = new Animation2D(repeat,nanoInterval);
        String tmp;
        for(int i = startIndex; i <= endIndex; i++){
            tmp = resWithPlaceholder.replaceAll(indexPlaceholder, "" + i);
            anim.addTexture(new ResourceLocation(modId,tmp));
        }
        return anim;
    }

    public static Animation2D fromSingleTexture2D(SingleTexture2D st2d){
        Animation2D anim = new Animation2D(-1, (long) 1e9);
        anim.addTexture(st2d.getCurrentTexture());
        return anim;
    }


    public static Animation2D combine(Animation2D anim1, Animation2D anim2, int repeat){
        long interval = anim1.nanoInterval;
        if(anim1.nanoInterval != anim2.nanoInterval){
            int fps1 = anim1.fps;
            int fps2 = anim2.fps;
            int resfps = (int) lcm(fps1,fps2);
            Animation2D anim = new Animation2D(repeat,resfps);

            int cnt1 = resfps / anim1.fps;
            int cnt2 = resfps / anim2.fps;
            int r = 0;

            r = anim1.repeat > 1 ? anim1.repeat : 1;
            for(; r > 0; r--){
                for(int i = 0; i < anim1.texturesCount; i++){
                    for(int j = 0; j < cnt1; j++){
                        anim.addTexture(anim1.textures.get(i));
                    }
                }
            }

            r = anim2.repeat > 1 ? anim2.repeat : 1;
            for(; r > 0; r--){
                for(int i = 0; i < anim2.texturesCount; i++){
                    for(int j = 0; j < cnt2; j++){
                        anim.addTexture(anim2.textures.get(i));
                    }
                }
            }
            return anim;

        }else {
            Animation2D anim = new Animation2D(repeat, interval);
            int r = 0;
            r = anim1.repeat > 1 ? anim1.repeat : 1;
            for(; r > 0; r--){
                for (ResourceLocation texture : anim1.textures) {
                    anim.addTexture(texture);
                }
            }
            r = anim2.repeat > 1 ? anim2.repeat : 1;
            for(; r > 0; r--){
                for (ResourceLocation texture : anim2.textures) {
                    anim.addTexture(texture);
                }
            }
            return anim;

        }
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
