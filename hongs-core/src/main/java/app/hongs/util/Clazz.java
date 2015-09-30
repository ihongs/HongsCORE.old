package app.hongs.util;

import app.hongs.CoreLogger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 通过包名获取类名集合
 * <p>
 * 此类被用于处理 _begin_.properties 中 core.load.serv 下 .* 后缀的包名扩展类名;
 * 但有个缺陷, 比如 app.demo.action 包存在于两个不同包下, 只有一个包的类会被找到;
 * 这是因为 ClassLoader 的 getResource() 并不会把所有不同 jar 中的同名资源都返回;
 * </p>
 * @author Hongs
 */
public class Clazz {

    /**
     * 通过包名获取类名集合
     * @param pkgn 包名
     * @param recu 递归
     * @return
     * @throws IOException
     */
    public static Set<String> getClassNames(String pkgn, boolean recu) throws IOException {
        ClassLoader pload = Thread.currentThread().getContextClassLoader();
        String      ppath = pkgn.replace( ".", "/" );
        URL         plink = pload.getResource(ppath);
        Set<String> names ;
        
        CoreLogger.trace("Clazz package path: " + plink);

        if (plink != null) {
            String  proto = plink.getProtocol();
            String  proot = plink.getPath( ).replaceFirst( "/$", "" );
            proot = proot.substring(0, proot.length()-ppath.length());
            
            CoreLogger.trace("Clazz package root: " + proot);

            if ( "jar".equals(proto)) {
                // 路径格式: file:/PATH!/
                int b = proot.indexOf("/") + 1;
                int e = proot.length (   ) - 2;
                proot = proot.substring(b , e);
                names = getClassNamesByJar(proot, ppath, recu);
            } else
            if ("file".equals(proto)){
                // 路径格式: /PATH/
                proot = proot.substring(1);
                names = getClassNamesByDir(proot, ppath, recu);
            } else {
                names = new HashSet();
            }
        } else {
            names = new HashSet( );
            URL[]   paurl = ((URLClassLoader) pload).getURLs();

            if (    null != paurl)
            for(URL pourl : paurl) {
                String proot = pourl.getPath( );
                // 忽略搜索: classes
                if (proot.endsWith("/classes/")) {
                    continue;
                }

                CoreLogger.trace("Clazz package root: " + proot);

                // 路径格式: file:/PATH!/
                int p = proot.indexOf("/") + 1 ;
                proot = proot.substring(p, proot.length() - 2);
                names.addAll(getClassNamesByJar(proot, ppath, recu));
            }
        }

        return  names;
    }

    private static Set<String> getClassNamesByDir(String root, String path, boolean recu) {
        Set<String> names = new HashSet();
        File[]      files = new File(root + path).listFiles();

        for (File file : files) {
            if (! file.isDirectory()) {
                String name = file.getPath().substring(root.length());
                if (name.endsWith(".class")) {
                    name = name.substring(0, name.lastIndexOf( '.' ));
                    name = name.replace(File.separator, ".");
                    names.add(name);
                }
            } else if (recu) {
                String name = path + File.separator + file.getName( );
                names.addAll(getClassNamesByDir(root, name, recu));
            }
        }

        return  names;
    }

    private static Set<String> getClassNamesByJar(String root, String path, boolean recu)
            throws IOException {
        Set<String> names = new HashSet();
        int         pathl = 1 + path.length();

        Enumeration<JarEntry> items = new JarFile(root).entries();
        while ( items.hasMoreElements( )) {
            String name = items.nextElement().getName();
            if (!name.startsWith( path )) {
                continue;
            }
            if (!name.endsWith(".class")) {
                continue;
            }
            name = name.substring(0, name.length() - 6);
            if (!recu && name.indexOf("/", pathl ) > 0) {
                continue;
            }
            name = name.replace("/", ".");
            names.add(name);
        }

        return  names;
    }

    /**
     * 测试
     * @param args 包名1 包名2... [-r(包含下级)]
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        boolean wcp  =  false;
        Set<String> pkgs = new HashSet();
        for (String pkg  :  args) {
            if ("-r".equals(pkg)) {
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

}
