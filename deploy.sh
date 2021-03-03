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
# $1 = 1979-02-26 ou <branchName>
# $2 = 18:30:00

USE_BRANCH="master"

ACTUAL_COMMIT_ID=`cat target/commitId`

if [ -n "$1" ]; then
	if [ -n "$2" ]; then
		DATE="$1 $2"
	else
		BRANCH_NAME="$1"
	fi
else
	echo -e "Tu peux choisir la version de l'api en ajoutant une date (ex ./deploy.sh 1979-02-26 18:30:00)."
fi

if [ -n "$BRANCH_NAME" ]; then
	if [[ $USE_BRANCH == *"$BRANCH_NAME"* ]]; then
		git checkout $BRANCH_NAME --force
	else
		unset BRANCH_NAME
	fi
fi

if [ -n "$DATE" ]; then
	git checkout 'master@{$DATE}' --force
elif [ -z "$BRANCH_NAME" ]; then
	git checkout master --force
fi

if [ -n "$ACTUAL_COMMIT_ID" ]; then
	git pull
	if [ "$ACTUAL_COMMIT_ID" = `git rev-parse HEAD` ]; then
		echo -e "\e[32mPas besoin de maven install, le jar est déjà crée.\e[0m"
		exit 1
	fi
fi
git pull && mvn install
if [[ "$?" -ne 0 ]] ; then
	echo -e "\e[91m\n\nLe build de l'api a échoué !\e[0m"; exit $rc
else
	echo `git rev-parse HEAD` > target/commitId
fi
echo -e "\e[32mLe jar du commit $(cat target/commitId) a été crée.\e[0m"
