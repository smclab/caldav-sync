# CalDAV Sync - Liferay Workspace for Liferay 7.0+ CE e DXP

This workspace contains the plugin "CalDAV sync" that allows Liferay to act like a CalDAV Server.

CalDAV Sync plugin let's you access your Liferay calendar from any CalDAV enabled client. We support the following clients:
* iPhone
* iPad
* Mac Calendar App
* Thunderbird
* Android (requires third party app)
* Outlook (requires third party app)

Please visit our link for documentations and manuals:
[https://liferaypartneritalia.smc.it/calendar-mobile-sync](https://liferaypartneritalia.smc.it/calendar-mobile-sync)

Some hints for contributors:
* this repository manages and maintains compatibility with both the CE and DXP versions of Liferay
* in this branch the basic compatibility, managed through BOM in the gradle.properties file, is:
    - Liferay 7.3 CE GA6 (7.3.5)
    - Liferay 7.3 DXP GA1
* any compatibility issues with GA or subsequent fixpacks must be managed through new targeted artifacts
