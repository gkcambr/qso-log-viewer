# qso-log-viewer
An application that analyzes ham radio logbooks for duplicate and missing records

For a full tutorial go to http://radiocheck.us/QsoLogViewerHelp.html

Requires a current Java run-time environment. Download QsoLogServer.jar and invoke the application with:

java -jar QsoLogViewer.jar

You can create a shortcut for your desktop:
- copy QsoLogViewer.jar to a working directory, such as C:/apps/QsoLog
- copy the file QsoLogViewer_48.ico from the github master/images directory to your working directory
- create a desktop shortcut with a target of 'javaw -jar YOUR_WORK_DIR/QsoLogViewer.jar and add the icon file QsoLogViewer_48.ico to the shortcut.

Description:
Ham radio operators use a number of different databases to manage their contacts, or QSOs. Many are web-hosted sites that are viewed by other hams and grant awards for achieving milestones such as all states contacted. Among these web-hosted sites three are quite popular, eqsl.cc, qrz.com and lotw.arrl.org . Operators generally use a PC based logging program, such as Ham Radio Deluxe (hrd) Logbook, N1MM Logger, Winlog32 or other programs to record their QSOs as they complete their contacts. Periodically hams export their recent QSOs to a standard file and upload it to one or more web-hosted sites.
Over time the web-hosted sites and the local logger(s) become inconsistent. Some of the reasons inconsistencies occur are:
hams forget to upload all of their QSOs
QSOs are uploaded twice. These duplicates should be caught by the logging site but they are not always discovered.
an operator has their clock improperly set to UTC. When the contacted operator upload their QSO log entry the two will not match, and a duplicate may be created.
QsoLogViewer is an application for aiding operators in finding duplicate QSOs and missing QSOs in these different repositories. Once duplicates are identified operator can use the edit features of the logbook to delete or correct those records. QsoLogViewer can also prepare an update file for missing QSOs so the operator can upload them into the correct logbook. You can update all your qso adif  files to include gridsquare information using the 'Action->Update Grid Info' menu option.

