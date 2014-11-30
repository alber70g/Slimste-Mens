public class ExampleClassUsesMySQLConnectionPool {
     private static final Log LOG = LogFactory.getLog(ExampleClassUsesMySQLConnectionPool.class);
     private static final String SQL_SELECT = "SELECT ... FROM ... ORDER BY ... DESC LIMIT ...";
     private final ObjectPool connPool;
 
     public ExampleClassUsesMySQLConnectionPool(ObjectPool connPool) {
          this.connPool = connPool;
     }
 
     public List<SomeRecord> getRecords(String sql) throws SQLException, MySqlPoolableException {
          Builder<SomeRecord> builder = new ImmutableList.Builder<SomeRecord>();
          Connection conn = null;
          Statement st = null;
          ResultSet res = null;
          try {
               conn = (Connection)connPool.borrowObject();
               st = conn.createStatement();
               res = st.executeQuery(sql);
               while (res.next()) {
                    SomeRecord someRecord = new SomeRecord(String.valueOf(res.getInt(1)),
                    String.valueOf(res.getInt(2)), res.getString(3));
                    builder.add(someRecord);
               }
          } catch (SQLException e) {
               throw e;
          }  catch (Exception e) {
               throw new MySqlPoolableException("Failed to borrow connection from the pool", e);
          } finally {
               safeClose(res);
               safeClose(st);
               safeClose(conn);
          }
          return builder.build();
     }
 
     private void safeClose(Connection conn) {
          if (conn != null) {
               try {
                    connPool.returnObject(conn);
               }
               catch (Exception e) {
                    LOG.warn("Failed to return the connection to the pool", e);
               }
          }
     }
 
     private void safeClose(ResultSet res) {
          if (res != null) {
               try {
                    res.close();
               } catch (SQLException e) {
                    LOG.warn("Failed to close databse resultset", e);
               }
          }
     }
 
     private void safeClose(Statement st) {
          if (st != null) {
               try {
                    st.close();
               } catch (SQLException e) {
                    LOG.warn("Failed to close databse statment", e);
               }
          }
     }
}