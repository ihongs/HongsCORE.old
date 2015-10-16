#!/usr/bin/python
#coding=utf-8

def hsBakup(sp, dn, tn, gz):
    """
    sp 待备份的文件或目录
    dn 备份的目录
    tn 备份的名称
    gz 是否压缩
    名称可以使用日期参数如: %Y/%m/%d/{n}_%Y-%m-%d_%H-%M-%S.{x}
    """
    pass

if  __name__ == "__main__":
    def cmd_help():
        print "Usage: bakup.py SRC_PATH DEST_PATH TIME_NAME"
        print "Another options:"
        print "  -z --gzip             Gzip the file"
        print "  -h --help             Show this msg"
