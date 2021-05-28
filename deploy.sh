#!/bin/bash
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
# ./deploy.sh master
# ./deploy.sh dev
#

# PARAMETRES
PLUGIN_NAME="api"
USE_BRANCH="master"
ACTUAL_COMMIT_ID=`cat target/commitId`

if [ -n "$1" ]; then
	BRANCH_NAME="$1"
else
	echo -e "\e[0;36mTu dois choisir la version du $PLUGIN_NAME en ajoutant une branch (ex './deploy.sh $master')"
	echo -e "un commit (ex './deploy.sh $(git rev-parse HEAD)').\e[0m"
	exit 1
fi
git pull --all
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME, tentative de git reset\e[0m"
	git reset --hard HEAD
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git reset  pour $PLUGIN_NAME, tentative de git checkout\e[0m"
		git checkout $BRANCH_NAME --force
		if [ "$?" -ne 0 ]; then
			echo -e "\e[91mEchec du git checkout pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID[0m"; exit 1
		fi
	fi
	git pull --all
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
fi
if [ -n "$BRANCH_NAME" ]; then
	commit_id=`git rev-parse -q --verify $BRANCH_NAME`
	if [ -n "$commit_id" ]; then
		if [ "$commit_id" != `git rev-parse HEAD` ]; then
			git checkout $BRANCH_NAME --force
		fi
	else
		echo -e "\e[91mLa branch ou commit id $BRANCH_NAME n'existe pas pour $PLUGIN_NAME !\e[0m"; exit 1
	fi
fi

if [ -n "$ACTUAL_COMMIT_ID" ]; then
	if [ "$ACTUAL_COMMIT_ID" = `git rev-parse HEAD` ]; then
		echo -e "\e[32mPas besoin de maven install l'$PLUGIN_NAME, le jar est déjà crée.\e[0m"
		exit 0
	fi
fi
if [[ ${@:1} == *justGitPull ]]; then
	echo -e "\e[0;36mLe $PLUGIN_NAME n'a pas été build comme demandé, il a juste été git pull.\e[0m"; exit 0
else
	gradle publishToMavenLocal
fi
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mLe build de l'$PLUGIN_NAME a échoué !. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
else
	echo `git rev-parse HEAD` > target/commitId
fi
echo -e "\e[32mLe jar du commit de l'$PLUGIN_NAME $(cat target/commitId) a été crée.\e[0m"
