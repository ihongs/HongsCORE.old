/**
 * Use i18n configure by HongsCORE
 * Huang Hong <ihongs@live.cn>
 */
;(function($){
    $.fn.datetimepicker.dates['zh-CN'] = {
        months      : hsGetLang("date.LM"),//["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        monthsShort : hsGetLang("date.SM"),//["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        days        : hsGetLang("date.LE"),//["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
        daysShort   : hsGetLang("date.SE"),//["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
        daysMin     : hsGetLang("date.SE"),//["日", "一", "二", "三", "四", "五", "六", "日"],
        meridiem    : hsGetLang("time.La"),//["上午", "下午"],
        weekStart   : hsGetLang("week.start"),
        today       : hsGetLang("date.today"),
        format      : hsGetLang("datetime.format"),
        suffix      : []
    };
}(jQuery));
