import java.sql.*;

public class ZooManagement {
    private static final String URL = "jdbc:postgresql://localhost:5432/zoo_management";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to the database!");

            createTables(conn);
            int zooId = insertZoo(conn, "Central Zoo", "New York");
            int keeperId = insertZooKeeper(conn, "John Smith", 5, zooId);
            int animalId = insertAnimal(conn, "Leo", "Lion", 4, zooId, keeperId);

            readAnimals(conn);
            updateAnimalAge(conn, animalId, 5);
            deleteAnimal(conn, animalId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String createZooTable = "CREATE TABLE IF NOT EXISTS Zoo (" +
                "zoo_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "location VARCHAR(100) NOT NULL)";

        String createZooKeeperTable = "CREATE TABLE IF NOT EXISTS ZooKeeper (" +
                "keeper_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "experience INT NOT NULL, " +
                "zoo_id INT REFERENCES Zoo(zoo_id) ON DELETE CASCADE)";

        String createAnimalTable = "CREATE TABLE IF NOT EXISTS Animal (" +
                "animal_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "species VARCHAR(100) NOT NULL, " +
                "age INT NOT NULL, " +
                "zoo_id INT REFERENCES Zoo(zoo_id) ON DELETE CASCADE, " +
                "keeper_id INT REFERENCES ZooKeeper(keeper_id) ON DELETE SET NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createZooTable);
            stmt.executeUpdate(createZooKeeperTable);
            stmt.executeUpdate(createAnimalTable);
        }
    }

    private static int insertZoo(Connection conn, String name, String location) throws SQLException {
        String sql = "INSERT INTO Zoo (name, location) VALUES (?, ?) RETURNING zoo_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, location);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("zoo_id");
        }
        return -1;
    }

    private static int insertZooKeeper(Connection conn, String name, int experience, int zooId) throws SQLException {
        String sql = "INSERT INTO ZooKeeper (name, experience, zoo_id) VALUES (?, ?, ?) RETURNING keeper_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, experience);
            stmt.setInt(3, zooId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("keeper_id");
        }
        return -1;
    }

    private static int insertAnimal(Connection conn, String name, String species, int age, int zooId, int keeperId) throws SQLException {
        String sql = "INSERT INTO Animal (name, species, age, zoo_id, keeper_id) VALUES (?, ?, ?, ?, ?) RETURNING animal_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, species);
            stmt.setInt(3, age);
            stmt.setInt(4, zooId);
            stmt.setInt(5, keeperId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("animal_id");
        }
        return -1;
    }

    private static void readAnimals(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Animal";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("Animal: " + rs.getString("name") +
                        ", Species: " + rs.getString("species") +
                        ", Age: " + rs.getInt("age"));
            }
        }
    }

    private static void updateAnimalAge(Connection conn, int animalId, int newAge) throws SQLException {
        String sql = "UPDATE Animal SET age = ? WHERE animal_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newAge);
            stmt.setInt(2, animalId);
            stmt.executeUpdate();
            System.out.println("Animal age updated to " + newAge + " years.");
        }
    }

    private static void deleteAnimal(Connection conn, int animalId) throws SQLException {
        String sql = "DELETE FROM Animal WHERE animal_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            stmt.executeUpdate();
            System.out.println("Animal with ID " + animalId + " deleted.");
        }
    }
}

