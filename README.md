# VKMessanger

##Правила работы с репозиторием:

Перед началом работы с репозиторием каждый раз необходимо обновить проект с помощью меню:
>VCS -> Update Project

Получить актуальные данные с интересующей ветки можно с помощью меню:
>VCS -> Git -> Pull

###Master branch
В ветку **'master'** ничего нельзя **коммитить**, а также производить **слияние** без предварительного согласования 
с другими членами комманды.

###Наименование веток

#####Разработка
Ветки в которых происходит разработка должны иметь имя **'develop-XXXXX'**

#####Добавление нового функционала
В случае, когда надо добавить новый функционал к существующей части проекта, необходимо
создать отдельную ветку **'feature-XXXXX'**, в которой и производить изменения кода, после 
чего слить ее в соответствующую ветку 'develop-XXXXX'

#####Исправление багов
Все исправления найденных ошибок, которые уже были закоммичены ранее,
необходимо проводить в отдельных ветках **'hotfix-XXXXX'**
