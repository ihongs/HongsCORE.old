package app.hongs.util;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 通过包名获取类名集合
 * @author Hongs
 */
public class ClassNames {
    /**
     * 测试
     * @param args 包名1 包名2... [-w(包含下级)]
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        boolean wcp  =  false;
        Set<String> pkgs = new HashSet();
        for (String pkg  :  args) {
            if ("-w".equals(pkg)) {
                wcp  =  true ;
            } else {
                pkgs.add(pkg);
            }
        }
        for (String pkg  :  pkgs) {
            Set<String> clss = getClassNames(pkg, wcp);
            for(String  cls  : clss) {
                System.out.println(cls);
            }
        }
    }

    /**
     * 通过包名获取类名集合(不含下级包)
     * @param packageName
     * @return
     * @throws IOException
     */
    public static Set<String> getClassNames(String packageName) throws IOException {
        return getClassNames(packageName, false);
    }

    /**
     * 通过报名获取类名集合
     * @param packageName
     * @param withChildPackage 是否包含下级包
     * @return
     * @throws IOException
     */
    public static Set<String> getClassNames(String packageName, boolean withChildPackage) throws IOException {
        Set<String>     names = null;
        ClassLoader     pload = Thread.currentThread().getContextClassLoader();
        String          ppath = packageName.replace(".", "/");
        URL             ppurl = pload.getResource  (  ppath );

        if (ppurl != null) {
            String type = ppurl.getProtocol();
            if (type.equals("file")) {
                names = getClassNamesByDir(ppurl.getPath(), withChildPackage);
            } else
            if (type.equals("jar" )) {
                names = getClassNamesByJar(ppurl.getPath(), withChildPackage);
            }
        } else {
            names = new HashSet();
            URL[] paurl = ((URLClassLoader) pload).getURLs();
            if (paurl != null) for (URL pourl : paurl) {
                String psurl = pourl.getPath( );
                // classes 不需要搜索
                if (psurl.endsWith("classes/")) {
                    continue;
                }
                String jarPath = psurl+"!/"+ppath;
                names.addAll( getClassNamesByJar(jarPath, withChildPackage) );
            }
        }
        return names;
    }

    private static Set<String> getClassNamesByDir(String path, boolean withChildPackage) {
        Set<String> names = new HashSet<String>();
        File        dfile = new File( path  );
        File[]      files = dfile.listFiles();

        for (File file : files) {
            if (! file.isDirectory()) {
                String name = file.getPath();
                if (name.endsWith(".class")) {
                    name = name.substring(name.indexOf("\\classes")+9, name.lastIndexOf("."));
                    name = name.replace("\\", ".");
                    names.add(name);
                }
            } else if (withChildPackage) {
                names.addAll(getClassNamesByDir(file.getPath(), withChildPackage));
            }
        }

        return names;
    }

    private static Set<String> getClassNamesByJar(String path, boolean withChildPackage)
            throws IOException {
        Set<String>     names = new HashSet<String>();
        String[]        namez = path.split("!");
        String          ppath = namez[1].substring(1);
        String          jpath = namez[0].substring(namez[0].indexOf("/"));

        Enumeration<JarEntry> items = new JarFile(jpath).entries();
        while ( items.hasMoreElements( )) {
            String ename = items.nextElement().getName();
            if (ename.endsWith(".class")) {
                if ( ! withChildPackage ) {
                    String pname;
                    int index = ename.lastIndexOf( "/" );
                    if (index == -1) {
                        pname = ename;
                    } else {
                        pname = ename.substring(0,index);
                    }
                    if (pname.equals  (  ppath)) {
                        ename = ename.replace("/", ".").substring(0, ename.lastIndexOf("."));
                        names.add(ename);
                    }
                } else {
                    if (ename.startsWith(ppath)) {
                        ename = ename.replace("/", ".").substring(0, ename.lastIndexOf("."));
                        names.add(ename);
                    }
                }
            }
        }

        return names;
    }

}
