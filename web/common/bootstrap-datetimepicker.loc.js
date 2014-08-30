/**
 * Use i18n configure by HongsCORE
 * Huang Hong <ihongs@live.cn>
 */
;(function($){
    $.fn.datetimepicker.dates['en'] = {
        months      : hsGetLang("date.LM"),//["一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月", "十二月"],
        monthsShort : hsGetLang("date.SM"),//["一","二","三","四","五","六","七","八","九","十","十一","十二"],
        days        : hsGetLang("date.LE"),//["星期日","星期一","星期二","星期三","星期四","星期五","星期六"],
        daysShort   : hsGetLang("date.SE"),//["日","一","二","三","四","五","六"],
        daysMin     : hsGetLang("date.SE"),//["日","一","二","三","四","五","六"],
        meridiem    : hsGetLang("time.Sa"),//["AM","PM"],
        weekStart   : hsGetLang("week.start"),
        today       : hsGetLang("date.today"),
        format      : hsGetLang("datetime.format"),
        suffix      : []
    };
}(jQuery));
