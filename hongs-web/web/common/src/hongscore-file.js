/**
 * File 国际化及初始化
 * Huang Hong <ihongs@live.cn>
 * @param $ jQuery
 */
(function($) {
    if (!$.fn.fileinput) {
        return;
    }

    $.fn.fileinputLocales.en = {
        fileSingle      : hsGetLang('file.single'),
        filePlural      : hsGetLang('file.plural'),
        browseLabel     : hsGetLang('file.browse'),
        removeLabel     : hsGetLang('file.remove'),
        removeTitle     : hsGetLang('file.remove.title'),
        uploadLabel     : hsGetLang('file.upload'),
        uploadTitle     : hsGetLang('file.upload.title'),
        cancelLabel     : hsGetLang('file.cancel'),
        cancelTitle     : hsGetLang('file.cancel.title'),
        dropZoneTitle   : hsGetLang('file.drop.to.here'),

        fileActionSettings: {
            removeTitle : hsGetLang('file.remove'),
            uploadTitle : hsGetLang('file.upload'),

            indicatorNewTitle     : 'Not uploaded yet',
            indicatorLoadingTitle : 'Uploading ...',
            indicatorSuccessTitle : 'Uploaded',
            indicatorErrorTitle   : 'Upload Error'
        },

        msgLoading              : 'Loading file {index} of {files} &hellip;',
        msgProgress             : 'Loading file {index} of {files} - {name} - {percent}% completed.',
        msgSelected             : '{n} {files} selected',
        msgZoomTitle            : 'View details',
        msgZoomModalHeading     : 'Detailed Preview',
        msgSizeTooLarge         : 'File "{name}" (<b>{size} KB</b>) exceeds maximum allowed upload size of <b>{maxSize} KB</b>.',
        msgFilesTooLess         : 'You must select at least <b>{n}</b> {files} to upload.',
        msgFilesTooMany         : 'Number of files selected for upload <b>({n})</b> exceeds maximum allowed limit of <b>{m}</b>.',
        msgFileSecured          : 'Security restrictions prevent reading the file "{name}".',
        msgFileNotFound         : 'File "{name}" not found!',
        msgFileNotReadable      : 'File "{name}" is not readable.',
        msgFilePreviewAborted   : 'File preview aborted for "{name}".',
        msgFilePreviewError     : 'An error occurred while reading the file "{name}".',
        msgUploadAborted        : 'The file upload was aborted',
        msgValidationError      : 'File Upload Error',
        msgFoldersNotAllowed    : 'Drag & drop files only! Skipped {n} dropped folder(s).',
        msgInvalidFileType      : 'Invalid type for file "{name}". Only "{types}" files are supported.',
        msgInvalidFileExtension : 'Invalid extension for file "{name}". Only "{extensions}" files are supported.',

        msgImageWidthSmall      : 'Width of image file "{name}" must be at least {size} px.',
        msgImageHeightSmall     : 'Height of image file "{name}" must be at least {size} px.',
        msgImageWidthLarge      : 'Width of image file "{name}" cannot exceed {size} px.',
        msgImageHeightLarge     : 'Height of image file "{name}" cannot exceed {size} px.',
        msgImageResizeError     : 'Could not get the image dimensions to resize.',
        msgImageResizeException : 'Error while resizing the image.<pre>{errors}</pre>'
    };

    $.extend($.fn.fileinput.defaults, $.fn.fileinputLocales.en);

    $(document).on("hsReady", function() {
        $(this).find("[data-toggle=fileinput]").each(function() {
            if ($(this).data("fileinput")) {
                return;
            }

            var that = $(this);
            var attr;
            var opts;

            // 基础配置
            attr = that.attr("data-config" );
            if (attr) {
                opts =  eval("{"+ attr +"}");
            } else {
                opts =  { };
            }
            if (opts.showCaption === undefined) {
                opts.showCaption  =  false;
            }
            if (opts.showRemove  === undefined) {
                opts.showRemove   =  false;
            }
            if (opts.showUpload  === undefined) {
                opts.showUpload   =  false;
            }
            if (opts.browseClass === undefined) {
                opts.browseClass  =  "btn btn-default form-control";
            }

            // 预览类型
            attr = that.attr( "data-type"  );
            if (attr) {
                opts.previewFileType  = attr;
            }

            // 预览文件
            attr = that.attr( "data-files" );
            if (attr) {
                opts.initialPreview   = eval("["+ attr +"]");
            }

            // 类型校验
            attr = that.attr( "data-types" );
            if (attr) {
                opts.allowedFileTypes = eval("["+ attr +"]");
            }
            attr = that.attr( "data-extns" );
            if (attr) {
                opts.allowedFileExtensions = eval("["+ attr +"]");
            }

            that.removeClass( "input-file" );
//              .removeClass("form-control");
            that.fileinput(opts);
        });
    });
})(jQuery);