1) To use protoc, go to top of project and type 
protoc -I=src/com/lvl6/proto/ --java_out=src/ src/com/lvl6/proto/*.proto

2) Useful commands if you know what they do
git ls-files --deleted -z | xargs -0 git rm

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


6) EC2 stuff
dev: 		50.18.106.161		ec2-50-18-106-161.us-west-1.compute.amazonaws.com
prod:		184.169.148.243		ec2-184-169-148-243.us-west-1.compute.amazonaws.com

ALL EC2 INSTANCES SHOULD HAVE THIS TIME ZONE
us pacific pdt America/Los Angeles
dpkg-reconfigure tzdata on ubuntu box

To move to server:
a) Build project locally through eclipse
b) Go to UtopiaServer path (type utopia), and do:
./movetoec2 50.18.106.161			(moves lib, res and bin folders)
./movetoec2 50.18.106.161 binonly (for bin only)
c) ssh ubuntu@<elastic ip>				//make a script for inside the shell.
d) sudo mkdir /vol/LostNations
e) sudo mv bin /vol/LostNations/
f) sudo mv lib /vol/LostNations/
g) 	
h) to kill existing server- type jobs to check. ps aux, kill <id>. or fg, ctrl c. rm nohup.out
i) nohup java -cp ../lib/*:./ com.lvl6.server.GameServer <public dns name> 8888 &
RUN THIS FROM /bin
nohup java -cp ../lib/*:./:../res com.lvl6.server.GameServer ec2-50-18-106-161.us-west-1.compute.amazonaws.com 8888 &
nohup java -cp ../lib/*:./:../res com.lvl6.server.GameServer ec2-184-169-148-243.us-west-1.compute.amazonaws.com 8888 &

nohup for generic logs, log4j

test apns

output goes to nohup.out- can tail it. create daily / hourly logs. look into log4j instead of nohup