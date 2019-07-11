#!/bin/bash

mkdir ~/bash_completion.d
mkdir ~/.ananas

rm -rf ~/.ananas/*.jar

cp *.jar ~/.ananas

chmod 755 ~/.ananas/*.jar

echo '#!/usr/bin/env bash' > ananas
echo 'java -jar "$(echo ~/*.jar)" $@' >> ananas 
chmod 755 ananas

yes | cp -rf ananas /usr/local/bin

java -cp *.jar picocli.AutoComplete -n ananas org.ananas.cli.commands.MainCommand -f 

cp ananas_completion ~/bash_completion.d

[ -f ~/.bashrc ] && printf "# ananas cli \nfor bcfile in ~/bash_completion.d/* ; do\n   . \$bcfile\ndone\n" >> ~/.bashrc

[ -f ~/.zshrc ] && printf "# ananas cli \nautoload -U +X compinit && compinit\nautoload -U +X bashcompinit && bashcompinit\nfor bcfile in ~/bash_completion.d/* ; do\n  . \$bcfile\ndone\n" >> ~/.zshrc
