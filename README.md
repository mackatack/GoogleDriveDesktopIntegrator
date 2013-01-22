GoogleDriveDesktopIntegrator
============================

A GreaseMonkey / TamperMonkey userScript that adds more desktop integration to the Google Drive web interface.

It does this by adding a Java applet to the Google Drive interface which acts as a gateway to the local computer. If the user clicks on a file in the Google Drive web interface the Java applet tries to find a locally synced version of the file. If one is found the file is opened using the default program on the local computer.

For example if you have your default file associations setup:

WordDocument.doc will be opened with Microsoft Office  
OpenOfficeDocument.odt will be opened with OpenOffice or LibreOffice Writer  
OpenOfficeSpreadsheet.odt will be opened with OpenOffice or LibreOffice Calc  
VectorDrawing.ai will be opened with Adobe Illustrator  
etc.


The file extensions handled are configurable per computer and by default this userscript handles the following file extensions:

odt, ods, doc, docx, xls, xlsx, ppt, pptx, vsd, zip, ai, psd

## Assumptions / requirements: ##
- A [Google Drive](http://drive.google.com) Account
- A browser with [GreaseMonkey](https://addons.mozilla.org/nl/firefox/addon/greasemonkey/) or [TamperMonkey](https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo) extension installed (Firefox or Chrome)
- Your Google Drive synched locally using [Google Drive Application](https://www.google.com/drive/start/download.html) or other Client (InSync for example)
- [Java JRE](http://java.com) 6 or higher installed


