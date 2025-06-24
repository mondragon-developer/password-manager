@echo off
echo Generating JavaDocs...
mkdir docs 2>nul
javadoc -d docs -sourcepath src -subpackages com.passwordmanager -windowtitle "Password Manager API Documentation" -doctitle "Password Manager Application v1.0" -author -version -use -private -splitindex -bottom "Copyright © 2025 Jose Mondragon. All rights reserved."
echo JavaDocs generated in ./docs directory
pause