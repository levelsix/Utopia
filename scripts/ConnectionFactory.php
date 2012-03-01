<?php

class ConnectionFactory{


	private static $factory;
	private $db;
	public static $log = array();

	function __construct() {
	}
	
	private static function getArrayInString($array, $delim) {
		$arrlength = count($array);
		$toreturn = "";
		for ($i = 0; $i < $arrlength; $i++) {
			$toreturn .= $array[$i];
			if ($i != $arrlength-1) {
				$toreturn .= " " . $delim . " ";
			}
		}
		return $toreturn;
	}

	public static function getFactory() {
		if (!self::$factory)
		self::$factory = new ConnectionFactory();
		return self::$factory;
	}

	private function getConnection() {
		if (!$this->db) {
			try {			
				include 'DBProperties.php';
				$this->db = new PDO("mysql:host=".$db_host.";dbname=".$db_name, $db_user, $db_pass);
								$this->db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
			} catch (PDOException $e) {
				echo $e;
				die("test");
				exit;
			}
		}
		return $this->db;
	}

	/*
	 * returns a PDO statement handler using the given query
	*/
	public static function SelectAsStatementHandler($query, $values) {
		$mydb = self::getFactory()->getConnection();
		$stmt = $mydb->prepare($query);

		$stmt->execute($values);
		$stmt->setFetchMode(PDO::FETCH_ASSOC);

		return $stmt;
	}


	/*
	 * $params should be an associative array from column names to values
	* used for basic inserts
	* returns success or failure
	*/
	public static function InsertIntoTableBasic($tablename, $params) {
		$mydb = self::getFactory()->getConnection();
		$questions = array();
		$keys = array();
		$values = array();
		foreach($params as $key=>$value) {
			$keys[] = $key;
			$values[] = $value;
			$questions[] = '?';
		}

		$stmtString = "INSERT INTO ". $tablename . "(";
		$stmtString .= self::getArrayInString($keys, ',') . ") VALUES (";
		$stmtString .= self::getArrayInString($questions, ',') . ")";

		$stmt = $mydb->prepare($stmtString);
		$result = $stmt->execute($values);
		return $result;
	}

	/*
	 * deletes a row from a table
	* $conditions should be an associative array from column names to values
	*/
	public static function DeleteRowFromTable($tablename, $conditions) {
		$mydb = self::getFactory()->getConnection();
		//TODO: after refactor, just eliminate getFactory, change getConnection to static, and call that?
		$stmtString = "DELETE FROM ". $tablename . " WHERE ";
		$condclauses = array();
		foreach($conditions as $key=>$value) {
			$condclauses[] = $key."=?";
			$values[] = $value;
		}
		$stmtString .= self::getArrayInString($condclauses, 'and');

		$stmt = $mydb->prepare($stmtString);
		$result = $stmt->execute($values);
		return $result;
	}
	
}
?>