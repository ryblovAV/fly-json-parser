rem Hint for http/https proxy - https://stackoverflow.com/questions/27127687/how-to-use-sbt-from-behind-proxy-in-windows-7
sbt assembly
rem Run
rem java -cp fly-json-parser-assembly-0.1.jar fly.json.parser.MainApp "./data�
rem java -cp fly-json-parser-assembly-0.1.jar fly.json.parser.MainApp "C:\git-bit\fly-json-parser-1\data"
rem "./data" - ���� � �������� � �������
copy .\target\scala-2.12\*.jar .
