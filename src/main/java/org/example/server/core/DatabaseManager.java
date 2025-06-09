package org.example.server.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.example.common.network.User;
import org.example.server.ServerApp;
import org.example.common.models.*;


public class DatabaseManager {
    private Connection connection;
    private MessageDigest md;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrs" +
            "tuvwxyz0123456789<>?:@{!$%^&*()_+£$";
    private static final String PEPPER = "[g$J*(l;";
    private static final Logger databaseLogger = Logger.getLogger(DatabaseManager.class.getName());

    public DatabaseManager(){
        try {
            md = MessageDigest.getInstance(ServerApp.HASHING_ALGORITHM);

            this.connect();
            this.createMainBase();
        } catch (SQLException e) {
            databaseLogger.log(Level.WARNING, "Ошибка при исполнении изначального запроса либо таблицы уже созданы", e);
        } catch (NoSuchAlgorithmException e) {
            databaseLogger.log(Level.SEVERE, "Такого алгоритма нет!", e);
        }
    }

    public void connect(){
        Properties info = null;
        try {
            info = new Properties();
            info.load(new FileInputStream(ServerApp.DATABASE_CONFIG_PATH));
            connection = DriverManager.getConnection(ServerApp.DATABASE_URL, info);
            databaseLogger.info("Успешно подключен к базе данных");
        } catch (SQLException | IOException e) {
            try {
                connection = DriverManager.getConnection(ServerApp.DATABASE_URL_HELIOS, info);
                databaseLogger.info("Успешно подключен к базе данных (Helios)");
            } catch (SQLException ex) {
                databaseLogger.log(Level.SEVERE, "Невозможно подключиться к базе данных", ex);
                databaseLogger.log(Level.FINE, "Исходная ошибка:", e);
                System.exit(1);
            }
        }
    }

    public void createMainBase() throws SQLException {
        connection
                .prepareStatement(DatabaseCommands.allTablesCreation)
                .execute();
        databaseLogger.info("Таблицы созданы");
    }

    public void addUser(User user) throws SQLException {
        String login = user.name();
        String salt = this.generateRandomString();
        String pass = PEPPER + user.password() + salt;

        if (this.checkExistUser(login)) throw new SQLException("User already exists");

        try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.addUser)) {
            ps.setString(1, login);
            ps.setString(2, this.getSHA512Hash(pass));
            ps.setString(3, salt);
            ps.execute();
            databaseLogger.info("Добавлен юзер " + user);
        }
    }

    public boolean confirmUser(User inputUser){
        try (PreparedStatement getUser = connection.prepareStatement(DatabaseCommands.getUser)) {
            String login = inputUser.name();
            getUser.setString(1, login);
            ResultSet resultSet = getUser.executeQuery();
            if(resultSet.next()) {
                String salt = resultSet.getString("salt");
                String toCheckPass = this.getSHA512Hash(PEPPER + inputUser.password() + salt);
                return toCheckPass.equals(resultSet.getString("password"));
            } else {
                return false;
            }
        } catch (SQLException e) {
            databaseLogger.log(Level.SEVERE, "Неверная команда sql!", e);
            return false;
        }
    }

    public boolean checkExistUser(String login) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.getUser)) {
            ps.setString(1, login);
            ResultSet resultSet = ps.executeQuery();
            return resultSet.next();
        }
    }

    public int addObject(StudyGroup studyGroup, User user) {
        try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.addObject)) {
            ps.setString(1, studyGroup.getName());
            ps.setFloat(2, studyGroup.getCoordinates().getX());
            ps.setDouble(3, studyGroup.getCoordinates().getY());
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setLong(5, studyGroup.getStudentsCount());
            ps.setLong(6, studyGroup.getShouldBeExpelled());
            ps.setObject(7, studyGroup.getFormOfEducation().name(), Types.OTHER);  // MATCH ENUM TYPE
            ps.setString(8, studyGroup.getGroupAdmin().getName());
            ps.setInt(9, studyGroup.getGroupAdmin().getWeight());
            ps.setObject(10, studyGroup.getGroupAdmin().getEyeColor().name(), Types.OTHER);
            ps.setObject(11, studyGroup.getGroupAdmin().getHairColor().name(), Types.OTHER);
            ps.setObject(12, studyGroup.getGroupAdmin().getNationality().name(), Types.OTHER);
            ps.setDouble(13, studyGroup.getGroupAdmin().getLocation().getX());
            ps.setDouble(14, studyGroup.getGroupAdmin().getLocation().getY());
            ps.setString(15, studyGroup.getGroupAdmin().getLocation().getName());
            ps.setString(16, user.name());

            ResultSet resultSet = ps.executeQuery();

            if (!resultSet.next()) {
                databaseLogger.info("Объект не добавлен в таблицу");
                return -1;
            }
            databaseLogger.info("Объект добавлен в таблицу");
            return resultSet.getInt(1);
        } catch (SQLException e) {
            databaseLogger.log(Level.INFO, "Объект не добавлен в таблицу", e);
            return -1;
        }
    }


    public boolean updateObject(int id, StudyGroup studyGroup, User user){
        try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.updateUserObject)) {
            ps.setString(1, studyGroup.getName());
            ps.setFloat(2, studyGroup.getCoordinates().getX());
            ps.setDouble(3, studyGroup.getCoordinates().getY());
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setLong(5, studyGroup.getStudentsCount());
            ps.setLong(6, studyGroup.getShouldBeExpelled());
            ps.setObject(7, studyGroup.getSemesterEnum().name());
            ps.setString(7, studyGroup.getFormOfEducation().name());
            ps.setString(9, studyGroup.getGroupAdmin().getName());
            ps.setInt(10, studyGroup.getGroupAdmin().getWeight());
            ps.setObject(11, studyGroup.getGroupAdmin().getEyeColor(), Types.OTHER);
            ps.setObject(12, studyGroup.getGroupAdmin().getHairColor(), Types.OTHER);
            ps.setObject(13, studyGroup.getGroupAdmin().getNationality(), Types.OTHER);
            ps.setDouble(14, studyGroup.getGroupAdmin().getLocation().getX());
            ps.setDouble(15, studyGroup.getGroupAdmin().getLocation().getY());
            ps.setString(16, studyGroup.getGroupAdmin().getLocation().getName());

            ps.setInt(17, id);
            ps.setString(18, user.name());
            ResultSet resultSet = ps.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            databaseLogger.log(Level.FINE, "Ошибка обновления объекта", e);
            return false;
        }
    }

    public boolean deleteObject(int id, User user){
        try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.deleteUserObject)) {
            ps.setString(1, user.name());
            ps.setInt(2, id);
            ResultSet resultSet = ps.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            databaseLogger.log(Level.SEVERE, "Объект удалить не удалось", e);
            return false;
        }
    }

    public boolean deleteAllObjects(User user, List<Integer> ids){
        try {
            for (Integer id : ids) {
                try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.deleteUserOwnedObjects)) {
                    ps.setString(1, user.name());
                    ps.setInt(2, id);
                    ps.execute();
                }
            }
            databaseLogger.log(Level.WARNING, "Удалены все строки таблицы studygroup принадлежащие " + user.name());
            return true;
        } catch (SQLException e) {
            databaseLogger.log(Level.SEVERE, "Удалить строки таблицы studygroup не удалось!", e);
            return false;
        }
    }

    public ArrayDeque<StudyGroup> loadCollection() {
        try (PreparedStatement ps = connection.prepareStatement(DatabaseCommands.getAllObjects)) {
            ResultSet resultSet = ps.executeQuery();
            ArrayDeque<StudyGroup> collection = new ArrayDeque<>();
            while (resultSet.next()) {
                collection.add(new StudyGroup(
                        resultSet.getInt("id"),
                        resultSet.getString("group_name"),
                        new Coordinates(
                                (int) resultSet.getFloat("cord_x"),
                                (int) resultSet.getDouble("cord_y")
                        ),
                        resultSet.getObject("creation_date", LocalDate.class),
                        resultSet.getLong("students_count"),
                        resultSet.getObject("expelled_students", Long.class),
                        FormOfEducation.valueOf(resultSet.getString("form_of_education")),
                        Semester.valueOf(resultSet.getString("semester_enum")),
                        new Person(
                                resultSet.getString("person_name"),
                                resultSet.getInt("person_weight"),
                                Color.valueOf(resultSet.getString("person_eye_color")),
                                Color.valueOf(resultSet.getString("person_hair_color")),
                                Country.valueOf(resultSet.getString("person_nationality")),
                                new Location(
                                        (int) resultSet.getDouble("person_location_x"),
                                        resultSet.getLong("person_location_y"),
                                        resultSet.getString("person_location_name")
                                )
                        ),
                        resultSet.getString("owner_login")
                ));
            }
            databaseLogger.info("Коллекция успешно загружена из таблицы");
            return collection;
        } catch (SQLException e) {
            databaseLogger.log(Level.WARNING, "Коллекция пуста либо возникла ошибка при исполнении запроса", e);
            return new ArrayDeque<>();
        }
    }


    private String generateRandomString() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String getSHA512Hash(String input){
        byte[] inputBytes = input.getBytes();
        md.update(inputBytes);
        byte[] hashBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
