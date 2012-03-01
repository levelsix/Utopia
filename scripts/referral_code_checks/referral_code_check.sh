#!/bin/sh

path='/Users/conradchan/Documents/workspace/UtopiaServer/scripts/referral_code_checks'

count=`mysql -h utopiainstance1.cx558rzcwi45.us-west-1.rds.amazonaws.com -u lvl6admin --password=robashen123 --skip-column-names < $path/referral_code_count.php`

if [ $count -lt 2000 ]
then

`/usr/bin/php $path/generate_referral_codes.php`
echo 'Generated referral codes at' `date` >> $path/referral_code_logs

else

echo 'Checked count at' `date` $count >> $path/referral_code_logs

fi
