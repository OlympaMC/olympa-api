#!/bin/sh
##   ____  _
##  / __ \| |
## | |  | | |_   _ _ __ ___  _ __   __ _
## | |  | | | | | | '_ ` _ \| '_ \ / _` |
## | |__| | | |_| | | | | | | |_) | (_| |
##  \____/|_|\__, |_| |_| |_| .__/ \__,_|
##            __/ |         | |
##           |___/          |_|
##
## Script to deploy api with good version
## Author > Tristiisch
#
# ./deploy.sh date "2021-02-26 18:30:00"
# ./deploy.sh master
# ./deploy.sh dev
#

# PARAMETRES
PLUGIN_NAME="api"
USE_BRANCH="master"
ACTUAL_COMMIT_ID=`cat target/commitId`

if [ -n "$1" ]; then
	if [ -n "$2" ]; then
		DATE="$1 $2"
	else
		BRANCH_NAME="$1"
	fi
else
	echo -e "\e[0;36mTu peux choisir la version du core en ajoutant une date (ex './deploy.sh date \"2021-02-26 18:30:00\"') ou une branch (ex './deploy.sh dev').\e[0m"
fi
git pull --all
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mEchec du git pull, tentative de git reset\e[0m"
	git reset --hard HEAD
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git reset !\e[0m" && rm target/commit*; exit 1
	fi
	git pull --all
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git pull !\e[0m" && rm target/commit*; exit 1
	fi
fi
if [ -n "$BRANCH_NAME" ]; then
	exists=`git show-ref refs/heads/$BRANCH_NAME`
	if [ -n "$exists" ]; then
		git checkout $BRANCH_NAME --force
	fi
fi
if [ -n "$DATE" ]; then
	git checkout 'master@{$DATE}' --force
elif [ -z "$BRANCH_NAME" ]; then
	git checkout master --force
fi
if [ -n "$ACTUAL_COMMIT_ID" ]; then
	if [ "$ACTUAL_COMMIT_ID" = `git rev-parse HEAD` ]; then
		echo -e "\e[32mPas besoin de maven install l'$PLUGIN_NAME, le jar est déjà crée.\e[0m"
		exit 0
	fi
fi
mvn install
if [ "$?" -ne 0 ]; then
	echo -e "\e[91m\n\nLe build de l'$PLUGIN_NAME a échoué !\e[0m"&& rm target/commit*; exit 1
else
	echo `git rev-parse HEAD` > target/commitId
fi
echo -e "\e[32mLe jar du commit de l'$PLUGIN_NAME $(cat target/commitId) a été crée.\e[0m"
