# PI(π) Lib

<ol>
<li> What is PI Lib? </li>  

PI Lib is a Minecraft library mod that provides a simple way to create your 2D entities.

<li> How to implement in my project?</li>

Add the information of maven repositories and dependencies to 'build.gradle'

```
repositories {      
    maven { url 'https://dl.cloudsmith.io/public/jissee/pilib/maven/' }
}    

dependencies {
    implementation fg.deobf('me.jissee.pilib:PILib-<MC-version>:<LIB-version>')
}
```
Latest versions

| Minecraft | Lib  |
|:---------:|:----:|
|  1.19.3   | 3.14 |
|  1.19.2   | 3.14 |

Currently, it only runs on forge 1.19.2 and 1.19.3, but it is scheduled to back port to some legacy versions.   
Also, the compatibility on different platforms is also in need of development and test.

<li> How to use?   </li>

The precise javadocs (in Chinese and English) have been included in the source codes. You can refer to it. A wiki is also scheduled to be written.

<li> Cooperation </li>

It would be appreciated if you can help with this project. Some further functions are in need of development.    
Also, the compatibility on different platforms is also in need of development and test.    
Legacy version development and fabric development are also needed.

<li> ADDITIONAL LICENSE </li>

ANY PARTS OF THIS PROJECT ARE PROHIBITED FROM BEING USED ON NETEASE VERSION

</ol>