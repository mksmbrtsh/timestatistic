[![](https://owncloud.org/wp-content/themes/owncloudorgnew/assets/img/clients/buttons/googleplay.png)](https://play.google.com/store/apps/details?id=maximsblog.blogspot.com.timestatistic)  
Свободный проект для учета времени на мобильных устройствах под управлением ОС Android.
[![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Get%20timestatistic%20170%20free%204&url=https://github.com/mksmbrtsh/timestatistic&via=mksmbrtsh&hashtags=timestatistic,android,androidapp)
  
[![](https://upload.wikimedia.org/wikipedia/ru/d/d6/Sberbank.svg)](https://www.sberbank.ru/ru/person)  
Поддержать проект, номер карты сбербанка: 4817 7601 3803 3687
# Обратная связь
Идея: <a href="mailto:ruslan.popov@gmail.com">Ruslan Popov</a>  
Дизайн иконки запуска: Сергей Барков (superbarok)  
Дизайн иконок меню: Anna Bartosh  
Разработчик: <a href="mailto:mksmbtrsh@gmail.com">Maksim Bartosh</a>  
Другие приложения на <a href="https://play.google.com/store/search?q=maksim+bartosh&amp;c=apps">GooglePlay</a>  
<a href="mailto:mksmbtrsh@gmail.com">Связь для предложений и замечаний</a>
# Кратная справка
Главный экран приложения имеет 4-ре вкладки: счётчики, записи, диаграмма и дневник.  
На вкладке счётчики можно создавать, редактировать и удалять счётчики. <b>Счётчик - это действие</b>.  
При создании и редактировании можно указать название и цвет плитки счётчика, задать его порядковый номер и интервал напоминания.
Удаление счётчика приводит к удалению всех записей, относящихся к нему, будьте внимательны.  
По умолчанию работает счётчик <b>Idle</b>, на него же происходит переключение, если нажать на работающий счетчик.
Т.е. он используется для учета времени простоя, "ненужного" времени и тд.  
## Фильтрация времени  
По умолчанию, фильтрация отключена (учитывается всё время с момента установки программы). Обрабатывается всё время от момента установки приложения.  
Фильтрацию можно задать двумя параметрами: начало учёта и конец учёта.  
##Начало учёта  
Можно указывать следующие значения:  
1. С сегодня - текущие сутки с 00:00;  
2. Со вчера - предыдущие сутки с 00:00;  
3. С начала недели - первый день недели с 00:00;  
4. С начала месяца - первый день месяца с 00:00;  
5. С начала года - первый день года с 00:00;  
6. С установки программы;  
7. Установить своё значение.  
В последнем варианте можно установить любую дату и время, но раньше, чем сегодня и сейчас.  
## Конец учёта  
Можно указывать следующие значения:  
1. До завтра - до 24:00;  
2. До начала сегодня - до 00:00;  
3. До конца недели;  
4. До конца месяца;  
5. До конца года;  
6. Не задано;  
7. Установить своё значение.  
В последнем варианте можно установить любую дату и время. Шестой вариант означает, что диаграмма будет строиться до текущего момента времени, а счетчик обратного отчета не будет отображаться на экране. В остальных случаях, если время окончания больше, чем сейчас, то на диаграмме будет отмечено черным цветом сколько осталось.  
# Для сборки
Надо org.achartengine - библиотека для построения графиков.  
#Что нового:
```
1.0.33  
- убрана реклама;  
+ добавлен экспорт в календарь и в csv-файл.  
1.0.32  
+ виджет счетчиков на рабочий стол;  
1.0.31  
* подправлен немецкий язык;  
в про версии  
- убран левый бар, пофикшено торможение;  
+ настройка фильтра снесена влево;  
1.0.30  
+ добавлено общее время на круговую диаграмму(https://github.com/mksmbrtsh/timestatistic/issues/20).  
1.0.29  
* исправлена критическая ошибка с объединением интервалов;  
PRO-версия  
+ slide-меню слева;  
+ возможность выбора какие счетчики учитывать в круговой диаграмме;  
* два главный таба: счётчики и записи;  
* лаг переключения между табами;  
* автообновление фильтра на главной форме.  
1.0.28  
+ новый экран о программе с историей версий;  
+ выбор текста легенды диаграмм;  
+ новая информация при нотификации;  
* исправлено не сохранение записи дневника;  
+ настройка для включения и выключения вибрации.  
1.0.27  
+ экспорт в CSV;  
+ расшаривание диаграммы в виде картинки.  
1.0.26  
+ периоды, суммарное время, среднее арифметическое, диаграмма;  
+ бекап в Google Drive;  
+ экспорт в Google calendar;  
+ ПРО-версия.  
1.0.25  
+ новая легенда для диаграмм;  
+ иконки на вкладке;  
* исправление ошибок.  
1.0.24.3  
* исправление ошибок.  
1.0.24.2  
* исправление ошибок.  
1.0.24  
+ сортировка счетчиков;  
+ фильтры по времени;  
+ серый цвет для счетчиков;  
1.0.23  
+ автостарт напоминания при перезагрузке устройства;  
* исправление дизайна на маленьких экранах;  
* исправление ошибок.  
1.0.22  
+ вкладка дневник.  
1.0.21  
+ возможность указать дату и время отчета (сегодня, с начала недели, месяца, года);  
* исправление ошибок.  
1.0.11  
+ напоминание.  
1.0.10  
+ объединение записей.  
1.0.9  
+ разбитие записей.  
1.0.8  
+ выбор цвета для счетчиков.  
1.0.7  
+ удаление счетчика и сброс всех в ноль.  
1.0.6  
+ добавлена диаграмма.  
1.0.5  
+ вкладка записи.  
alpha3  
+ счетчики.  
```
