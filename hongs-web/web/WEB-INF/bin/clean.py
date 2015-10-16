#!/usr/bin/python
#coding=utf-8

# 过期文件清理工具
# 用于清理超过一定时间的日志、临时文件
# 作者: kevein.hongs@gmail.com
# 修改: 2015/10/17

import os
import sys
import re
import time
import datetime
from getopt import getopt

def hsClean(dn, tm, ep, pr, nm, ne):
    """
    清理工具
    dn: 待清理的目录
    tm: 清除此时间前的文件
    ep: 删除空的目录
    pr: 仅输出不删除
    nm: 文件名称正则
    ne: 排除 nm 匹配的文件
    """

    fc  = 0
    fe  = 0
    for fi in os.listdir(dn):
        if  fi == "." or fi == "..":
            continue

        fn  = os.path.join(dn, fi)
        if  os.path.islink(fn):
            continue
        if  os.path.isfile(fn):
            st  =  os.stat(fn)

            if  tm < st.st_mtime:
                continue
            if  nm:
                if  nm.match(fi):
                    if  ne:
                        continue
                else:
                    if  not ne:
                        continue

            print time.strftime("%Y/%m/%d %H:%M:%S", time.localtime(st.st_mtime)), fn
            if  not pr:
                os.remove(fn)
            fe += 1
        else:
            ap  = hsClean(fn, tm, ep, pr, nm, ne)

            if  not ap:
                continue
            if  not ep:
                continue

            print "0000/00/00 00:00:00", fn
            if  not pr:
                os.remove(fn)
            fe += 1
        fc += 1

    return  fc == fe

if  __name__ == "__main__":
    def cmd_help():
        print "Usage: system.clean.py DIR_NAME EXP_TIME"
        print "EXP_TIME format:"
        print "  2015/12/17T12:34:56   Before this time"
        print "  1234567890            Before this timestamp"
        print "  1w2d3h5m6s            Before some weeks, days..."
        print "Another options:"
        print "  -p --print            Just print files"
        print "  -e --empty            Remove empty dir"
        print "  -n --name             File name regexp"
        print "  -x --deny             Exclude names"
        print "  -h --help             Show this msg"

    if  len(sys.argv) < 3:
        cmd_help( )
        sys.exit(0)

    dn = sys.argv[1]
    tm = sys.argv[2]
    ep = False
    pr = False
    nm = None
    ne = False

    if  not dn:
        print "Argument 1 (folder name) required!"
        cmd_help( )
        sys.exit(1)
    if  not tm:
        print "Argument 2 (expire time) required!"
        cmd_help( )
        sys.exit(1)

    opts, args = getopt(sys.argv[3:], "pen:xh", ["print", "empty", "name=", "deny", "help"])
    for n,v in opts:
        if  n in ("-p", "--print"):
            pr = True
        if  n in ("-p", "--empty"):
            ep = True
        if  n in ("-n", "--name"):
            nm = v
        if  n in ("-d", "--deny"):
            de = True
        if  n in ("-h", "--help"):
            cmd_help( )
            sys.exit(0)

    # 时间格式:
    # 1234567890
    # 1w2d3h5m6s
    # 2015/10/11
    # 2015/10/11.10:20:30
    while True:
        mt = re.compile(r"^\d+$").match(tm)
        if  mt:
            tm = int(tm)
            break

        mt = re.compile(r"^(\d+w)?(\d+d)?(\d+h+)?(\d+m)?(\d+s)?$").match(tm)
        if  mt:
            tm = datetime.datetime.now()
            tg = mt.group(1)
            if  tg:
                tm -= datetime.timedelta(weeks=int(tg[:-1]))
            tg = mt.group(2)
            if  tg:
                tm -= datetime.timedelta( days=int(tg[:-1]))
            tg = mt.group(3)
            if  tg:
                tm -= datetime.timedelta(hours=int(tg[:-1]))
            tg = mt.group(4)
            if  tg:
                tm -= datetime.timedelta(minutes=int(tg[:-1]))
            tg = mt.group(5)
            if  tg:
                tm -= datetime.timedelta(seconds=int(tg[:-1]))
            tm = time.mktime(tm.timetuple())
            break

        if  len(tm) <= 10:
            tm = time.mktime(time.strptime(tm, r"%Y/%m/%d"))
        else:
            tm = time.mktime(time.strptime(tm, r"%Y/%m/%d.%H:%M:%S"))
        break

    dn  = os.path.abspath(dn)
    if  nm:
        nm  =  re.compile(nm)

    print "Delete files before " + time.strftime(r"%Y/%m/%d %H:%M:%S", time.localtime(tm)) + " in " + dn

    hsClean(dn, tm, ep, pr, nm, ne)
