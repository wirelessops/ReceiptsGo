git update-index --assume-unchanged app/src/free/res/values/ads.xml
git update-index --assume-unchanged app/src/free/res/xml/analytics.xml
git update-index --assume-unchanged app/src/main/res/values/secrets.xml
gpg -d secrets.tar.gpg | tar xv || true
