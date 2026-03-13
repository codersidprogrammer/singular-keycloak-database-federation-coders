package org.opensingular.dbuserprovider.util;

import org.opensingular.dbuserprovider.persistence.RDBMS;

public class PagingUtil {

    public static class Pageable {
        private final int firstResult;
        private final int maxResults;

        public Pageable(int firstResult, int maxResults) {
            this.firstResult = firstResult;
            this.maxResults = maxResults;
        }
    }

    /**
     * Appends database-specific pagination clauses to the given SQL query.
     * <p>
     * Uses stable, version-appropriate SQL syntax for each RDBMS:
     * <ul>
     *   <li>PostgreSQL 10+, MySQL 5.7+: {@code LIMIT … OFFSET …}</li>
     *   <li>Oracle 12+, SQL Server 2012+, IBM DB2: {@code OFFSET … ROWS FETCH NEXT … ROWS ONLY}</li>
     * </ul>
     * Replaces the previous Hibernate-5-only {@code RowSelection} / {@code LimitHandler} approach
     * which was removed in Hibernate ORM 6.
     */
    public static String formatScriptWithPageable(String query, Pageable pageable, RDBMS rdbms) {
        switch (rdbms) {
            case ORACLE:
                // Oracle 12c+ ISO SQL row-limiting clause
                return query + " OFFSET " + pageable.firstResult
                        + " ROWS FETCH NEXT " + pageable.maxResults + " ROWS ONLY";
            case SQL_SERVER:
                // SQL Server 2012+ — query must already contain ORDER BY
                return query + " OFFSET " + pageable.firstResult
                        + " ROWS FETCH NEXT " + pageable.maxResults + " ROWS ONLY";
            case IBMDB2:
                // IBM DB2 11.1+ ISO SQL row-limiting clause
                return query + " OFFSET " + pageable.firstResult
                        + " ROWS FETCH NEXT " + pageable.maxResults + " ROWS ONLY";
            case POSTGRESQL:
            case MYSQL:
            default:
                // Standard LIMIT / OFFSET supported by PostgreSQL 10+ and MySQL 5.7+
                return query + " LIMIT " + pageable.maxResults
                        + " OFFSET " + pageable.firstResult;
        }
    }

}