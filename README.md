# PILib(π)

<ol>
<li> What is PILib? </li>  

PILib is a Minecraft library mod that provides a simple way to create your 2D entities.

<li> How to get release version? (For playing purpose)</li>

Latest versions

| Minecraft |  Lib   |
|:---------:|:------:|
|  1.19.3   | 3.14.1 |
|  1.19.2   | 3.14.1 |

Currently, all latest release versions can be found in release page on the right. You just need to download them and put them in your "mods" folder.
But note that this mod does not provide any contents. It only provides APIs for other mods who provide new contents.

<li> How to implement in my project? (For development purpose)</li>

Add the information of maven repositories and dependencies to 'build.gradle'

```
repositories {      
    maven { url 'https://dl.cloudsmith.io/public/jissee/pilib/maven/' }
}    

dependencies {
    implementation fg.deobf('me.jissee.pilib:PILib-<MC-version>:<LIB-version>')
}
```

Currently, it only runs on forge 1.19.2 and 1.19.3, but it is scheduled to back port to some legacy versions.   
Also, the compatibility on different platforms is also in need of development and test.

<li> How to develop with PILib?   </li>

The javadocs (in Chinese and English) have been included in the source codes. You can refer to it. A wiki is also scheduled to be written.

<li> Cooperation </li>

It would be appreciated if you can help with this project. Some further functions are in need of development.    
Also, the compatibility on different platforms is also in need of development and test.    
Legacy version development and fabric development are also needed.

<li> ADDITIONAL LICENSE </li>

ANY PARTS OF THIS PROJECT ARE PROHIBITED FROM BEING USED ON NETEASE VERSION

</ol>