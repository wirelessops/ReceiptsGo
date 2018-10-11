#!/bin/bash
	
# Some preliminary work
printf "\nSome Setup\n"
echo "----------------"

# Create some files and folders
if [ ! -e twine.txt ]; then
	touch twine.txt
	chmod 777 twine.txt
	printf "Created twine.txt file with 777 unix permissions.\n"
else
	printf "twine.txt already exists - check why, delete it and repeat.\n"
fi

if [ ! -e scripts/logs/twine-log.txt ]; then
	mkdir scripts/logs
else
	rm scripts/logs/twine-log.txt
fi
touch scripts/logs/twine-log.txt
chmod 777 scripts/logs/twine-log.txt	
printf "Created scripts/logs/twine-log.txt file with 777 unix permissions.\n"

if [ ! -d iOSValues ]; then
	mkdir iOSValues
	printf "Created iOSValues folder for translated iOS locales. \n"
fi

# Check required packages are installed
hub=`hub --version | grep "hub"`
twine=`twine --version | grep "Twine"`
if [ -z "$hub" ]; then
	echo "hub (https://github.com/github/hub) is required for the proper function of this script. Please install it and re-run this gradle task."
	exit 1
else 
	echo "hub (https://github.com/github/hub) is installed, carrying on."
fi

if [ -z "$twine" ]; then
	echo "twine (https://github.com/scelis/twine) is required for the proper function of this script. Please install it and re-run this gradle task."
	exit 1
else
	echo "twine (https://github.com/scelis/twine) is installed, carrying on."
fi

# Clone iOS repo 
if [ ! -d SmartReceiptsiOS ]; then
	git clone https://github.com/wbaumann/SmartReceiptsiOS.git
	printf "Successfully cloned iOS repo.\n"
else
	printf "Already cloned iOS repo - check why, delete it and repeat.\n"
fi

# Twine-ify all-non en langs
# First get all the language codes by parsing folder names
list=`find app/src/main/res -maxdepth 1 -type d -name '*values-*' -exec basename {} \;`
printf "\nStarting Conversions\n"
echo "-----------------------"
for dir in $list
do
	# Then Slurp Android locale Files
	if [ -e app/src/main/res/${dir}/strings.xml ]; then
		twine consume-localization-file twine.txt app/src/main/res/${dir}/strings.xml --format android --consume-all --consume-comments --developer-language en >> scripts/logs/twine-log.txt

	fi
	if [ -e app/src/main/res/${dir}/preferences.xml ]; then
		twine consume-localization-file twine.txt app/src/main/res/${dir}/preferences.xml --format android --consume-all --consume-comments --developer-language en >> scripts/logs/twine-log.txt
	fi
	
	# Create iOS locale files
	lang=`echo ${dir} | cut -f2 -d-`
	
	# Avoiding values-sw600dp and values-large folders - they're not really locale files
	if [ ${lang} != "sw600dp" ] && [ ${lang} != "large" ]; then
		# "Tranlsating" into iOS
		newDir="iOSValues/${lang}.lproj"
		echo "New Language - ${lang} - Starting Now \n" >> scripts/logs/twine-log.txt
		mkdir ${newDir}
		printf "Created iOS value folder for languge: ${lang}\n"
		twine generate-localization-file twine.txt ${newDir}/SharedLocalizable.strings --lang ${lang} >> scripts/logs/twine-log.txt
		echo "End of Language\n" >> scripts/logs/twine-log.txt
		printf "Converted language file for: ${lang} from Android -> iOS\n"
		
		# Run diffs to check for differences
		git diff --no-index --word-diff ${newDir}/SharedLocalizable.strings SmartReceiptsiOS/SmartReceipts/Supporting\ Files/${lang}.lproj/SharedLocalizable.strings > ${lang}-diff.txt
		eval $command
		# If file size is not 0, then there was a difference between the two files - move files
		if [ -s ${lang}-diff.txt ]; then
			printf "Found differences between SharedLocalizable.strings files for languge: ${lang}\n"
			mv ${newDir}/SharedLocalizable.strings SmartReceiptsiOS/SmartReceipts/Supporting\ Files/${lang}.lproj/SharedLocalizable.strings >> ${lang}-diff.txt
			printf "Copied new file to repository.\n"
		fi
		# Clean diff files
		rm ${lang}-diff.txt
	fi
done

# Twine-ify English alone because twine's bulk-import feature works poorly
# First Slurp Android locale Files
if [ -e app/src/main/res/values/strings.xml ]; then
	twine consume-localization-file twine.txt app/src/main/res/values/strings.xml --format android --consume-all --consume-comments --developer-language en >> scripts/logs/twine-log.txt

fi
if [ -e app/src/main/res/values/preferences.xml ]; then
	twine consume-localization-file twine.txt app/src/main/res/values/preferences.xml --format android --consume-all --consume-comments --developer-language en >> scripts/logs/twine-log.txt
fi

# "Tranlsating" into iOS
newDir="iOSValues/en.lproj"
echo "Hard-Coded English Language Import - Starting Now \n" >> scripts/logs/twine-log.txt
mkdir ${newDir}
printf "Created iOS value folder for languge: en\n"
twine generate-localization-file twine.txt ${newDir}/SharedLocalizable.strings --lang en >> scripts/logs/twine-log.txt
echo "End of Language\n" >> scripts/logs/twine-log.txt
printf "Converted language file for: en from Android -> iOS\n"

# Run diffs to check for differences
git diff --no-index --word-diff ${newDir}/SharedLocalizable.strings SmartReceiptsiOS/SmartReceipts/Supporting\ Files/en.lproj/SharedLocalizable.strings > en-diff.txt
eval $command
# If file size is not 0, then there was a difference between the two files - move files
if [ -s en-diff.txt ]; then
	printf "Found differences between SharedLocalizable.strings files for languge: en\n"
	mv ${newDir}/SharedLocalizable.strings SmartReceiptsiOS/SmartReceipts/Supporting\ Files/en.lproj/SharedLocalizable.strings >> en-diff.txt
	printf "Copied new file to repository.\n"
fi
# Clean diff files
rm en-diff.txt

# Fix twine's dumb us.lproj folder issue - uncomment if it repeats
#mv SmartReceiptsiOS/SmartReceipts/Supporting\ Files/us.lproj/SharedLocalizable.strings SmartReceiptsiOS/SmartReceipts/Supporting\ Files/en.lproj/SharedLocalizable.strings
#printf "Moving us.lproj strings file -> en.lproj.\n"
#rm -rf SmartReceiptsiOS/SmartReceipts/Supporting\ Files/us.lproj
#printf "Removed us.lproj.\n"

## Push changes to repo
printf "Start Interacting With GitHub\n"
echo "-----------------------"
cd SmartReceiptsIOS
echo "Forking repository into https://github.com/twine-botty-bot/SmartReceiptsiOS"
hub fork
echo "Adding new fork as remote"
git remote add fork https://github.com/twine-botty-bot/SmartReceiptsiOS.git
echo "Adding & committing new langauge files"
git add .
git commit -m "New Translation Files From Twine - Android -> iOS"
echo "Pushing changes into fork"
echo "twine-botty-bot tjt6VbG5d5jP" | git push fork master
echo "Creating pull request in wbaumann/SmartReceiptsiOS"
git status
hub pull-request -f -m "New Translation Files From Twine - Android -> iOS"
echo "^ This is the new pull request ^"
echo "Deleting fork"
hub delete -y twine-botty-bot/SmartReceiptsiOS

# If twine.txt is empty, then the import failed. Else - probable success
echo "Going up a folder, back to /scripts"
cd ..
if [ ! -s twine.txt ];
then
	printf "The twine.txt file is empty, hence the import failed! Did you check the strings.xml & preferences.xml files exist?\n"
else
	printf "twine.txt file seems fine.\n"
fi

# Clean up after yourself - delete everything
printf "\nCleaning Up!\n"
echo "----------------"
rm twine.txt
printf "Removed twine.txt\n"
rm -Rf iOSValues
printf "Removed the iOSValues folder\n"
rm -Rf SmartReceiptsiOS
printf "Removed the cloned git repo for SmartReceiptsiOS\n"
printf "Make sure to check scripts/logs/twine-log.txt for any errors that twine sent out.\n"