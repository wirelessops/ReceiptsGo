git update-index --assume-unchanged app/src/free/res/values/ads.xml 2> /dev/null
git update-index --assume-unchanged app/src/free/res/xml/analytics.xml 2> /dev/null
git update-index --assume-unchanged app/src/main/res/values/secrets.xml 2> /dev/null
gpg -d secrets.tar.gpg | tar xv 2> /dev/null
