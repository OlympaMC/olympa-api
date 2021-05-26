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
	echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME, tentative de git reset\e[0m"
	git reset --hard HEAD
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git reset pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
	git pull --all
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
fi
if [ -n "$BRANCH_NAME" ]; then
	commit_id=`git rev-parse -q --verify $BRANCH_NAME`
	if [ -n "$commit_id" ]; then
		git checkout $commit_id --force
	else
		echo -e "\e[91mLa branch ou commit id $BRANCH_NAME n'existe pas pour $PLUGIN_NAME !\e[0m"; exit 1
	fi
fi
if [ -n "$DATE" ]; then
	git checkout 'master@{$DATE}' --force
elif [ -z "$BRANCH_NAME" ]; then
	echo -e "\e[32mIl faut ajouter une branch en argument 1. Souvent dev ou master, marche aussi avec un commit.\e[0m"
	exit 0
fi
if [ -n "$ACTUAL_COMMIT_ID" ]; then
	if [ "$ACTUAL_COMMIT_ID" = `git rev-parse HEAD` ]; then
		echo -e "\e[32mPas besoin de maven install l'$PLUGIN_NAME, le jar est déjà crée.\e[0m"
		exit 0
	fi
fi
gradle publishToMavenLocal
if [ "$?" -ne 0 ]; then
	echo -e "\e[91m\n\nLe build de l'$PLUGIN_NAME a échoué !. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
else
	echo `git rev-parse HEAD` > target/commitId
fi
echo -e "\e[32mLe jar du commit de l'$PLUGIN_NAME $(cat target/commitId) a été crée.\e[0m"
