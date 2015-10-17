-- DB: module

DELETE FROM a_module_data WHERE ctime < '{{yyyy/MM/dd|-2w}}';
