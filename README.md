# CalDAV Sync - Liferay Workspace for Liferay 7.0 CE e DXP

* [Getting Started Liferay Workspace](docs/GETTING_STARTED_LIFERAY.markdown)
* [Gradle Workspace hints](docs/GRADLE.md)

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
    - **JDK 8** (prefer ones from adoptium.net)
    - Liferay 7.4 CE GA64
    - Liferay 7.4 DXP U64
* any compatibility issues with GA or subsequent fixpacks must be managed through new targeted artifacts
