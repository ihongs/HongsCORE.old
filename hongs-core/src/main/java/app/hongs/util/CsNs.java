package app.hongs.util;

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
 * @author Hongs
 */
public class CsNs {
    /**
     * 测试
     * @param args 包名1 包名2... [-w(包含下级)]
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

    /**
     * 通过报名获取类名集合
     * @param pkgn 包名
     * @param recu 递归
     * @return
     * @throws IOException
     */
    public static Set<String> getClassNames(String pkgn, boolean recu) throws IOException {
        ClassLoader pload = Thread.currentThread().getContextClassLoader();
        String      ppath = pkgn.replace(".", "/");
        URL         ppurl = pload.getResource  (  ppath );System.out.println(ppurl);
        Set<String> names ;

        if (ppurl != null) {
            String  proot = ppurl.getPath();
            proot = proot.substring(0, proot.length() - ppath.length());System.out.println(proot);
            if ("file".equals(ppurl.getProtocol())){
                // 路径格式: /PATH/
                proot = proot.substring(1);
                names = getClassNamesByDir(proot, ppath, recu);
            } else
            if ( "jar".equals(ppurl.getProtocol())) {
                // 路径格式: file:/PATH!/
                int p = proot.indexOf("/") + 1 ;
                proot = proot.substring(p, proot.length() - 2);
                names = getClassNamesByJar(proot, ppath, recu);
            } else {
                names = new HashSet();
            }
        } else {
            names = new HashSet();
            URL[]   paurl = ((URLClassLoader) pload).getURLs();
            if (paurl != null) for (URL pourl : paurl) {
                String proot = pourl.getPath( );
                // 忽略搜索: classes
                if (proot.endsWith("/classes/")) {
                    continue;
                }
                // 路径格式: file:/PATH!/
                int p = proot.indexOf("/") + 1 ;
                proot = proot.substring(p, proot.length() - 2);
                names.addAll(getClassNamesByJar(proot, ppath, recu));
            }
        }
        return  names;
    }

    private static Set<String> getClassNamesByDir(String root, String path, boolean recu) {
        Set<String> names = new HashSet<String>();
        File        fileo = new File(root + path);
        File[]      files = fileo.listFiles();

        for (File file : files) {
            if (! file.isDirectory()) {
                String name = file.getPath().substring(root.length());
                if (name.endsWith(".class")) {
                    name = name.substring(0, name.lastIndexOf( "." ));
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
        Set<String> names = new HashSet<String>();

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
            if (!recu && name.lastIndexOf( "/" ) > path.length()) {
                continue;
            }
            name = name.replace("/", ".");
            names.add(name);
        }

        return  names;
    }

}
