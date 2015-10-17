#!/usr/bin/python
#coding=utf-8

# 压缩命令
# 其中 {D} 为目录路径 {S} 为源文路径
GZ_CMD = "tar cvfz {D} {S}"

def hsBakup(sp, dn, tn, gz):
    """
    sp 待备份的文件或目录
    dn 备份的目录
    tn 备份的名称
    gz 是否要压缩
    名称可以使用日期参数如:
    %Y/%m/%d/{N}_%Y%m%d%H%M%S.{E}
    其中 {N} 为文件名 {E} 为扩展名
    开启压缩则再加上扩展名 .tar.gz
    """
    pass

if  __name__ == "__main__":
    def cmd_help():
        print "Usage: bakup.py SRC_PATH DEST_PATH TIME_NAME"
        print "Another options:"
        print "  -z --gzip             Gzip the back"
        print "  -h --help             Show this msg"
