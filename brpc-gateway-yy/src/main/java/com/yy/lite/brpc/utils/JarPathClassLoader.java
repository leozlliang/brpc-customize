package com.yy.lite.brpc.utils;

import com.google.common.base.Predicate;
import com.yy.lite.brpc.namming.s2s.annotation.S2SNamming;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Created by yihui on 2017/8/19.
 */
@Slf4j
public class JarPathClassLoader extends URLClassLoader {

    private String packageName;


    public JarPathClassLoader(URL[] urls, String pkgName) {
        super(urls);
        packageName = pkgName;
    }

    /**
     * 扫描包路径下所有的class文件
     *
     * @return
     */
    public  Set<Class<?>> getClzFromPkg() {
        String pkg= packageName;
        Set<Class<?>> classes = new LinkedHashSet<>();

        try {
            for(URL url : this.getURLs()){
                JarFile jar = new JarFile(url.getFile());
                findClassesByJar(pkg, jar, classes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }





    /**
     * 扫描包路径下的所有class文件
     *
     * @param pkgName 包名
     * @param jar     jar文件
     * @param classes 保存包路径下class的集合
     */
    private  void findClassesByJar(String pkgName, JarFile jar, Set<Class<?>> classes) {
        String pkgDir = pkgName.replace(".", "/");


        Enumeration<JarEntry> entry = jar.entries();

        JarEntry jarEntry;
        String name, className;
        Class<?> claze;
        while (entry.hasMoreElements()) {
            jarEntry = entry.nextElement();

            name = jarEntry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }


            if (jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith(".class")) {
                // 非指定包路径， 非class文件
                continue;
            }


            // 去掉后面的".class", 将路径转为package格式
            className = name.substring(0, name.length() - 6).replace("/", ".");
            claze = loadUrlClass(className);
            if (claze != null) {
                classes.add(claze);
            }
        }
    }


    private  Class<?> loadUrlClass(String fullClzName) {
        try {
            return loadClass(fullClzName);
        } catch (Exception e) {
            log.error("load class error! clz: {}, e:{}", fullClzName, e);
        }catch (Throwable te){
            log.error("load class error! clz: {}, e:{}", fullClzName, te);
        }
        return null;
    }

    public  Set<Class<?>> getClzFromPkg( Predicate<Class> filter){
        Set<Class<?>> classSet =  getClzFromPkg();
        if(CollectionUtils.isEmpty(classSet)){
            return  classSet;
        }
        return classSet.stream().filter(aClass -> filter.apply(aClass)).collect(Collectors.toSet());
    }

    public static URL[] getURLsByJarRoot(String jarPath) throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(jarPath), new String[]{"jar"},true);
        File[] filess = new File[files.size()];
        URL[] urls = FileUtils.toURLs(files.toArray(filess));
        return urls;
    }


    public static void main(String[] args) throws IOException {
        String jarPath = System.getProperty("user.dir") + "\\test";
        URL[] urls = JarPathClassLoader.getURLsByJarRoot(jarPath);
        JarPathClassLoader pkgUtil = new JarPathClassLoader(urls,"com.yy");
        Set<Class<?>> classSet =  pkgUtil.getClzFromPkg( new Predicate<Class>() {
            @Override
            public boolean apply( Class aClass) {
                return  aClass.isInterface() && aClass.getAnnotation(S2SNamming.class)!=null;
            }
        });
        System.out.print(classSet);



    }
}