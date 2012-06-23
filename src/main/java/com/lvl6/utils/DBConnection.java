package com.lvl6.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.lvl6.properties.DBConstants;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.utilmethods.StringUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnection {
	
	public static DBConnection get(){
		return (DBConnection) AppContext.getApplicationContext().getBean("dbConnection");
	}
	
	protected Logger log = Logger.getLogger(new Object() {
	}.getClass().getEnclosingClass());


	// private final Level MCHANGE_LOG_LEVEL = Level.DEBUG;
	private final Level MCHANGE_LOG_LEVEL = Level.INFO;

	private final int SELECT_LIMIT_NOT_SET = -1;

	
	@Resource(name="dataSource")
	protected DataSource dataSource;

	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource ds) {
		log.info("Setting datasource for DBConnection: "+dataSource);
		this.dataSource = ds;
	}

	
	protected ThreadLocal<Connection> connectionManager = new ThreadLocal<Connection>() {
		
		protected Connection connec; 
		@Override
        protected Connection initialValue()
        {
			connec = DataSourceUtils.getConnection(dataSource); 
			return connec;
        }
		
		@Override 
		public Connection get() {
			try {
				if(connec == null || connec.isClosed()) {
					connec = DataSourceUtils.getConnection(dataSource); 
				}
			} catch (SQLException e) {
				log.error("Error getting connection", e);
				return null;
			}
			return connec;
		}
	};
	
	
	public Connection getConnection() {
		return connectionManager.get();
	}

	private void printConnectionInfoInDebug() {
		try {
			if(dataSource instanceof ComboPooledDataSource) {
				ComboPooledDataSource ds = (ComboPooledDataSource) dataSource;
				log.debug("num_connections: "
						+ ds.getNumConnectionsDefaultUser());
				log.debug("num_busy_connections: "
						+ ds.getNumBusyConnectionsDefaultUser());
				log.debug("num_idle_connections: "
						+ ds.getNumIdleConnectionsDefaultUser());
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	

	public void close(ResultSet rs, Statement statement, Connection conn) {
		try {
			if (rs != null) {
				statement = rs.getStatement();
				rs.close();
			}
		} catch (SQLException e) {
			log.error("The result set cannot be closed.", e);
		}
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException e) {
			log.error("The statement cannot be closed.", e);
		}
/*		try {
			//if (conn != null)
				//conn.close();
		} catch (SQLException e) {
			//log.error("The data source connection cannot be closed.", e);
		}*/
	}

	public void init() {
		Logger.getLogger("com.mchange.v2").setLevel(MCHANGE_LOG_LEVEL);
//
//		if (Globals.IS_SANDBOX) {
//			dataSource = new ComboPooledDataSource();
//		} else {
//			dataSource = new ComboPooledDataSource("production");
//		}
//		try {
//			dataSource.setDriverClass("com.mysql.jdbc.Driver");
//			dataSource.setJdbcUrl("jdbc:mysql://" + server + "/" + database);
//			dataSource.setUser(user);
//			dataSource.setPassword(password);
//		} catch (PropertyVetoException e) {
//			e.printStackTrace();
//		}
		printConnectionInfoInDebug();
	}

	public ResultSet selectRowsByUserId(Connection conn, int userId,
			String tablename) {
		return selectRowsByIntAttr(conn, null, DBConstants.GENERIC__USER_ID,
				userId, tablename);
	}

	public ResultSet selectRowsById(Connection conn, int id, String tablename) {
		return selectRowsByIntAttr(conn, null, DBConstants.GENERIC__ID, id,
				tablename);
	}

	public ResultSet selectWholeTable(Connection conn, String tablename) {
		return selectRows(null, null, null, null, tablename, null, null, false,
				SELECT_LIMIT_NOT_SET, false);
	}

	public ResultSet selectRowsAbsoluteOr(Connection conn,
			Map<String, Object> absoluteConditionParams, String tablename) {
		return selectRows(null, absoluteConditionParams, null, null, tablename,
				"or", null, false, SELECT_LIMIT_NOT_SET, false);
	}

	public ResultSet selectRowsAbsoluteAnd(Connection conn,
			Map<String, Object> absoluteConditionParams, String tablename) {
		return selectRows(null, absoluteConditionParams, null, null, tablename,
				"and", null, false, SELECT_LIMIT_NOT_SET, false);
	}

	public ResultSet selectRowsAbsoluteAndOrderbydesc(Connection conn,
			Map<String, Object> absoluteConditionParams, String tablename,
			String orderByColumn) {
		return selectRows(null, absoluteConditionParams, null, null, tablename,
				"and", orderByColumn, false, SELECT_LIMIT_NOT_SET, false);
	}

	public ResultSet selectRowsAbsoluteAndOrderbydescLimit(Connection conn,
			Map<String, Object> absoluteConditionParams, String tablename,
			String orderByColumn, int limit) {
		return selectRows(null, absoluteConditionParams, null, null, tablename,
				"and", orderByColumn, false, limit, false);
	}

	public ResultSet selectRowsAbsoluteAndOrderbydescLimitLessthan(
			Connection conn, Map<String, Object> absoluteConditionParams,
			String tablename, String orderByColumn, int limit,
			Map<String, Object> lessThanConditionParams) {
		return selectRows(null, absoluteConditionParams, null, lessThanConditionParams,
				tablename, "and", orderByColumn, false,
				limit, false);
	}

	public ResultSet selectRowsAbsoluteAndOrderbydescLimitGreaterthan(
			Connection conn, Map<String, Object> absoluteConditionParams,
			String tablename, String orderByColumn, int limit,
			Map<String, Object> greaterThanConditionParams) {
		return selectRows(null, absoluteConditionParams, greaterThanConditionParams,
				null, tablename, "and", orderByColumn,
				false, limit, false);
	}

	public ResultSet selectRowsAbsoluteAndLimitLessthanGreaterthanRand(
			Connection conn, Map<String, Object> absoluteConditionParams,
			String tablename, String orderByColumn, int limit,
			Map<String, Object> lessThanConditionParams,
			Map<String, Object> greaterThanConditionParams) {
		return selectRows(null, absoluteConditionParams, greaterThanConditionParams,
				lessThanConditionParams, tablename, "and",
				null, false, limit, true);
	}

	public ResultSet selectRowsAbsoluteAndOrderbydescGreaterthan(
			Connection conn, Map<String, Object> absoluteConditionParams,
			String tablename, String orderByColumn,
			Map<String, Object> greaterThanConditionParams) {
		return selectRows(null, absoluteConditionParams, greaterThanConditionParams,
				null, tablename, "and", orderByColumn,
				false, SELECT_LIMIT_NOT_SET, false);
	}

	/* assumes number of ? in the query = values.size() */
	public ResultSet selectDirectQueryNaive(Connection conn, String query,
			List<Object> values) {
		ResultSet rs = null;
		try {
			conn = getConnection();
			PreparedStatement stmt = conn.prepareStatement(query);
			if (values != null && values.size() > 0) {
				int i = 1;
				for (Object value : values) {
					stmt.setObject(i, value);
					i++;
				}
			}
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			log.error("problem with " + query + ", values are " + values, e);
		} catch (NullPointerException e) {
			log.error("problem with " + query + ", values are " + values, e);
		}
		return rs;
	}

	/* assumes number of ? in the query = values.size() */
	public int updateDirectQueryNaive(String query, List<Object> values) {
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = getConnection();
			stmt = conn.prepareStatement(query);
			if (values != null && values.size() > 0) {
				int i = 1;
				for (Object value : values) {
					stmt.setObject(i, value);
					i++;
				}
			}
			int numUpdated = stmt.executeUpdate();
			return numUpdated;
		} catch (SQLException e) {
			log.error("problem with " + query + ", values are " + values, e);
		} catch (NullPointerException e) {
			log.error("problem with " + query + ", values are " + values, e);
		} finally {
			close(null, stmt, conn);
		}
		return 0;
	}

	/*
	 * returns num of rows affected
	 */
	public int updateTableRows(String tablename,
			Map<String, Object> relativeParams,
			Map<String, Object> absoluteParams,
			Map<String, Object> conditionParams, String condDelim) {
		String query = "update " + tablename;
		List<Object> values = new LinkedList<Object>();

		int numUpdated = 0;

		List<String> relUpClauses = null;
		if ((relativeParams != null && relativeParams.size() > 0)
				|| (absoluteParams != null && absoluteParams.size() > 0)) {
			query += " set ";
			if (relativeParams != null && relativeParams.size() > 0) {

				relUpClauses = new LinkedList<String>();
				for (String param : relativeParams.keySet()) {
					relUpClauses.add(param + "=" + param + "+?");
					values.add(relativeParams.get(param));
				}
				query += StringUtils.getListInString(relUpClauses, ",");
			}
			if (absoluteParams != null && absoluteParams.size() > 0) {
				List<String> absUpClauses = new LinkedList<String>();
				for (String param : absoluteParams.keySet()) {
					absUpClauses.add(param + "=?");
					values.add(absoluteParams.get(param));
				}
				if (relUpClauses != null && relUpClauses.size() > 0) {
					query += ",";
				}
				query += StringUtils.getListInString(absUpClauses, ",");
			}
		} else {
			return numUpdated;
		}

		if (conditionParams != null && conditionParams.size() > 0) {
			query += " where ";
			List<String> condClauses = new LinkedList<String>();
			for (String param : conditionParams.keySet()) {
				condClauses.add(param + "=?");
				values.add(conditionParams.get(param));
			}
			query += StringUtils.getListInString(condClauses, condDelim);
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.prepareStatement(query);
			if (values.size() > 0) {
				int i = 1;
				for (Object value : values) {
					stmt.setObject(i, value);
					i++;
				}
			}
			numUpdated = stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("problem with " + query + ", values are " + values, e);
			e.printStackTrace();
		} finally {
			close(null, stmt, conn);
		}
		return numUpdated;
	}

	public int insertIntoTableBasic(String tablename,
			Map<String, Object> insertParams) {
		List<String> questions = new LinkedList<String>();
		List<String> columns = new LinkedList<String>();
		List<Object> values = new LinkedList<Object>();
		int numUpdated = 0;

		if (insertParams != null && insertParams.size() > 0) {
			for (String column : insertParams.keySet()) {
				questions.add("?");
				columns.add(column);
				values.add(insertParams.get(column));
			}
			String query = "insert into " + tablename + "("
					+ StringUtils.getListInString(columns, ",") + ") VALUES ("
					+ StringUtils.getListInString(questions, ",") + ")";

			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = getConnection();
				stmt = conn.prepareStatement(query);
				if (values.size() > 0) {
					int i = 1;
					for (Object value : values) {
						stmt.setObject(i, value);
						i++;
					}
				}
				numUpdated = stmt.executeUpdate();
			} catch (SQLException e) {
				log.error("problem with " + query + ", values are " + values, e);
				e.printStackTrace();
			} finally {
				close(null, stmt, conn);
			}
		}
		return numUpdated;
	}

	/*
	 * assumes every list for each column is numRows length
	 */
	public int insertIntoTableMultipleRows(String tablename,
			Map<String, List<Object>> insertParams, int numRows) {
		List<String> questionsPerRow = new LinkedList<String>();
		List<String> columns = new LinkedList<String>();
		List<Object> values = new LinkedList<Object>();

		int numUpdated = 0;

		if (numRows > 0 && insertParams != null && insertParams.size() > 0) {
			boolean firstTime = true;
			for (int i = 0; i < numRows; i++) {
				for (String column : insertParams.keySet()) {
					List<Object> valuesForColumn = insertParams.get(column);
					values.add(valuesForColumn.get(i));

					if (firstTime) {
						if (valuesForColumn.size() != numRows) {
							return 0;
						}
						columns.add(column);
						questionsPerRow.add("?");
					}
				}
				firstTime = false;
			}

			String query = "insert into " + tablename + "("
					+ StringUtils.getListInString(columns, ",") + ") VALUES ";

			List<String> valuesStrings = new ArrayList<String>();
			String rowQuestionsString = StringUtils.getListInString(
					questionsPerRow, ",");
			for (int i = 0; i < numRows; i++) {
				valuesStrings.add("(" + rowQuestionsString + ")");
			}
			query += StringUtils.getListInString(valuesStrings, ",");

			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = getConnection();
				stmt = conn.prepareStatement(query);
				if (values.size() > 0) {
					int i = 1;
					for (Object value : values) {
						stmt.setObject(i, value);
						i++;
					}
				}
				numUpdated = stmt.executeUpdate();
			} catch (SQLException e) {
				log.error("problem with " + query + ", values are " + values, e);
				e.printStackTrace();
			} finally {
				close(null, stmt, conn);
			}
		}
		return numUpdated;
	}

	/* returns 0 if error */
	public int insertIntoTableBasicReturnId(String tablename,
			Map<String, Object> insertParams) {
		List<String> questions = new LinkedList<String>();
		List<String> columns = new LinkedList<String>();
		List<Object> values = new LinkedList<Object>();

		int generatedKey = 0;
		if (insertParams != null && insertParams.size() > 0) {
			for (String column : insertParams.keySet()) {
				questions.add("?");
				columns.add(column);
				values.add(insertParams.get(column));
			}
			String query = "insert into " + tablename + "("
					+ StringUtils.getListInString(columns, ",") + ") VALUES ("
					+ StringUtils.getListInString(questions, ",") + ")";
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = getConnection();
				stmt = conn.prepareStatement(query,
						Statement.RETURN_GENERATED_KEYS);
				if (values.size() > 0) {
					int i = 1;
					for (Object value : values) {
						stmt.setObject(i, value);
						i++;
					}
				}
				int numUpdated = stmt.executeUpdate();
				if (numUpdated == 1) {
					ResultSet rs = stmt.getGeneratedKeys();
					if (rs.next()) {
						generatedKey = rs.getInt(1);
					}
				}
			} catch (SQLException e) {
				log.error("problem with " + query + ", values are " + values, e);
				e.printStackTrace();
			} finally {
				close(null, stmt, conn);
			}
		}
		return generatedKey;
	}

	public int insertOnDuplicateKeyRelativeUpdate(String tablename,
			Map<String, Object> insertParams, String columnUpdate,
			Object updateQuantity) {

		List<String> questions = new LinkedList<String>();
		List<String> columns = new LinkedList<String>();
		List<Object> values = new LinkedList<Object>();

		int numUpdated = 0;

		if (insertParams != null && insertParams.size() > 0) {
			for (String column : insertParams.keySet()) {
				questions.add("?");
				columns.add(column);
				values.add(insertParams.get(column));
			}
			values.add(updateQuantity);
			String query = "insert into " + tablename + "("
					+ StringUtils.getListInString(columns, ",") + ") VALUES ("
					+ StringUtils.getListInString(questions, ",")
					+ ") on duplicate key update " + columnUpdate + "="
					+ columnUpdate + "+?";
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = getConnection();
				stmt = conn.prepareStatement(query);
				if (values.size() > 0) {
					int i = 1;
					for (Object value : values) {
						stmt.setObject(i, value);
						i++;
					}
				}
				numUpdated = stmt.executeUpdate();
			} catch (SQLException e) {
				log.error("problem with " + query + ", values are " + values, e);
				e.printStackTrace();
			} finally {
				close(null, stmt, conn);
			}
		}
		return numUpdated;
	}

	/*
	 * returns num of rows affected
	 */
	public int deleteRows(String tablename,
			Map<String, Object> absoluteConditionParams, String condDelim) {
		String query = "delete from " + tablename;
		List<Object> values = new LinkedList<Object>();

		if (absoluteConditionParams != null
				&& absoluteConditionParams.size() > 0) {
			query += " where ";
			List<String> condClauses = new LinkedList<String>();
			for (String param : absoluteConditionParams.keySet()) {
				condClauses.add(param + "=?");
				values.add(absoluteConditionParams.get(param));
			}
			query += StringUtils.getListInString(condClauses, condDelim);
		}

		int numDeleted = 0;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.prepareStatement(query);
			if (values.size() > 0) {
				int i = 1;
				for (Object value : values) {
					stmt.setObject(i, value);
					i++;
				}
			}
			numDeleted = stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("problem with " + query + ", values are " + values, e);
			e.printStackTrace();
		} finally {
			close(null, stmt, conn);
		}
		return numDeleted;
	}

	private ResultSet selectRowsByIntAttr(Connection conn,
			List<String> columns, String attr, int value, String tablename) {
		String query = "select ";
		if (columns != null) {
			query += StringUtils.getListInString(columns, ",");
		} else {
			query += "* ";
		}
		query += " from " + tablename + " where " + attr + "=?";
		conn = getConnection();
		ResultSet rs = null;
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, value);
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			log.error("problem with " + query + ", int is " + value, e);
		} catch (NullPointerException e) {
			log.error("problem with " + query, e);
		}
		return rs;
	}

	private ResultSet selectRows(List<String> columns, Map<String, Object> absoluteConditionParams,
			Map<String, Object> relativeGreaterThanConditionParams,
			Map<String, Object> relativeLessThanConditionParams,
			String tablename,
			String conddelim, String orderByColumn, boolean orderByAsc,
			int limit, boolean random) {
		Connection conn = getConnection();
		String query = "select ";
		if (columns != null) {
			query += StringUtils.getListInString(columns, ",");
		} else {
			query += "* ";
		}
		query += " from " + tablename;

		List<String> absoluteCondClauses = new LinkedList<String>();
		List<String> lessthanCondClauses = new LinkedList<String>();
		List<String> greaterthanCondClauses = new LinkedList<String>();

		List<Object> values = new LinkedList<Object>();

		boolean useWhere = true;
		if (absoluteConditionParams != null
				&& absoluteConditionParams.size() > 0) {
			for (String param : absoluteConditionParams.keySet()) {
				absoluteCondClauses.add(param + "=?");
				values.add(absoluteConditionParams.get(param));
			}

			if (useWhere) {
				query += " where (";
			} else {
				query += " and (";
			}
			useWhere = false;
			query += StringUtils
					.getListInString(absoluteCondClauses, conddelim);
			query += " ) ";
		}

		if (relativeGreaterThanConditionParams != null
				&& relativeGreaterThanConditionParams.size() > 0) {
			for (String param : relativeGreaterThanConditionParams.keySet()) {
				greaterthanCondClauses.add(param + ">?");
				values.add(relativeGreaterThanConditionParams.get(param));
			}

			if (useWhere) {
				query += " where (";
			} else {
				query += " and (";
			}
			useWhere = false;
			query += StringUtils.getListInString(greaterthanCondClauses,
					conddelim);
			query += " ) ";
		}

		if (relativeLessThanConditionParams != null
				&& relativeLessThanConditionParams.size() > 0) {
			for (String param : relativeLessThanConditionParams.keySet()) {
				lessthanCondClauses.add(param + "<?");
				values.add(relativeLessThanConditionParams.get(param));
			}

			if (useWhere) {
				query += " where (";
			} else {
				query += " and (";
			}
			useWhere = false;
			query += StringUtils
					.getListInString(lessthanCondClauses, conddelim);
			query += " ) ";
		}

		if (orderByColumn != null) {
			query += " order by " + orderByColumn;
			if (!orderByAsc) {
				query += " desc";
			}
		} else if (random) {
			query += " order by rand() ";
		}

		if (limit != SELECT_LIMIT_NOT_SET) {
			query += " limit " + limit;
		}

		ResultSet rs = null;
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			if (values.size() > 0) {
				int i = 1;
				for (Object value : values) {
					stmt.setObject(i, value);
					i++;
				}
			}
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			log.error("problem with " + query + ", values are " + values, e);
		} catch (NullPointerException e) {
			log.error("problem with " + query + ", values are " + values, e);
		}
		return rs;
	}

	
}
