package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Small JDBC helper that removes the connection / statement / close
 * boilerplate that used to be repeated in every class. All resources are
 * closed automatically and every SQLException becomes a DataAccessException.
 */
final class Db {

    @FunctionalInterface
    interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    static final Binder NO_PARAMS = ps -> { };

    private Db() { }

    /** Runs INSERT / UPDATE / DELETE and returns the affected row count. */
    static int update(String sql, String action, Binder binder) {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            binder.bind(ps);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    /** Runs an INSERT and returns the generated primary key. */
    static int insertReturningId(String sql, String action, Binder binder) {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            binder.bind(ps);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }

        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    static <T> List<T> query(String sql, String action,
                             Binder binder, RowMapper<T> mapper) {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {

                List<T> rows = new ArrayList<>();

                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }

                return rows;
            }

        } catch (SQLException e) {
            throw DataAccessException.of(action, e);
        }
    }

    static <T> Optional<T> queryOne(String sql, String action,
                                    Binder binder, RowMapper<T> mapper) {

        return query(sql, action, binder, mapper).stream().findFirst();
    }
}
