<?php
include_once '../ConnectionFactory.php';

$numToGenerate=1000000;
$referralCodeLength = 6;
$availableReferralCodesTableName = 'available_referral_codes';
$generatedReferralCodesTableName = 'generated_referral_codes';
$codeColumnName = 'code';

//$soldierCode = new SoldierCode();
$unique_entries = 0;
$referralCode;
while ($unique_entries < $numToGenerate) {
	$referralCode = generate_random($referralCodeLength);
	if(!codeExistsInAvailableReferralCodesTable($referralCode, $availableReferralCodesTableName, $codeColumnName)){
		if(insertIntoAvailableReferralCodesTable($referralCode, $availableReferralCodesTableName, $codeColumnName, $generatedReferralCodesTableName))
		{
			$unique_entries++;
		}
	}
}

// Generate any 6 digit Random Code using 0-9 and A-Z
function generate_random($referralCodeLength){
	$code = null;
	for ($i=0; $i<$referralCodeLength; $i++) {
		// Just to make more random effect.
		$chr =rand(1,30)%2;
		// Select character from 0-9 and A-Z
		$code .= $chr ? chr(rand(65,90)) : chr(rand(48,57));
	}
	return $code;
}

function codeExistsInAvailableReferralCodesTable($code, $availableReferralCodesTableName, $codeColumnName) {
	$statement = ConnectionFactory::SelectAsStatementHandler("SELECT * FROM ".$availableReferralCodesTableName." WHERE ".$codeColumnName." = ?", array($code));
	$count = $statement->rowCount();
	if($count == 0)	return false;
	return true;
}


/*
* Available Referral Codes Tables
*/
function insertIntoAvailableReferralCodesTable($code, $availableReferralCodesTableName, $codeColumnName, $generatedReferralCodesTableName) {
	$params[$codeColumnName] = $code;
	$query = ConnectionFactory::InsertIntoTableBasic($availableReferralCodesTableName,$params);
	if($query == 1)	{
		if(!codeExistsInGeneratedReferralCodesTable($code, $generatedReferralCodesTableName, $codeColumnName)) {
			if (insertIntoGeneratedReferralCodesTable($code, $generatedReferralCodesTableName, $codeColumnName)) return true;
			else deleteRowFromAvailableReferralCodesTable($code, $availableReferralCodesTableName);
		}
		else deleteRowFromAvailableReferralCodesTable($code, $availableReferralCodesTableName);
	}
	return false;
}

function deleteRowFromAvailableReferralCodesTable($code, $availableReferralCodesTableName) {
	$status = ConnectionFactory::DeleteRowFromTable($availableReferralCodesTableName, array('code'=>$code));
	return $status;
}


/*
 * Generated Referral Codes Tables
 */
function insertIntoGeneratedReferralCodesTable($code, $generatedReferralCodesTableName, $codeColumnName) {
	
	$params[$codeColumnName] = $code;
	$query = ConnectionFactory::InsertIntoTableBasic($generatedReferralCodesTableName, $params);
	return $query;
}

function codeExistsInGeneratedReferralCodesTable($code, $generatedReferralCodesTableName, $codeColumnName) {
	$statement = ConnectionFactory::SelectAsStatementHandler("SELECT * FROM ".$generatedReferralCodesTableName." WHERE ".$codeColumnName." = ?", array($code));
	$count = $statement->rowCount();
	if($count == 0)	return false;
	return true;
}

?>


