package socialnetwork.repository.database;

import socialnetwork.domain.User;
import socialnetwork.paging.*;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDatabaseRepository implements PagingRepository<Long, User>
{
    private Connection connection;

    public UserDatabaseRepository(String url, String username, String password)
    {
        try
        {
            this.connection = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public User findOne(Long id)
    {
        User user = null;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Utilizator WHERE id = (?)");

            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next())
                return null;

            Long idUser = resultSet.getLong("id");
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String email = resultSet.getString("email");

            user = new User(firstName, lastName, email);
            user.setId(idUser);
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Iterable<User> findAll()
    {
        Set<User> users = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Utilizator");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                User user = new User(firstName, lastName, email);
                user.setId(id);
                users.add(user);
            }

            return users;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User save(User entity)
    {
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO Utilizator(id, first_name, last_name, email) VALUES (?, ?, ?, ?)");

            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getFirstName());
            statement.setString(3, entity.getLastName());
            statement.setString(4, entity.getEmail());

            statement.executeUpdate();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User delete(Long id)
    {
        User user = this.findOne(id);
        if(user == null)
            return null;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM Utilizator WHERE id = (?)");

            statement.setLong(1, id);

            statement.executeUpdate();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public User update(User entity)
    {
        User user = this.findOne(entity.getId());
        if(user == null)
            return entity;

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("UPDATE Utilizator SET first_name = (?), last_name = (?) WHERE id = (?)");

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());

            statement.executeUpdate();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        Set<User> users = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Utilizator LIMIT ? OFFSET ?");
            statement.setInt(1,pageable.getPageSize());
            statement.setInt(2,pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                User user = new User(firstName, lastName, email);
                user.setId(id);
                users.add(user);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, users.stream());
    }

    public Page<User> findAllPotentialFriends(Long id, Pageable pageable) {
        Set<User> users = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT DISTINCT id, first_name, last_name, email FROM utilizator u INNER JOIN prietenie p ON u.id = p.id1 OR u.id = p.id2 WHERE p.id1 <> (?) AND p.id2 <> (?) AND NOT(EXISTS (SELECT id1, id2 FROM prietenie WHERE id1 = u.id AND id2 = (?) OR id1 = (?) AND id2 = u.id)) LIMIT ? OFFSET ?");
            statement.setInt(1, Math.toIntExact(id));
            statement.setInt(2, Math.toIntExact(id));
            statement.setInt(3, Math.toIntExact(id));
            statement.setInt(4, Math.toIntExact(id));
            statement.setInt(5,pageable.getPageSize());
            statement.setInt(6,pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                Long idUser = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                User user = new User(firstName, lastName, email);
                user.setId(idUser);
                users.add(user);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, users.stream());
    }

    public Page<User> findUsersMatch(String text, Pageable pageable) {
        Set<User> users = new HashSet<>();
        try
        {
            String ceva = "%" + text + "%";
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Utilizator WHERE (first_name LIKE (?) OR last_name LIKE (?)) LIMIT ? OFFSET ?");
            statement.setString(1, ceva);
            statement.setString(2, ceva);
            statement.setInt(3,pageable.getPageSize());
            statement.setInt(4,pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                User user = new User(firstName, lastName, email);
                user.setId(id);
                users.add(user);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, users.stream());
    }

    public Page<User> findFriendsMatch(String text, Long id, Pageable pageable) {
        Set<User> users = new HashSet<>();
        try
        {
            String ceva = "%" + text + "%";
            PreparedStatement statement = this.connection.prepareStatement("SELECT * from Utilizator u INNER JOIN prietenie p ON (u.id = p.id1 AND (?) = p.id2 OR u.id = p.id2 AND (?) = p.id1) WHERE (first_name LIKE (?) OR last_name LIKE (?)) LIMIT ? OFFSET ?");
            statement.setInt(1, Math.toIntExact(id));
            statement.setInt(2, Math.toIntExact(id));
            statement.setString(3, ceva);
            statement.setString(4, ceva);
            statement.setInt(5,pageable.getPageSize());
            statement.setInt(6,pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                Long idF = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                User user = new User(firstName, lastName, email);
                user.setId(idF);
                users.add(user);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, users.stream());
    }
}
