#!/usr/bin/python
#coding=utf-8

import os
import time
import shutil
import tarfile

def hsBakup(sp, dn, tn, gz):
    """
    sp 待备份的文件或目录
    dn 备份的目录
    tn 备份的名称
    gz 是否要压缩
    名称可以使用日期参数如:
    %Y/%m/%d/{N}_%Y%m%d%H%M%S{X}
    其中 {N} 为文件名 {X} 为扩展名
    开启压缩则再加上扩展名 .tar.gz
    """

    fn = sp
    fe = sp
    tn = time.strftime(tn)
    tn = tn.replace("{N}", sp)
    tn = tn.replace("{E}", fe)
    tn = dn + "/" + tn
    shutil.copytree(sp, tn)

    if  gz:
        zp = tarfile.open(tn+".tar.gz", 'w:gz')
        zp.add(sp)
        zp.close()
        os.remove(tn)

if  __name__ == "__main__":
    def cmd_help():
        print "Usage: bakup.py SRC_PATH DEST_PATH TIME_NAME"
        print "Another options:"
        print "  -z --gzip             Gzip the back"
        print "  -h --help             Show this msg"
