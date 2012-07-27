1) To use protoc, go to top of project and type
protoc -I=src/main/java/com/lvl6/proto/ --java_out=src/main/java/ src/main/java/com/lvl6/proto/*.proto

2) Useful commands if you know what they do
git ls-files --deleted -z | xargs -0 git rm
lsof -w -n -i tcp:8888

3) Make sure you include this file (but don't push it to repo (it's already in .gitignore, if right path)):
DBProperties.java
in com.lvl6.properties package.

Should look like this:
package com.lvl6.properties;

public class DBProperties {
  
  public static final String USER = <fill in your info>;
  public static final String PASSWORD = <fill in your info>;
  public static final String SERVER = <fill in your info>;
  public static final String DATABASE = <fill in your info>;
    
}


4) Also add

APNSProperties
in com.lvl6.properties package.

Should look like this:
package com.lvl6.properties;

public class APNSProperties {

  public static final String PATH_TO_CERT = <fill in your info>
  public static final String CERT_PASSWORD = <fill in your info>
  
}


4) Bottom of EquipmentRetrieveUtils.java explains how equipments are stored

5) Referral code notes when moving to prod:

At same code level as ConnectionFactory.php, make DBProperties.php. Contents are:
<?php 
$db_name = <fill in your info>;
$db_user = <fill in your info>;
$db_pass = <fill in your info>;
$db_host = <fill in your info>;
?>

Prod server cron commands: (just copy my laptops crontab -l, and crontab -r from my laptop)
MAILTO=conrad@lvl6.com
0 */12 * * * ~/<path to referral code script folder>/referral_code_check.sh

Check paths. ie change $path in referral_code_check.sh. 
Assumes log file, generate php file, and query for count file
Check variable names, etc. for everything in scripts folder.



7) Building with Maven

Install Maven and m2eclipse

http://maven.apache.org/download.html

http://eclipse.org/m2e/

cd to root of project and run:

./installJavaJsonToLocalMaven.sh 
mvn clean
mvn install



8) Building with Maven in Eclipse

After installing m2eclipse

Import Project > Maven > Existing Maven project

Choose Utopia folder root

Select the project in the tree and OK



9) Tomcat

Install tomcat7

Stop tomcat

remove all files from tomcatroot/webapps/

copy utopia/target/utopia-server-1.0-SNAPSHOT.war to tomcatroot/webapps/ROOT.war

Start tomcat






